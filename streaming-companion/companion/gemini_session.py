"""Wires screen capture, mic/speaker audio, chat, and topics into a Gemini Live API session.

The Gemini Live API is a bidirectional streaming API built for exactly this kind of
use case (real-time voice conversation plus a live video/screen feed - see Google's
"Project Astra" demos), so it does the heavy lifting: speech-to-text, text-to-speech,
and native vision understanding of the frames we stream to it all happen server-side.
"""

from __future__ import annotations

import asyncio
import logging
from typing import Callable, Optional

from .capture.audio import MicStream, SpeakerOutput, rms
from .capture.screen import stream_regions
from .chat.aggregator import ChatAggregator
from .chat.twitch import TwitchChat
from .chat.youtube import YoutubeChat
from .config import Settings
from .personality import Personality, build_system_prompt
from .topics import IdleTopicScheduler, TopicBank

log = logging.getLogger("companion")

# Mic RMS above this is treated as "the streamer is talking" for idle-topic purposes.
# 16-bit PCM full scale is 32767; typical speech at a normal mic gain sits well above this.
SPEECH_RMS_THRESHOLD = 400

TranscriptCallback = Callable[[str, str], None]


class CompanionSession:
    def __init__(
        self,
        settings: Settings,
        personality: Personality,
        topic_bank: TopicBank,
        on_transcript: Optional[TranscriptCallback] = None,
        test_mode: bool = False,
    ):
        """test_mode=True skips connecting to Twitch/YouTube chat and disables topic
        nudges, so you can run the companion solo against just your mic and screen to
        hear how it sounds and reacts, without any chat/topic config in place."""
        self.settings = settings
        self.personality = personality
        self.on_transcript = on_transcript or (lambda who, text: None)
        self.test_mode = test_mode
        self.topics = IdleTopicScheduler(topic_bank, settings.topics.idle_seconds_before_topic)
        self.chat = ChatAggregator()
        self._stop = asyncio.Event()

    def stop(self) -> None:
        self._stop.set()

    def _setup_chat_sources(self) -> None:
        if self.settings.chat.twitch.enabled and self.settings.chat.twitch.channel:
            log.info("Connecting to Twitch chat: #%s", self.settings.chat.twitch.channel)
            self.chat.add_source(TwitchChat(self.settings.chat.twitch.channel).messages())
        if self.settings.chat.youtube.enabled and self.settings.chat.youtube.video_id:
            api_key = self.settings.youtube_api_key
            if not api_key:
                log.warning("YouTube chat enabled but YOUTUBE_API_KEY is not set; skipping.")
            else:
                log.info("Connecting to YouTube live chat for video %s", self.settings.chat.youtube.video_id)
                self.chat.add_source(
                    YoutubeChat(api_key, self.settings.chat.youtube.video_id).messages()
                )

    async def run(self) -> None:
        from google import genai
        from google.genai import types

        region_names = [r.name for r in self.settings.regions]
        system_prompt = build_system_prompt(self.personality, region_names, test_mode=self.test_mode)

        client = genai.Client(api_key=self.settings.gemini_api_key)
        live_config = types.LiveConnectConfig(
            response_modalities=["AUDIO"],
            system_instruction=types.Content(parts=[types.Part(text=system_prompt)]),
            speech_config=types.SpeechConfig(
                voice_config=types.VoiceConfig(
                    prebuilt_voice_config=types.PrebuiltVoiceConfig(voice_name=self.personality.voice)
                )
            ),
            input_audio_transcription=types.AudioTranscriptionConfig(),
            output_audio_transcription=types.AudioTranscriptionConfig(),
        )

        if self.test_mode:
            log.info("Test mode: skipping chat sources and topic nudges - screen + voice only.")
        else:
            self._setup_chat_sources()

        mic = MicStream(
            sample_rate=self.settings.audio.input_sample_rate,
            device=self.settings.audio.mic_device,
        )
        speaker = SpeakerOutput(
            sample_rate=self.settings.audio.output_sample_rate,
            device=self.settings.audio.speaker_device,
        )
        mic.start()
        speaker.start()

        log.info("Connecting to Gemini Live (%s) as %s...", self.settings.gemini.model, self.personality.name)
        try:
            async with client.aio.live.connect(model=self.settings.gemini.model, config=live_config) as session:
                log.info("Connected. %s is live.", self.personality.name)
                tasks = [
                    self._pump_audio(session, mic),
                    self._pump_video(session),
                    self._receive(session, speaker),
                ]
                if not self.test_mode:
                    tasks.append(self._pump_chat(session))
                    tasks.append(self._pump_topics(session))
                await asyncio.gather(*tasks)
        finally:
            mic.stop()
            speaker.stop()
            await self.chat.aclose()

    async def _pump_audio(self, session, mic: MicStream) -> None:
        from google.genai import types

        async for chunk in mic.chunks():
            if self._stop.is_set():
                return
            if rms(chunk) > SPEECH_RMS_THRESHOLD:
                self.topics.mark_activity()
            await session.send_realtime_input(
                audio=types.Blob(
                    data=chunk,
                    mime_type=f"audio/pcm;rate={self.settings.audio.input_sample_rate}",
                )
            )

    async def _pump_video(self, session) -> None:
        from google.genai import types

        async for frame in stream_regions(self.settings.regions, self.settings.gemini.fps):
            if self._stop.is_set():
                return
            await session.send_realtime_input(video=types.Blob(data=frame, mime_type="image/jpeg"))

    async def _pump_chat(self, session) -> None:
        from google.genai import types

        interval = self.settings.chat.inject_interval_seconds
        while not self._stop.is_set():
            await asyncio.sleep(interval)
            messages = await self.chat.drain(self.settings.chat.max_messages_per_injection)
            text = self.chat.format_injection(messages)
            if text:
                self.topics.mark_activity()
                await session.send_client_content(
                    turns=types.Content(role="user", parts=[types.Part(text=text)]),
                    turn_complete=True,
                )

    async def _pump_topics(self, session) -> None:
        from google.genai import types

        while not self._stop.is_set():
            await asyncio.sleep(5)
            if self.topics.due():
                topic = self.topics.next_topic()
                if topic:
                    log.debug("Injecting idle topic: %s", topic)
                    await session.send_client_content(
                        turns=types.Content(role="user", parts=[types.Part(text=f"[TOPIC IDEA] {topic}")]),
                        turn_complete=True,
                    )

    async def _receive(self, session, speaker: SpeakerOutput) -> None:
        loop = asyncio.get_event_loop()
        async for response in session.receive():
            if self._stop.is_set():
                return
            if response.data:
                self.topics.mark_activity()
                await loop.run_in_executor(None, speaker.play, response.data)
            server_content = getattr(response, "server_content", None)
            if server_content:
                out_t = getattr(server_content, "output_transcription", None)
                if out_t and out_t.text:
                    self.on_transcript(self.personality.name, out_t.text)
                in_t = getattr(server_content, "input_transcription", None)
                if in_t and in_t.text:
                    self.on_transcript("You", in_t.text)

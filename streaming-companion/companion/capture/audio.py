"""Microphone capture and speaker playback for the Gemini Live audio stream."""

from __future__ import annotations

import array
import asyncio
import queue
from typing import AsyncIterator, Optional


def rms(pcm_bytes: bytes) -> float:
    """Root-mean-square level of 16-bit little-endian PCM audio, used for cheap voice-activity
    detection (marking the topic scheduler's idle clock as active while the streamer talks)."""
    if not pcm_bytes:
        return 0.0
    samples = array.array("h")
    samples.frombytes(pcm_bytes[: len(pcm_bytes) - (len(pcm_bytes) % 2)])
    if not samples:
        return 0.0
    return (sum(s * s for s in samples) / len(samples)) ** 0.5


class MicStream:
    """Captures microphone audio as 16-bit PCM mono chunks."""

    def __init__(self, sample_rate: int = 16000, device: Optional[str] = None, chunk_ms: int = 100):
        self.sample_rate = sample_rate
        self.device = device
        self.chunk_frames = int(sample_rate * chunk_ms / 1000)
        self._queue: "queue.Queue[bytes]" = queue.Queue()
        self._stream = None

    def _callback(self, indata, frames, time_info, status) -> None:
        self._queue.put(bytes(indata))

    def start(self) -> None:
        import sounddevice as sd

        self._stream = sd.RawInputStream(
            samplerate=self.sample_rate,
            channels=1,
            dtype="int16",
            device=self.device,
            blocksize=self.chunk_frames,
            callback=self._callback,
        )
        self._stream.start()

    def stop(self) -> None:
        if self._stream is not None:
            self._stream.stop()
            self._stream.close()
            self._stream = None

    async def chunks(self) -> AsyncIterator[bytes]:
        loop = asyncio.get_event_loop()
        while True:
            chunk = await loop.run_in_executor(None, self._queue.get)
            yield chunk


class SpeakerOutput:
    """Plays back 16-bit PCM audio chunks received from the model.

    To get this into OBS/Discord as the companion's "voice", set speaker_device to a
    virtual audio cable (e.g. VB-Audio Virtual Cable on Windows) and add that cable as
    a microphone source in OBS/Discord.
    """

    def __init__(self, sample_rate: int = 24000, device: Optional[str] = None):
        self.sample_rate = sample_rate
        self.device = device
        self._stream = None

    def start(self) -> None:
        import sounddevice as sd

        self._stream = sd.RawOutputStream(
            samplerate=self.sample_rate,
            channels=1,
            dtype="int16",
            device=self.device,
        )
        self._stream.start()

    def stop(self) -> None:
        if self._stream is not None:
            self._stream.stop()
            self._stream.close()
            self._stream = None

    def play(self, pcm_bytes: bytes) -> None:
        if self._stream is not None:
            self._stream.write(pcm_bytes)

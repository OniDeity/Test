"""Loads settings.yaml (structure/behavior) and .env (secrets) into typed config objects."""

from __future__ import annotations

import dataclasses
import os
from pathlib import Path
from typing import Optional, Union

import yaml
from dotenv import load_dotenv


@dataclasses.dataclass
class ScreenRegion:
    name: str
    x: int
    y: int
    width: int
    height: int
    description: str = ""


@dataclasses.dataclass
class TwitchConfig:
    enabled: bool = False
    channel: str = ""


@dataclasses.dataclass
class YoutubeConfig:
    enabled: bool = False
    video_id: str = ""
    api_key_env: str = "YOUTUBE_API_KEY"


@dataclasses.dataclass
class ChatConfig:
    twitch: TwitchConfig = dataclasses.field(default_factory=TwitchConfig)
    youtube: YoutubeConfig = dataclasses.field(default_factory=YoutubeConfig)
    inject_interval_seconds: float = 8.0
    max_messages_per_injection: int = 5


@dataclasses.dataclass
class GeminiConfig:
    model: str = "gemini-live-2.5-flash-preview"
    voice: str = "Puck"
    fps: float = 1.0
    api_key_env: str = "GEMINI_API_KEY"


@dataclasses.dataclass
class AudioConfig:
    mic_device: Optional[str] = None
    speaker_device: Optional[str] = None
    input_sample_rate: int = 16000
    output_sample_rate: int = 24000


@dataclasses.dataclass
class TopicsConfig:
    idle_seconds_before_topic: float = 90.0
    file: str = "config/topics.example.yaml"


@dataclasses.dataclass
class Settings:
    gemini: GeminiConfig
    audio: AudioConfig
    regions: list[ScreenRegion]
    chat: ChatConfig
    topics: TopicsConfig
    personality_file: str

    @property
    def gemini_api_key(self) -> str:
        key = os.environ.get(self.gemini.api_key_env, "")
        if not key:
            raise RuntimeError(
                f"Missing Gemini API key. Set {self.gemini.api_key_env} in your .env file "
                "(copy .env.example to .env and fill it in)."
            )
        return key

    @property
    def youtube_api_key(self) -> str:
        return os.environ.get(self.chat.youtube.api_key_env, "")


def load_settings(
    path: Union[str, Path] = "config/settings.yaml",
    env_path: Union[str, Path] = ".env",
) -> Settings:
    load_dotenv(env_path, override=False)
    path = Path(path)
    if not path.exists():
        raise FileNotFoundError(
            f"Settings file not found at {path}. Copy config/settings.example.yaml to "
            "config/settings.yaml and edit it for your setup."
        )
    raw = yaml.safe_load(path.read_text()) or {}

    gemini_raw = raw.get("gemini", {})
    gemini = GeminiConfig(
        model=gemini_raw.get("model", GeminiConfig.model),
        voice=gemini_raw.get("voice", GeminiConfig.voice),
        fps=float(gemini_raw.get("fps", GeminiConfig.fps)),
    )

    audio_raw = raw.get("audio", {})
    audio = AudioConfig(
        mic_device=audio_raw.get("mic_device"),
        speaker_device=audio_raw.get("speaker_device"),
        input_sample_rate=int(audio_raw.get("input_sample_rate", AudioConfig.input_sample_rate)),
        output_sample_rate=int(audio_raw.get("output_sample_rate", AudioConfig.output_sample_rate)),
    )

    regions = [
        ScreenRegion(
            name=r["name"],
            x=int(r["x"]),
            y=int(r["y"]),
            width=int(r["width"]),
            height=int(r["height"]),
            description=r.get("description", ""),
        )
        for r in raw.get("regions", [])
    ]
    if not regions:
        raise ValueError("settings.yaml must define at least one entry under 'regions'.")

    chat_raw = raw.get("chat", {})
    twitch_raw = chat_raw.get("twitch", {})
    youtube_raw = chat_raw.get("youtube", {})
    chat = ChatConfig(
        twitch=TwitchConfig(
            enabled=bool(twitch_raw.get("enabled", False)),
            channel=twitch_raw.get("channel", ""),
        ),
        youtube=YoutubeConfig(
            enabled=bool(youtube_raw.get("enabled", False)),
            video_id=youtube_raw.get("video_id", ""),
        ),
        inject_interval_seconds=float(
            chat_raw.get("inject_interval_seconds", ChatConfig.inject_interval_seconds)
        ),
        max_messages_per_injection=int(
            chat_raw.get("max_messages_per_injection", ChatConfig.max_messages_per_injection)
        ),
    )

    topics_raw = raw.get("topics", {})
    topics = TopicsConfig(
        idle_seconds_before_topic=float(
            topics_raw.get("idle_seconds_before_topic", TopicsConfig.idle_seconds_before_topic)
        ),
        file=topics_raw.get("file", TopicsConfig.file),
    )

    personality_file = raw.get("personality", {}).get("file", "config/personalities/default.yaml")

    return Settings(
        gemini=gemini,
        audio=audio,
        regions=regions,
        chat=chat,
        topics=topics,
        personality_file=personality_file,
    )

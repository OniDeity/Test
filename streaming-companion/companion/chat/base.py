"""Shared types for chat sources."""

from __future__ import annotations

import dataclasses
import time


@dataclasses.dataclass
class ChatMessage:
    source: str  # "twitch" | "youtube"
    username: str
    text: str
    timestamp: float = dataclasses.field(default_factory=time.time)

    def label(self) -> str:
        return f"[{self.source.upper()} CHAT] {self.username}: {self.text}"

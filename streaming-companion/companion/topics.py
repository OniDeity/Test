"""Tracks conversational idle time and decides when to nudge the companion toward a new topic."""

from __future__ import annotations

import dataclasses
import time
from pathlib import Path
from typing import Callable, Optional, Union

import yaml


@dataclasses.dataclass
class TopicBank:
    topics: list[str]
    cooldown_seconds: float = 300.0


def load_topics(path: Union[str, Path]) -> TopicBank:
    path = Path(path)
    if not path.exists():
        return TopicBank(topics=[])
    raw = yaml.safe_load(path.read_text()) or {}
    return TopicBank(
        topics=list(raw.get("topics", [])),
        cooldown_seconds=float(raw.get("cooldown_seconds", 300.0)),
    )


class IdleTopicScheduler:
    """Decides when the stream has gone quiet long enough to inject a fresh topic.

    Activity (the streamer speaking, a chat message being injected, or the companion
    itself speaking) resets the idle clock. Topics are also rate-limited by the topic
    bank's cooldown so the companion doesn't fire off a new idea every time it checks.
    """

    def __init__(
        self,
        bank: TopicBank,
        idle_seconds_before_topic: float,
        now_fn: Callable[[], float] = time.monotonic,
    ):
        self._bank = bank
        self._idle_threshold = idle_seconds_before_topic
        self._now = now_fn
        self._last_activity = self._now()
        self._last_topic_at: Optional[float] = None
        self._next_index = 0

    def mark_activity(self) -> None:
        self._last_activity = self._now()

    def due(self) -> bool:
        if not self._bank.topics:
            return False
        if self._now() - self._last_activity < self._idle_threshold:
            return False
        if self._last_topic_at is not None:
            if self._now() - self._last_topic_at < self._bank.cooldown_seconds:
                return False
        return True

    def next_topic(self) -> Optional[str]:
        if not self._bank.topics:
            return None
        topic = self._bank.topics[self._next_index % len(self._bank.topics)]
        self._next_index += 1
        self._last_topic_at = self._now()
        self.mark_activity()
        return topic

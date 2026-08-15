"""Merges messages from multiple chat sources into one queue and batches them for injection."""

from __future__ import annotations

import asyncio
from typing import AsyncIterator, Optional

from .base import ChatMessage


class ChatAggregator:
    def __init__(self, max_queue: int = 200):
        self._queue: "asyncio.Queue[ChatMessage]" = asyncio.Queue(maxsize=max_queue)
        self._tasks: list[asyncio.Task] = []

    def add_source(self, messages: AsyncIterator[ChatMessage]) -> None:
        self._tasks.append(asyncio.create_task(self._pump(messages)))

    async def _pump(self, messages: AsyncIterator[ChatMessage]) -> None:
        async for message in messages:
            if self._queue.full():
                self._queue.get_nowait()
            await self._queue.put(message)

    async def drain(self, max_messages: int) -> list[ChatMessage]:
        drained: list[ChatMessage] = []
        while len(drained) < max_messages:
            try:
                drained.append(self._queue.get_nowait())
            except asyncio.QueueEmpty:
                break
        return drained

    @staticmethod
    def format_injection(messages: list[ChatMessage]) -> Optional[str]:
        if not messages:
            return None
        return "\n".join(message.label() for message in messages)

    async def aclose(self) -> None:
        for task in self._tasks:
            task.cancel()
        if self._tasks:
            await asyncio.gather(*self._tasks, return_exceptions=True)

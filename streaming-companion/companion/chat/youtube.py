"""Polls the YouTube Live Chat API for new messages on an active livestream."""

from __future__ import annotations

import asyncio
from typing import AsyncIterator, Optional

from .base import ChatMessage

API_BASE = "https://www.googleapis.com/youtube/v3"


class YoutubeChat:
    def __init__(self, api_key: str, video_id: str):
        self.api_key = api_key
        self.video_id = video_id

    async def _resolve_live_chat_id(self, client) -> str:
        resp = await client.get(
            f"{API_BASE}/videos",
            params={"part": "liveStreamingDetails", "id": self.video_id, "key": self.api_key},
        )
        resp.raise_for_status()
        items = resp.json().get("items", [])
        if not items:
            raise RuntimeError(f"No video found for id={self.video_id}")
        live_chat_id = items[0].get("liveStreamingDetails", {}).get("activeLiveChatId")
        if not live_chat_id:
            raise RuntimeError(
                f"Video {self.video_id} has no active live chat - is it live right now?"
            )
        return live_chat_id

    async def messages(self) -> AsyncIterator[ChatMessage]:
        import httpx  # imported lazily: only needed at runtime, not for unit tests

        async with httpx.AsyncClient(timeout=15) as client:
            live_chat_id = await self._resolve_live_chat_id(client)
            page_token: Optional[str] = None
            while True:
                params = {
                    "liveChatId": live_chat_id,
                    "part": "snippet,authorDetails",
                    "key": self.api_key,
                }
                if page_token:
                    params["pageToken"] = page_token
                resp = await client.get(f"{API_BASE}/liveChat/messages", params=params)
                resp.raise_for_status()
                data = resp.json()
                for item in data.get("items", []):
                    snippet = item.get("snippet", {})
                    author = item.get("authorDetails", {})
                    text = snippet.get("displayMessage", "")
                    if not text:
                        continue
                    yield ChatMessage(
                        source="youtube",
                        username=author.get("displayName", "viewer"),
                        text=text,
                    )
                page_token = data.get("nextPageToken")
                poll_ms = data.get("pollingIntervalMillis", 5000)
                await asyncio.sleep(max(poll_ms, 2000) / 1000)

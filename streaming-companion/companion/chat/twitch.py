"""Anonymous, read-only Twitch chat via IRC-over-websocket.

No OAuth token is required to read chat: Twitch allows anonymous "justinfan*"
connections for read-only access.
"""

from __future__ import annotations

import random
import re
from typing import AsyncIterator, Optional

from .base import ChatMessage

_PRIVMSG_RE = re.compile(r"^:(?P<user>[^!]+)![^ ]+ PRIVMSG #(?P<channel>[^ ]+) :(?P<text>.*)$")

TWITCH_IRC_WS_URL = "wss://irc-ws.chat.twitch.tv:443"


def parse_privmsg(line: str) -> Optional[tuple[str, str, str]]:
    """Parses a raw Twitch IRC line into (username, channel, text) if it's a chat PRIVMSG."""
    line = line.strip("\r\n")
    if " PRIVMSG " not in line:
        return None
    match = _PRIVMSG_RE.match(line)
    if not match:
        return None
    return match.group("user"), match.group("channel"), match.group("text")


class TwitchChat:
    def __init__(self, channel: str):
        self.channel = channel.lstrip("#").lower()

    async def messages(self) -> AsyncIterator[ChatMessage]:
        import websockets  # imported lazily: only needed at runtime, not for unit tests

        nick = f"justinfan{random.randint(10000, 99999)}"
        async with websockets.connect(TWITCH_IRC_WS_URL) as ws:
            await ws.send(f"NICK {nick}")
            await ws.send(f"JOIN #{self.channel}")
            async for raw in ws:
                for line in raw.splitlines():
                    if line.startswith("PING"):
                        await ws.send("PONG :tmi.twitch.tv")
                        continue
                    parsed = parse_privmsg(line)
                    if parsed is None:
                        continue
                    username, _channel, text = parsed
                    yield ChatMessage(source="twitch", username=username, text=text)

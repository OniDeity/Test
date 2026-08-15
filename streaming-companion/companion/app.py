"""CLI entry point: `python -m companion.app`"""

from __future__ import annotations

import argparse
import asyncio
import logging

from .config import load_settings
from .gemini_session import CompanionSession
from .personality import load_personality
from .topics import load_topics


def _print_transcript(who: str, text: str) -> None:
    print(f"{who}: {text}")


def main() -> None:
    parser = argparse.ArgumentParser(description="AI streaming companion powered by the Gemini Live API")
    parser.add_argument("--settings", default="config/settings.yaml", help="Path to settings.yaml")
    parser.add_argument("--env", default=".env", help="Path to .env with API keys")
    parser.add_argument(
        "-t",
        "--test-mode",
        action="store_true",
        help=(
            "Offline/solo testing mode: skip Twitch/YouTube chat and topic nudges, so the "
            "companion just reacts to your screen and voice. No chat or topics config needed."
        ),
    )
    parser.add_argument("-v", "--verbose", action="store_true")
    args = parser.parse_args()

    logging.basicConfig(level=logging.DEBUG if args.verbose else logging.INFO, format="%(message)s")

    settings = load_settings(args.settings, args.env)
    personality = load_personality(settings.personality_file)
    topic_bank = load_topics(settings.topics.file)

    session = CompanionSession(
        settings,
        personality,
        topic_bank,
        on_transcript=_print_transcript,
        test_mode=args.test_mode,
    )

    try:
        asyncio.run(session.run())
    except KeyboardInterrupt:
        session.stop()


if __name__ == "__main__":
    main()

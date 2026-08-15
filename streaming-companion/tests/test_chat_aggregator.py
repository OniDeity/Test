import asyncio

from companion.chat.aggregator import ChatAggregator
from companion.chat.base import ChatMessage


async def _gen(messages):
    for message in messages:
        yield message
        await asyncio.sleep(0)


def test_aggregator_merges_and_drains_messages():
    async def scenario():
        aggregator = ChatAggregator()
        messages = [
            ChatMessage(source="twitch", username="a", text="hi"),
            ChatMessage(source="youtube", username="b", text="yo"),
        ]
        aggregator.add_source(_gen(messages))
        # give the pump task a chance to run
        for _ in range(5):
            await asyncio.sleep(0)

        drained = await aggregator.drain(10)
        await aggregator.aclose()
        return drained

    drained = asyncio.run(scenario())
    assert {m.username for m in drained} == {"a", "b"}


def test_drain_respects_max_messages():
    async def scenario():
        aggregator = ChatAggregator()
        messages = [ChatMessage(source="twitch", username=str(i), text="x") for i in range(5)]
        aggregator.add_source(_gen(messages))
        for _ in range(5):
            await asyncio.sleep(0)
        drained = await aggregator.drain(2)
        await aggregator.aclose()
        return drained

    drained = asyncio.run(scenario())
    assert len(drained) == 2


def test_format_injection_labels_messages():
    messages = [
        ChatMessage(source="twitch", username="alice", text="hello"),
        ChatMessage(source="youtube", username="bob", text="hi there"),
    ]
    text = ChatAggregator.format_injection(messages)
    assert text == "[TWITCH CHAT] alice: hello\n[YOUTUBE CHAT] bob: hi there"


def test_format_injection_empty_returns_none():
    assert ChatAggregator.format_injection([]) is None

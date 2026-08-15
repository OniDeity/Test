from companion.topics import IdleTopicScheduler, TopicBank, load_topics


class FakeClock:
    def __init__(self, start: float = 0.0):
        self.t = start

    def __call__(self) -> float:
        return self.t

    def advance(self, seconds: float) -> None:
        self.t += seconds


def test_not_due_before_idle_threshold():
    clock = FakeClock()
    bank = TopicBank(topics=["a", "b"], cooldown_seconds=100)
    scheduler = IdleTopicScheduler(bank, idle_seconds_before_topic=30, now_fn=clock)

    clock.advance(10)
    assert scheduler.due() is False


def test_due_after_idle_threshold():
    clock = FakeClock()
    bank = TopicBank(topics=["a", "b"], cooldown_seconds=100)
    scheduler = IdleTopicScheduler(bank, idle_seconds_before_topic=30, now_fn=clock)

    clock.advance(31)
    assert scheduler.due() is True


def test_activity_resets_idle_clock():
    clock = FakeClock()
    bank = TopicBank(topics=["a", "b"], cooldown_seconds=100)
    scheduler = IdleTopicScheduler(bank, idle_seconds_before_topic=30, now_fn=clock)

    clock.advance(20)
    scheduler.mark_activity()
    clock.advance(20)
    assert scheduler.due() is False


def test_topics_cycle_in_order_and_respect_cooldown():
    clock = FakeClock()
    bank = TopicBank(topics=["a", "b"], cooldown_seconds=50)
    scheduler = IdleTopicScheduler(bank, idle_seconds_before_topic=10, now_fn=clock)

    clock.advance(11)
    assert scheduler.next_topic() == "a"

    # Right after firing, still within cooldown even if idle again.
    clock.advance(11)
    assert scheduler.due() is False

    clock.advance(50)
    assert scheduler.due() is True
    assert scheduler.next_topic() == "b"

    clock.advance(61)
    assert scheduler.next_topic() == "a"  # cycles back around


def test_due_false_with_no_topics():
    clock = FakeClock()
    bank = TopicBank(topics=[])
    scheduler = IdleTopicScheduler(bank, idle_seconds_before_topic=1, now_fn=clock)
    clock.advance(100)
    assert scheduler.due() is False
    assert scheduler.next_topic() is None


def test_load_topics_missing_file_returns_empty_bank(tmp_path):
    bank = load_topics(tmp_path / "missing.yaml")
    assert bank.topics == []


def test_load_topics_parses_file(tmp_path):
    path = tmp_path / "topics.yaml"
    path.write_text("cooldown_seconds: 42\ntopics:\n  - one\n  - two\n")
    bank = load_topics(path)
    assert bank.topics == ["one", "two"]
    assert bank.cooldown_seconds == 42

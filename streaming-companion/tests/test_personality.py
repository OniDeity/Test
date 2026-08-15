from companion.personality import Personality, build_system_prompt, load_personality

PERSONALITY_YAML = """
name: "Nova"
tagline: "Test tagline"
voice: Puck
tone: "playful"
traits:
  - curious
  - funny
speaking_style: "Speak briefly."
boundaries:
  - "Don't be rude."
backstory: "A test companion."
"""


def test_load_personality(tmp_path):
    path = tmp_path / "personality.yaml"
    path.write_text(PERSONALITY_YAML)

    personality = load_personality(path)

    assert personality.name == "Nova"
    assert personality.voice == "Puck"
    assert personality.traits == ["curious", "funny"]
    assert personality.boundaries == ["Don't be rude."]


def test_load_personality_missing_file_raises(tmp_path):
    import pytest

    with pytest.raises(FileNotFoundError):
        load_personality(tmp_path / "missing.yaml")


def test_build_system_prompt_includes_key_fields():
    personality = Personality(
        name="Nova",
        tagline="A tagline",
        voice="Puck",
        tone="playful",
        traits=["curious", "funny"],
        speaking_style="Speak briefly.",
        boundaries=["Don't be rude."],
        backstory="A test companion.",
    )

    prompt = build_system_prompt(personality, ["main_monitor", "discord_call"])

    assert "Nova" in prompt
    assert "curious, funny" in prompt
    assert "main_monitor, discord_call" in prompt
    assert "[TWITCH CHAT]" in prompt
    assert "[TOPIC IDEA]" in prompt
    assert "Don't be rude." in prompt


def test_build_system_prompt_handles_no_traits_or_boundaries():
    personality = Personality(name="Nova")
    prompt = build_system_prompt(personality, [])
    assert "friendly, curious" in prompt
    assert "Keep it friendly and on-topic." in prompt

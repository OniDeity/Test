import pytest

from companion.config import load_settings

SETTINGS_YAML = """
gemini:
  model: test-model
  voice: Kore
  fps: 2

audio:
  input_sample_rate: 16000
  output_sample_rate: 24000

regions:
  - name: main_monitor
    x: 0
    y: 0
    width: 1920
    height: 1080
    description: "primary"

chat:
  twitch:
    enabled: true
    channel: "#SomeChannel"
  youtube:
    enabled: false
    video_id: ""
  inject_interval_seconds: 5
  max_messages_per_injection: 3

topics:
  idle_seconds_before_topic: 60
  file: config/topics.example.yaml

personality:
  file: config/personalities/default.yaml
"""


def test_load_settings_parses_all_sections(tmp_path):
    settings_path = tmp_path / "settings.yaml"
    settings_path.write_text(SETTINGS_YAML)
    env_path = tmp_path / ".env"
    env_path.write_text("GEMINI_API_KEY=test-key\n")

    settings = load_settings(settings_path, env_path)

    assert settings.gemini.model == "test-model"
    assert settings.gemini.voice == "Kore"
    assert settings.gemini.fps == 2

    assert len(settings.regions) == 1
    region = settings.regions[0]
    assert region.name == "main_monitor"
    assert (region.x, region.y, region.width, region.height) == (0, 0, 1920, 1080)

    assert settings.chat.twitch.enabled is True
    assert settings.chat.twitch.channel == "#SomeChannel"
    assert settings.chat.inject_interval_seconds == 5
    assert settings.chat.max_messages_per_injection == 3

    assert settings.topics.idle_seconds_before_topic == 60
    assert settings.personality_file == "config/personalities/default.yaml"

    assert settings.gemini_api_key == "test-key"


def test_load_settings_missing_file_raises(tmp_path):
    with pytest.raises(FileNotFoundError):
        load_settings(tmp_path / "does_not_exist.yaml", tmp_path / ".env")


def test_load_settings_requires_at_least_one_region(tmp_path):
    settings_path = tmp_path / "settings.yaml"
    settings_path.write_text("regions: []\n")
    with pytest.raises(ValueError):
        load_settings(settings_path, tmp_path / ".env")


def test_gemini_api_key_missing_raises(tmp_path):
    settings_path = tmp_path / "settings.yaml"
    settings_path.write_text(SETTINGS_YAML)
    env_path = tmp_path / ".env"
    env_path.write_text("")

    settings = load_settings(settings_path, env_path)
    import os

    os.environ.pop("GEMINI_API_KEY", None)
    with pytest.raises(RuntimeError):
        _ = settings.gemini_api_key

# AI Streaming Companion

A voice-and-vision AI co-host for your stream, built on Google's **Gemini Live API**.
Plug in a Gemini API key and it:

- **Talks with you in real time** (native speech-to-text and text-to-speech, no extra
  TTS/STT services needed).
- **Sees your screen**: any set of regions you define (main monitor, a Discord call
  window, etc.) are captured and streamed to it as live video.
- **Reads chat**: Twitch and/or YouTube Live chat messages are fed in and it can react
  to them naturally.
- **Has a configurable personality**: name, tone, traits, speaking style, and
  boundaries are all defined in a YAML profile.
- **Brings up topics on its own**: when the stream goes quiet for a while, it organically
  steers the conversation toward something from a topic list you configure.

Why the Gemini *Live* API specifically: it's a bidirectional streaming API designed for
exactly this (see Google's "Project Astra" demos) - continuous audio conversation plus a
live video feed, with the model doing turn-taking, interruption handling, speech
recognition, and speech synthesis itself. That's what makes "plug in a key and it works"
realistic instead of you having to wire together separate STT/LLM/TTS services.

## How it sees your Discord call

Rather than a real Discord voice/video bot (which relies on undocumented protocol
internals and is brittle), this treats your Discord window like any other screen
region: point a region at wherever the call is on your screen and the companion sees
it exactly like it sees your game.

## Setup (Windows)

1. **Install Python 3.11+** from python.org (check "Add to PATH" during install).
2. **Get a Gemini API key**: https://aistudio.google.com/apikey
3. Clone/download this folder, then in a terminal:
   ```
   cd streaming-companion
   python -m venv .venv
   .venv\Scripts\activate
   pip install -r requirements.txt
   ```
   Or just double-click `run_windows.bat` - it does this automatically on first run.
4. **Configure secrets**: copy `.env.example` to `.env` and fill in `GEMINI_API_KEY`
   (and `YOUTUBE_API_KEY` if you're using YouTube chat).
5. **Configure settings**: copy `config/settings.example.yaml` to `config/settings.yaml`
   and edit it - see below.
6. **Define your screen regions**:
   ```
   python -m companion.region_picker main_monitor
   python -m companion.region_picker discord_call
   ```
   Each command opens a full-screen overlay - drag a box around the area you want the
   companion to see, and it prints a YAML snippet. Paste the printed entries under
   `regions:` in `config/settings.yaml`.
7. **Route its voice into OBS/Discord** (optional but recommended): install a virtual
   audio cable (e.g. [VB-Audio Virtual Cable](https://vb-audio.com/Cable/), free), set
   `audio.speaker_device` in `settings.yaml` to that cable's name, then add the cable as
   a microphone source in OBS and/or Discord so viewers/your co-players can hear it too.
8. **Run it**:
   ```
   run_windows.bat
   ```
   or, with the venv active:
   ```
   python -m companion.app
   ```
   Console output shows a live transcript of both sides of the conversation.

## Offline/solo test mode

To try the companion out without setting up Twitch/YouTube chat or a topics file first,
run it with `--test-mode` (or `-t`):

```
python -m companion.app --test-mode
```

This still needs your Gemini API key and internet access (it's "offline" in the sense
of no live chat/topic integration, not literally no network) - it just skips connecting
to chat and skips topic nudges, so it's purely reacting to your screen and voice. Handy
for checking your personality profile, mic/speaker setup, and screen regions sound and
look right before going live. Drop `--test-mode` once you're ready to actually stream.

## Configuring personality

Edit `config/personalities/default.yaml` (or create a new file and point
`personality.file` at it in `settings.yaml`). Fields: `name`, `tagline`, `voice`
(any Gemini prebuilt voice, e.g. `Puck`, `Kore`, `Fenrir`, `Charon`, `Aoede`), `tone`,
`traits`, `speaking_style`, `boundaries`, `backstory`. A second example,
`config/personalities/hype_gamer.yaml`, shows a high-energy alternative persona.

## Configuring topics

Edit `config/topics.example.yaml` (or point `topics.file` at your own copy). It's a
plain list of things to organically bring up, plus `cooldown_seconds` so it doesn't
spam ideas back-to-back. `topics.idle_seconds_before_topic` in `settings.yaml` controls
how long the stream needs to be quiet before it reaches for a topic.

## Configuring chat

Twitch chat needs nothing but your channel name (reads chat anonymously/read-only, no
bot account or OAuth token required). YouTube Live chat needs a Google API key with the
YouTube Data API v3 enabled, plus the video ID of your current live broadcast (from the
watch URL) - update that ID each time you go live, or automate it if you always stream
to the same persistent live page.

## Project layout

```
companion/
  config.py          settings.yaml + .env loading
  personality.py      personality profile -> Gemini system prompt
  topics.py            idle-detection + topic cycling
  gemini_session.py    orchestrates the Gemini Live API session
  app.py                CLI entry point
  region_picker.py     drag-select tool for screen regions
  capture/
    screen.py           region capture + compositing into one frame
    audio.py             mic capture / speaker playback
  chat/
    base.py, twitch.py, youtube.py, aggregator.py
config/
  settings.example.yaml, topics.example.yaml, personalities/
tests/                 unit tests for the non-hardware-dependent logic
```

## Limitations / possible next steps

- Discord is treated as a screen region, not a real bot connection - if the call window
  is covered or minimized, the companion won't "see" it.
- No GUI control panel yet - everything is configured via YAML and run from a terminal.
  A tray icon / control window (mute, live transcript, personality switcher, on-the-fly
  topic injection) would be a natural next step.
- The Gemini Live model ID in `settings.example.yaml` may need updating over time as
  Google ships newer Live models - check https://ai.google.dev/gemini-api/docs/live.
- YouTube's live chat video ID currently has to be set manually per broadcast.

## Running tests

```
pip install pytest
pytest
```

Tests cover the pure logic (config/personality loading, idle-topic scheduling, chat
message parsing/aggregation) that doesn't require a mic, screen, or live API key.

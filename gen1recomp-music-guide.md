# gen1recomp — Custom Music Playbook (notes-to-self)

Sourced from [Tutorial-09](https://github.com/bryanthaboi/gen1recomp/wiki/Tutorial-09-Custom-Music) and [Guide-Audio-Authoring](https://github.com/bryanthaboi/gen1recomp/wiki/Guide-Audio-Authoring). Quotes are cited; anything not directly documented is marked **[inferred — verify before relying on it]**.

## 1. Four registries, don't confuse them

- `music` → song definitions
- `sfx` → sound effects
- `cries` → species vocalizations
- `map_songs` → location → song assignment (which song plays where)

## 2. Simplest path: file-based song (OGG/WAV)

```lua
mod.content.music:register("Music_ModTown", {
  file = mod.assets:path("assets/town.ogg"),
  loopFile = mod.assets:path("assets/town_loop.ogg"),
})
```

- `file` plays once as the intro.
- `loopFile` repeats after the intro finishes.
- **A def with only `file` loops that same file** (self-looping) — omit `loopFile` only if the track is meant to loop as a whole from the start, not intro→loop.
- **A missing `loopFile` on a track that isn't meant to fully loop "degrades to the intro alone"** and then stops — this is the #1 pitfall (see table below).
- Registering alone **validates but produces no sound** — a song must also be assigned via `map_songs` (or the `music.select` hook) to actually play anywhere.

## 3. Assign the song to a map

```lua
mod.content.map_songs:override("MODROUTE", "Music_ModTown")
```
Same syntax retargets a **vanilla** map's theme too: `map_songs:override("PALLET_TOWN", "Music_ModTown")`. Note this is `:override()`, not `:register()`/`:patch()` — consistent with `map_songs` being a simple id→song mapping rather than a list/record needing merge semantics.

## 4. Native chiptune authoring (no audio files)

For an authentic Game Boy-style track instead of streamed audio, use `ChipAsm`:

```lua
local ChipAsm = require("src.audio.ChipAsm")

mod.content.music:register("Music_ModChip", ChipAsm.song({
  tempo = 140,
  channels = {
    { program = {
        { label = "top" },
        { notetype = { speed = 12, volume = 15, fade = 2 } },
        { octave = 4 },
        { note = "C", len = 4 },
        { note = "E", len = 4 },
        { note = "G", len = 4 },
        { octave = 5 },
        { note = "C", len = 8 },
        { loop = { count = 0, to = "top" } },  -- count = 0 means infinite
    } },
  },
}))
```

Supported program events: `note`/`len`, `rest`, `octave`, `notetype` (speed/volume/fade), `drum` (channel 4 only), `label`, `call`, `loop`, `vibrato`, `duty`, `tempo`, plus more detailed in the Audio Authoring guide. **Event parameters must match exactly** — assembly errors report the specific channel and event index, so a syntax slip is at least easy to locate.

## 5. Conditional music via the `music.select` hook

For situational music (e.g. a different track while surfing, or in a specific building) that isn't a static per-map assignment:

```lua
mod.hooks:wrap("music.select", function(next, song, ctx)
  if ctx.reason == "map" and ctx.surfing then
    return "Music_ModChip"
  end
  return next(song, ctx)
end)
```

Available `ctx` fields: `reason` (e.g. `"map"`), `mapId`, `mapSong`, `onBike`, `surfing`, `kind`, `battleKind`, `trainerId`. **All music choices pass through this single hook point** — always call `next(song, ctx)` for the fallthrough case so you don't accidentally silence music in every situation you didn't explicitly handle.

## 6. Format requirements & directory layout

- Streamed audio: `.ogg` (Tutorial 09) — Audio Authoring guide additionally lists `.wav` as supported for file-backed audio.
- Intro/loop segments need **clean seams** — no audible pop/gap at the file boundary.
- Reference audio assets via `mod.assets:path(...)`, same convention as image assets.

```
mods/tutorial_09_music/
├── manifest.json
├── main.lua
└── assets/
    ├── town.ogg
    └── town_loop.ogg
```

## 7. Common pitfalls

| Mistake | Consequence | Fix |
|---|---|---|
| Omitting `loopFile` on a track with a distinct intro | Plays intro once, then stops (silence) | Add `loopFile`, or make `file` itself the self-looping full track |
| Invalid/typo'd file path | **Silently skipped** — "logged with mod attribution," no crash | Check logs for the mod-attributed path error if a track isn't playing |
| Registering a new song under a **vanilla song id** | Registry collision (this registry appears to behave like `record` semantics — new content needs a new id) | Use `:override()` to intentionally replace a vanilla song; use a fresh id (e.g. `Music_ModTown`) for original content |
| Forgetting to assign via `map_songs` (or a hook) after registering | Song validates but is never heard | Always pair `music:register` with a `map_songs:override` or a `music.select` hook |
| `music.select` hook not calling `next(...)` on the non-matched branch | Breaks music selection for every other case | Always fall through to `next(song, ctx)` |

## 8. Checkpoint

- New map/route plays the file-based song with a clean intro→loop transition.
- The conditional case (e.g. surfing) triggers the right track.
- All vanilla themes remain intact when the mod is disabled (parity invariant, same as every other content type in this project).

## Sources
- [Tutorial 09 — Custom Music](https://github.com/bryanthaboi/gen1recomp/wiki/Tutorial-09-Custom-Music)
- [Guide — Audio Authoring](https://github.com/bryanthaboi/gen1recomp/wiki/Guide-Audio-Authoring)

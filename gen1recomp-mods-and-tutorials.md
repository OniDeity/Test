# gen1recomp — Mods & Tutorials Reference

Compiled reference for learning to mod with [gen1recomp](https://github.com/bryanthaboi/gen1recomp) (a Pokémon Gen 1 recompilation/modding engine). Sources: the project [wiki](https://github.com/bryanthaboi/gen1recomp/wiki) and the [gen1recomp-mod-index](https://github.com/bryanthaboi/gen1recomp-mod-index) repo. Compiled 2026-08-11.

## Topic Playbooks (this repo)

Working-notes-style guides, each pulling exact schemas/quotes from the wiki plus documented pitfalls, written while building a Celadon City shop mod:

- [`gen1recomp-warps-and-buildings-guide.md`](./gen1recomp-warps-and-buildings-guide.md) — new maps, connections vs. warps, the `destWarp` index cross-referencing, and the `__append` list-patch gotcha
- [`gen1recomp-tiles-and-art-guide.md`](./gen1recomp-tiles-and-art-guide.md) — custom tiles/tilesets, the 4-shade pixel contract, asset transforms
- [`gen1recomp-npc-guide.md`](./gen1recomp-npc-guide.md) — adding NPCs (static vs. dynamic) and dialogue scripts
- [`gen1recomp-shop-guide.md`](./gen1recomp-shop-guide.md) — marts/shop inventory and wiring a clerk NPC to `open_mart`
- [`gen1recomp-music-guide.md`](./gen1recomp-music-guide.md) — custom music, chiptune authoring, conditional music hooks
- [`gen1recomp-species-guide.md`](./gen1recomp-species-guide.md) — registering a new catchable Pokémon species

## Learning Path (Wiki)

Base URL for all wiki pages: `https://github.com/bryanthaboi/gen1recomp/wiki/<page>`

### Start Here
- [Home](https://github.com/bryanthaboi/gen1recomp/wiki/Home)
- [Getting Started](https://github.com/bryanthaboi/gen1recomp/wiki/Getting-Started)
- [Cookbook](https://github.com/bryanthaboi/gen1recomp/wiki/Cookbook)
- [Modkit CLI](https://github.com/bryanthaboi/gen1recomp/wiki/Guide-Modkit)

### Core Concepts
- [Lifecycle](https://github.com/bryanthaboi/gen1recomp/wiki/Concepts-Lifecycle)
- [Registries](https://github.com/bryanthaboi/gen1recomp/wiki/Concepts-Registries)
- [Events and Hooks](https://github.com/bryanthaboi/gen1recomp/wiki/Concepts-Events-And-Hooks)
- [Data Model](https://github.com/bryanthaboi/gen1recomp/wiki/Concepts-Data-Model)
- [Save Model](https://github.com/bryanthaboi/gen1recomp/wiki/Concepts-Save-Model)
- [Compatibility](https://github.com/bryanthaboi/gen1recomp/wiki/Concepts-Compatibility)

### Tutorial Series (12 rungs, do in order)
1. [Tutorials overview](https://github.com/bryanthaboi/gen1recomp/wiki/Tutorials)
2. [01 — Sprite and Text Tweak](https://github.com/bryanthaboi/gen1recomp/wiki/Tutorial-01-Sprite-And-Text-Tweak)
3. [02 — Balance Patch](https://github.com/bryanthaboi/gen1recomp/wiki/Tutorial-02-Balance-Patch)
4. [03 — New Species](https://github.com/bryanthaboi/gen1recomp/wiki/Tutorial-03-New-Species)
5. [04 — New Item and Ball](https://github.com/bryanthaboi/gen1recomp/wiki/Tutorial-04-New-Item)
6. [05 — New Move](https://github.com/bryanthaboi/gen1recomp/wiki/Tutorial-05-New-Move)
7. [06 — NPC and Dialogue](https://github.com/bryanthaboi/gen1recomp/wiki/Tutorial-06-NPC-And-Dialogue)
8. [07 — New Map](https://github.com/bryanthaboi/gen1recomp/wiki/Tutorial-07-New-Map)
9. [08 — Quest](https://github.com/bryanthaboi/gen1recomp/wiki/Tutorial-08-Quest)
10. [09 — Custom Music](https://github.com/bryanthaboi/gen1recomp/wiki/Tutorial-09-Custom-Music)
11. [10 — New Mechanic](https://github.com/bryanthaboi/gen1recomp/wiki/Tutorial-10-New-Mechanic)
12. [11 — Custom UI Screen](https://github.com/bryanthaboi/gen1recomp/wiki/Tutorial-11-Custom-UI-Screen)
13. [12 — Mini Total Conversion](https://github.com/bryanthaboi/gen1recomp/wiki/Tutorial-12-Mini-Total-Conversion)

### Cookbook (task-sized recipes, non-linear)
- [Cookbook index](https://github.com/bryanthaboi/gen1recomp/wiki/Cookbook)
- [Audio](https://github.com/bryanthaboi/gen1recomp/wiki/Cookbook-Audio)
- [Battle and Mechanics](https://github.com/bryanthaboi/gen1recomp/wiki/Cookbook-Battle-And-Mechanics)
- [Items and Balls](https://github.com/bryanthaboi/gen1recomp/wiki/Cookbook-Items-And-Balls)
- [Species and Moves](https://github.com/bryanthaboi/gen1recomp/wiki/Cookbook-Species-And-Moves)
- [Tweaks](https://github.com/bryanthaboi/gen1recomp/wiki/Cookbook-Tweaks)
- [UI and Infrastructure](https://github.com/bryanthaboi/gen1recomp/wiki/Cookbook-UI-And-Infrastructure)
- [World and Scripting](https://github.com/bryanthaboi/gen1recomp/wiki/Cookbook-World-And-Scripting)

### Reference Documentation
- [Registries](https://github.com/bryanthaboi/gen1recomp/wiki/Reference-Registries)
- [Events](https://github.com/bryanthaboi/gen1recomp/wiki/Reference-Events)
- [Hooks](https://github.com/bryanthaboi/gen1recomp/wiki/Reference-Hooks)
- [The Mod Object](https://github.com/bryanthaboi/gen1recomp/wiki/Reference-Mod-Object)
- [Manifest](https://github.com/bryanthaboi/gen1recomp/wiki/Reference-Manifest)
- [Script Commands](https://github.com/bryanthaboi/gen1recomp/wiki/Reference-Commands)

### Guides
- [Art Pipeline](https://github.com/bryanthaboi/gen1recomp/wiki/Guide-Art-Pipeline)
- [Audio Authoring](https://github.com/bryanthaboi/gen1recomp/wiki/Guide-Audio-Authoring)
- [Total Conversions](https://github.com/bryanthaboi/gen1recomp/wiki/Guide-Total-Conversions)
- [Link Compatibility](https://github.com/bryanthaboi/gen1recomp/wiki/Guide-Link-Compatibility)
- [Publishing a Mod](https://github.com/bryanthaboi/gen1recomp/wiki/Guide-Publishing)
- [Preparing for Gen 2](https://github.com/bryanthaboi/gen1recomp/wiki/Guide-Preparing-Your-Mod-For-Gen-2)
- [Translations](https://github.com/bryanthaboi/gen1recomp/wiki/Guide-Translations)
- [Style Guide](https://github.com/bryanthaboi/gen1recomp/wiki/Guide-Style)

### Player Resources
- [Link Play](https://github.com/bryanthaboi/gen1recomp/wiki/Guide-Link-Play)
- [Save Editor](https://github.com/bryanthaboi/gen1recomp/wiki/Guide-Save-Editor)
- [Developer Setup](https://github.com/bryanthaboi/gen1recomp/wiki/Guide-Developer-Setup)

---

## Mod Index (Working Examples)

Source repo: [gen1recomp-mod-index](https://github.com/bryanthaboi/gen1recomp-mod-index) — metadata-only index (no mod code/assets bundled); each mod's own repo/release holds the actual code. Submission helper / browsable site: `bryanthaboi.github.io/gen1recomp-mod-index`.

Each mod lives at `https://github.com/bryanthaboi/gen1recomp-mod-index/tree/main/mods/<entry>` and includes a `meta.json` + `description.md` with the real source/download link. Categories used by the index: `GAMEPLAY, CONTENT, BALANCE, ART, AUDIO, UI, QOL, TRANSLATION, TOTAL_CONVERSION, LIBRARY, TOOL, OTHER`.

Good starting points for learning from working code:
- **`ShaneMcGovernIE@*`** — prolific author, many small focused QOL mods (great for reading simple, single-purpose examples): `exp_share`, `critical_capture`, `relearn_moves`, `qol_toggles`, `useful_bag`, `useful_dex`, `useful_marts`, `useful_move_info`, `double_battles`, `free_fly`, `wild_skies`, `trainer_rematch`, `surround_audio`, `widescreen_battle_intro`, `mods_hotkeys`, `potato_voxel`, `yellow_legacy_changes`
- **`eduardocalafell@*`** — UI/QOL examples: `bag_sort`, `better_battle_ui`, `bike_anywhere`, `damage_numbers`, `minimap`, `modern_dialog`, `player_sprite_flip`, `auto_battle`, `fakemons`, `gen2_dex`
- **`masterwebx@*`** — engine/system-level examples: `CONTROLLER_RUMBLE`, `MULTI_SAVE_SLOTS`, `MUSIC_PLAYER`, `RUN_MODE`, `SHINY_POKEMON`, `MOVE_MATCHUP`, `HEAL_ANYWHERE`, `ACCESS_PC_ANYWHERE`, `FOLLOWERS_EX`
- **`bryanthaboi@*`** — mods from the tool's own author, worth studying as reference implementations: `nuzlocke`, `bookers_heaven`, `translation-ja-hrkt`, `versaovermelha`

### Full list of mod entries (author@id)

| Author | Mod ID |
|---|---|
| AntoniMan31 | day_night_cycle |
| AntoniMan31 | trad-fr-extended |
| ArmstrongThomas | gen1_modern_ui |
| AshJam | silphnet |
| BartInTheField | translation-nl |
| BrenoBertucci | TERRARIUM |
| Campo | kanto_dynamic_weather |
| Campo | pokemon_red_voice_acting |
| DarioMelo | Music_FRLG |
| DarioMelo | Music_HGSS |
| DarioMelo | Music_LGPE |
| DavidSchuchert | VOXEL_DEX |
| Gamecorner_033 | PokePCFollowers_VoxelMerge |
| Gamecorner_033 | gen1online |
| Gamecorner_033 | overworld_encounters |
| MFRTechConsult | DRAMATIC_SKY_RIDE |
| MadeinTaly | gen3_box |
| MadeinTaly | gen3_dex |
| MadeinTaly | groovy_palette |
| MadeinTaly | modern_kanto |
| MadeinTaly | running_shoes |
| MisterMiracle | snag_quest |
| MrJoufflu | translation-qc |
| MrKrisSatan | leaf_avatar |
| MraYT | kanto_dive |
| MyFriendDevBR | whos_that_trainer |
| Razor1993 | FLYING_OVERHAUL |
| Roxas2712 | deutsch-blau |
| Roxas2712 | deutsch-gelb |
| Roxas2712 | deutsch |
| Satori7Kensho | shiny_starters_gifts |
| Sebarosu | recomp-spanish |
| Sedatb23 | recomp_cartographer |
| ShaneHudson | dev-hook-inspector |
| ShaneHudson | double_battles |
| ShaneHudson | free_fly |
| ShaneHudson | wild_skies |
| ShaneMcGovernIE | critical_capture |
| ShaneMcGovernIE | exp_share |
| ShaneMcGovernIE | mods_hotkeys |
| ShaneMcGovernIE | potato_voxel |
| ShaneMcGovernIE | qol_toggles |
| ShaneMcGovernIE | relearn_moves |
| ShaneMcGovernIE | surround_audio |
| ShaneMcGovernIE | trainer_rematch |
| ShaneMcGovernIE | useful_bag |
| ShaneMcGovernIE | useful_dex |
| ShaneMcGovernIE | useful_marts |
| ShaneMcGovernIE | useful_move_info |
| ShaneMcGovernIE | widescreen_battle_intro |
| ShaneMcGovernIE | yellow_legacy_changes |
| TheRhysWyrill | instant_heal_pc_rest |
| YoDrehDenSwagAuf | overworld_wild_spawns |
| YukitaMayako | simple_girl |
| Yukitty | oak_brief |
| absol89 | BATTLE_ART_VOXEL_FORK |
| adrian | gen1_kr |
| alamops | rby_mmo |
| allanrmartins | SWITCH_ADVISOR |
| bryanthaboi | bookers_heaven |
| bryanthaboi | nuzlocke |
| bryanthaboi | translation-ja-hrkt |
| bryanthaboi | versaovermelha |
| ciddmandude | pokemon_randomizer |
| ddagent | npc_bubbles |
| eduardocalafell | auto_battle |
| eduardocalafell | bag_sort |
| eduardocalafell | better_battle_ui |
| eduardocalafell | bike_anywhere |
| eduardocalafell | damage_numbers |
| eduardocalafell | fakemons |
| eduardocalafell | gen2_dex |
| eduardocalafell | minimap |
| eduardocalafell | modern_dialog |
| eduardocalafell | player_sprite_flip |
| hdbreaker | cheat_engine |
| hdbreaker | intro_bypass |
| jherediagu | traduccion_es |
| liminal | no_exp_challenge |
| masterwebx | ACCESS_PC_ANYWHERE |
| masterwebx | CONTROLLER_RUMBLE |
| masterwebx | FOLLOWERS_EX |
| masterwebx | HEAL_ANYWHERE |
| masterwebx | MOVE_MATCHUP |
| masterwebx | MULTI_SAVE_SLOTS |
| masterwebx | MUSIC_PLAYER |
| masterwebx | RUN_MODE |
| masterwebx | SHINY_POKEMON |
| menyas | unique_menu_icons |
| mresnick67 | pokewalker |
| redelpedron | kanto_companion_lite |
| tebwritescode | gen1mmo |
| unxpected-uxp | quality_of_life |

> Note: the index reported 130+ total entries; this table captures what the file browser rendered (93). Re-check `https://github.com/bryanthaboi/gen1recomp-mod-index/tree/main/mods` for any added since compiling, and the source repo listed in each mod's own `meta.json` for the actual downloadable code.

---

## Suggested Order of Attack

1. Read **Getting Started** + **Developer Setup**, get the modkit building.
2. Work through **Tutorials 01–05** (sprite/text → balance → species → items → moves) — these establish the core registry/data-model patterns.
3. Skim **Concepts** (Lifecycle, Registries, Events and Hooks) once the basics feel repetitive — this is where the "why" behind the tutorial code lives.
4. Pull 2-3 small mods from `ShaneMcGovernIE@*` or `eduardocalafell@*` and read their source alongside the matching **Cookbook** page for that feature.
5. Once comfortable, use **Cookbook** pages as an on-demand reference instead of reading linearly, and use **Reference** pages (Registries/Events/Hooks/Manifest/Commands) as API lookup.
6. Before publishing anything of your own, read **Guide-Publishing** and **Guide-Style**.

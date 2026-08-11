# gen1recomp — Custom Pokémon Species Playbook (notes-to-self)

Sourced from [Tutorial-03](https://github.com/bryanthaboi/gen1recomp/wiki/Tutorial-03-New-Species). Quotes are cited; anything not directly documented is marked **[inferred — verify before relying on it]**.

## 1. Full worked example (a complete, catchable species)

```lua
return function(mod)
  mod.content.pokemon:register("MODMON", {
    id = "MODMON",
    name = "MODMON",
    dex = 152,
    types = { "NORMAL" },
    baseStats = { hp = 60, attack = 70, defense = 55, speed = 90, special = 65 },
    catchRate = 120,
    baseExp = 100,
    growthRate = "MEDIUM_FAST",
    level1Moves = { "TACKLE", "GROWL" },
    learnset = {
      { level = 12, move = "QUICK_ATTACK" },
      { level = 20, move = "SLASH" },
    },
    evolutions = {},
    spriteFront = mod.assets:path("assets/front.png"),
    spriteBack = mod.assets:path("assets/back.png"),
    frontSize = 5,
    dexEntry = { kind = "TUTORIAL", heightFt = 2, heightIn = 4,
                 weight = 30.0, text = "Added by a mod.\\nIt is very new." },
  })
end
```

**`register` (unlike `patch`) must carry every required field** — the registration is validated for completeness, not just correctness of what's present. Required, per the tutorial: `id`, `name`, `dex`, `types`, `baseStats`, `catchRate`, `baseExp`, `growthRate`, `level1Moves`, `learnset`, `evolutions`, `spriteFront`, `spriteBack`, `frontSize`. Optional (per Concepts-Data-Model): `index`, `tmhm`, `dexEntry`, `icon`, `cry`, `palette`, `trueColor`.

Valid `growthRate` values: `MEDIUM_FAST`, `MEDIUM_SLOW`, `FAST`, `SLOW`, `SLIGHTLY_FAST`, `SLIGHTLY_SLOW`.

## 2. Dex numbering has no hard cap

The Pokédex **scales automatically based on the highest registered `dex` value** — there's no hard-coded 151 limit to work around. Just don't reuse an existing dex number (see pitfalls).

## 3. Party icon (separate registry from the species itself)

```lua
mod.content.icons:register("MODMON", {
  image = mod.assets:path("assets/icon.png"),
})
```
Without this, the species falls back to a generic icon in the party menu — not an error, just visually generic.

## 4. Cry (also separate; three authoring paths)

```lua
mod.content.cries:register("MODMON", {
  file = mod.assets:path("assets/cry.wav"),
})
```
Alternatives: derive from a vanilla species' cry (`{ base = "PIKACHU", pitch = 200 }`), or author one with the same `ChipAsm` chiptune system used for music (see `gen1recomp-music-guide.md`). **A species with no cry degrades to silence rather than crashing** — skip only if that's actually acceptable, since it reads as a bug to players otherwise.

## 5. Making it catchable — add to a wild encounter table

```lua
mod.content.encounters:patch("ROUTE_1", {
  grass = { slots = { __prepend = { { species = "MODMON", level = 5 } } } },
})
```
Uses `__prepend` here to put it in the most-common (front) slot — same `__append`/`__prepend` list-patch mechanism documented for the `maps` registry (`record` semantics: a bare list assignment would replace the whole encounter table for that map/method, wiping out the other vanilla species there). **Always wrap with `__append`/`__prepend`, never assign a bare list, when patching an existing encounter table** — same rule as `warps`/`objects` on `maps`.

## 6. Asset requirements & layout

```
mods/tutorial_03_species/
├── manifest.json
├── main.lua
└── assets/
    ├── front.png    (4-shade grayscale, white = transparent)
    ├── back.png
    ├── icon.png
    └── cry.wav
```
Sprites follow the same **4-shade grayscale contract** as tiles (see `gen1recomp-tiles-and-art-guide.md`): white 255 / light 170 / dark 85 / black 0, with white acting as transparency for sprites specifically.

## 7. Common pitfalls

| Mistake | Consequence |
|---|---|
| Omitting `frontSize` | Battle layout can't size the sprite correctly (valid range 1–7 tiles) — validation requires it, so this fails fast rather than looking wrong at runtime |
| Duplicate `dex` number | Two species cannot occupy the same Pokédex slot |
| Skipping the cry | Silence in place of a cry — not a crash, but a visible gap |
| Wrong sprite art format | Must be 4-shade grayscale; anything else breaks the palette system |
| Bare-list patch on an encounter table | Wipes the other species already encounterable there — use `__append`/`__prepend` |

## 8. Save compatibility

If a mod adding a species is later disabled, any caught individuals of that species **move to a "LOST" box with a report**, and are restored automatically if the mod is re-enabled. Good to know before panicking mid-testing if a mod gets toggled off with modded Pokémon already caught — it's expected, recoverable behavior, not data loss.

## Sources
- [Tutorial 03 — New Species](https://github.com/bryanthaboi/gen1recomp/wiki/Tutorial-03-New-Species)
- [Concepts — Data Model](https://github.com/bryanthaboi/gen1recomp/wiki/Concepts-Data-Model) (optional field list)
- [Cookbook — Species and Moves](https://github.com/bryanthaboi/gen1recomp/wiki/Cookbook-Species-And-Moves) (not yet pulled in detail — check for additional recipes, e.g. evolutions, TM/HM compatibility, before finalizing a species with those features)

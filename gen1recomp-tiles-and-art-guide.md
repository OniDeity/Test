# gen1recomp — Custom Tiles & Art Playbook (notes-to-self)

Sourced from [Guide-Art-Pipeline](https://github.com/bryanthaboi/gen1recomp/wiki/Guide-Art-Pipeline) and [Tutorial-01](https://github.com/bryanthaboi/gen1recomp/wiki/Tutorial-01-Sprite-And-Text-Tweak). Quotes are cited; anything not directly documented is marked **[inferred — verify before relying on it]**.

## 1. The core philosophy: ship recipes, not pixels

The project **"ships no ROM data."** All vanilla-derived content decodes on the player's own verified ROM at runtime; mods must not distribute extracted/derived pixels.

- `modkit pack` **blocks cache-derived bytes** — trying to ship a raw extracted PNG will fail packaging.
- The pattern instead is a **transform function** that reads the vanilla asset, manipulates it, and writes the result — the transform (code) ships, not the output (pixels):

```lua
-- transforms.lua
return function(ctx)
  local front = ctx.readImage("battle/front/mew.png")
  ctx.writeImage(ctx.recolor(front, {
    { 255, 255, 255 }, { 255, 170, 200 }, { 180, 60, 120 }, { 40, 0, 20 },
  }), "battle/front/mew.png")
end
```
Wire it up in `manifest.json`: `"assets_transforms": "transforms.lua"`.

- The engine loads the transformed image automatically (it "shadows" the original because the derived path mirrors the cache path) — **no registry call needed** for a same-name reskin.
- Transform output persists in `save/mod-derived/<id>/`, which survives ROM re-imports; **`assets/generated/` gets wiped on re-import**, so never hand-edit files there.
- **Wholly original art** (a brand-new tileset/tile/sprite that isn't a recolor of vanilla) is a different path — reference it directly via `mod.assets:path(...)` in the relevant registry entry (see `tilesets`/`icons`/species sprite fields in the other guides), no transform needed since there's no ROM-derived source to shadow.

## 2. The 4-shade grayscale pixel contract

The importer converts all vanilla art to **"4-shade grayscale PNGs: white 255, light 170, dark 85, black 0."** Palettes recolor these four shades at render time — this is *why* the recolor transform above just remaps 4 RGB tuples instead of touching individual pixels.

- New art (tiles, sprites) should follow the same 4-shade contract so it picks up map/battle palettes automatically.
- To opt out and use full color instead: **set `trueColor = true` on the `tilesets` record** (or the equivalent record for the asset type).

## 3. Tileset dimensions — exact pixel grid matters

- Existing vanilla tilesets: **19 sheets covering 24 tileset IDs.**
- Most sheets are **128×48 px** (96 tiles); a few are **128×40 px** (80 tiles).
- Each tile is **8×8 px**.
- **"An upscaled sheet does not render upscaled — it renders as the wrong tiles."** Replacement art must match the exact pixel dimensions of what it replaces.
- To ship higher-resolution art, **register a separate tileset record** — don't just drop a bigger sheet in as an override.
- If you do change a sheet's tile count/layout, update `tilesPerRow` to match, or tiles will read from the wrong offsets (listed explicitly as a common mistake).

## 4. Art changes do NOT touch tile behavior

**"Block definitions, collision, door and warp tiles are untouched"** by art/texture changes. A texture-pack-style mod only changes what a tile *looks like*; walkability, warp-triggering, etc. all live on the **map's `blocks` field** (see `gen1recomp-warps-and-buildings-guide.md`). Changing tile *behavior* is a map mod, not a texture mod — don't expect a reskin to change collision, and don't expect a `blocks` patch to change appearance.

## 5. Registering an actual new tileset (for a new building's interior, etc.)

The Art Pipeline page **doesn't give a full worked code example** for registering a brand-new tileset from scratch — it documents the reskin path in detail but not this one. From Tutorial 07's "Advanced" note, a new tileset is registered via the `tilesets` registry with parameters including **`image`, `blocks`, and metadata fields like `grassTile` and `animation`** — but exact required fields and their types are **[inferred — verify against the actual registry schema / a real example mod before relying on this]**. When building the shop interior, check `Reference-Registries` for the `tilesets` schema directly (same way the `maps` schema was pulled for the warps guide) rather than guessing field names.

## 6. Common mistakes (as documented)

| Mistake | Consequence |
|---|---|
| Wrong pixel dimensions on a replacement sheet | Tiles render as the *wrong* tiles, not scaled — silent corruption, not an error |
| Changed tile count/layout without updating `tilesPerRow` | Tiles read from wrong offsets |
| Shipping ROM-derived pixels directly | `modkit pack` blocks it |
| Hand-editing `assets/generated/` | Wiped on next ROM import — put derived work in transforms so it regenerates, or in `save/mod-derived/` |
| Confusing tileset sheets with overworld character/NPC sprites | These are separate registries — a fix in one won't affect the other |
| Expecting a texture reskin to change walkability/warps | Behavior lives in `blocks` on the map record, unaffected by art transforms |

## Sources
- [Guide — Art Pipeline](https://github.com/bryanthaboi/gen1recomp/wiki/Guide-Art-Pipeline)
- [Tutorial 01 — Sprite and Text Tweak](https://github.com/bryanthaboi/gen1recomp/wiki/Tutorial-01-Sprite-And-Text-Tweak)
- [Tutorial 07 — New Map](https://github.com/bryanthaboi/gen1recomp/wiki/Tutorial-07-New-Map) (tileset registration mention)
- [Reference — Registries](https://github.com/bryanthaboi/gen1recomp/wiki/Reference-Registries) (check directly for full `tilesets` schema before registering a new one)

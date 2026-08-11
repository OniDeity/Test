# gen1recomp — Warps & Buildings Playbook (notes-to-self)

Written after a session where adding a new building in Celadon City and wiring up its warp went sideways. Purpose: capture the exact mechanics so the same mistakes aren't repeated. Sourced from the [wiki](https://github.com/bryanthaboi/gen1recomp/wiki) — quotes are cited; anything not directly documented is marked **[inferred — verify before relying on it]**.

## 1. There are two unrelated systems — don't mix them up

| | `connections` | `warps` |
|---|---|---|
| Use case | Seamless edge-to-edge overworld travel (walking off the edge of Route 1 into Pallet Town) | Discrete teleport-style transitions (doors, cave mouths, building interiors) |
| Lives on | `mod.content.maps` record, field `connections` | Same record, field `warps` |
| Shape | `{ north/south/east/west = { map = "ID", offset = n } }` | `list of { destMap, destWarp, x, y }` |
| Directionality | One-sided by default — **must patch both maps** to get two-way travel (Tutorial 07: *"A single connection is one-directional... patch the adjacent map"*) | Each entry stands alone; a building needs its own return warp defined separately (see §3) |

A new building in an existing town is a **warp**, not a connection. Don't try to model it as a `connections` edge.

## 2. Map registry field schema (from Reference-Registries)

`maps` registry semantics = **`record`** (matters a lot, see §4). Relevant fields:

- `warps` — `list of {destMap, destWarp, x, y}`
- `objects` — `list of any value`
- `blocks` — `list of integer 0..255`, length must equal `width * height`
- `tileset`, `width`, `height`, `borderBlock`, `connections`, `index` — as in Tutorial 07

## 3. `warps` entries reference each other **by index**, not by name

Each warp entry is `{ destMap, destWarp, x, y }`:
- `x, y` — the tile position of *this* warp on *this* map (where the player must be standing/step onto to trigger it, or where they appear if this is the landing entry — same field serves both roles depending on which side you're reading).
- `destMap` — the id of the map to send the player to.
- `destWarp` — **the numeric index into the destination map's own `warps` array**, not a coordinate. The engine looks up `destMap.warps[destWarp]` and uses *that* entry's `x, y` as the landing spot.

This mirrors the original Gen 1 warp-table design (two maps reference each other by array index, not by absolute coordinates). Concretely, for a Celadon City building:

```lua
-- Interior map — register the building. Its own warps[1] (index 0) is the exit.
mod.content.maps:register("CELADON_MY_SHOP", {
  id = "CELADON_MY_SHOP", label = "My Shop", index = 1000,
  tileset = "INTERIOR", width = 5, height = 4,
  blocks = { --[[ ... ]] },
  warps = {
    { destMap = "CELADON_CITY", destWarp = N, x = 2, y = 3 }, -- N = index of the door entry we add below
  },
})

-- Exterior map — append (do NOT replace) a door warp pointing into the shop.
mod.content.maps:patch("CELADON_CITY", {
  warps = { __append = {
    { destMap = "CELADON_MY_SHOP", destWarp = 0, x = 12, y = 4 }, -- destWarp=0 = the shop's only warp entry, its exit
  } },
})
```

**The index math is the sharpest edge here.** `N` above must equal the length of Celadon City's *existing* vanilla `warps` array (since `__append` puts your new entry at the end). Guessing this wrong doesn't error — it silently sends the player through the wrong door somewhere else in the city. **Don't guess it — dump/inspect the vanilla map data (or use `modkit`) to get the real count before hand-writing the index.**

## 4. THE likely root cause of past pain: `:patch()` on a list field replaces it wholesale

`maps` is `record` semantics. Per Concepts-Registries, for `record` semantics:

> `:patch()` performs deep merging on dictionary values recursively. Critically, **array values replace wholesale** — lists don't merge element-wise. To extend lists, wrap with `{ __append = { row } }` or `{ __prepend = { row } }`.

This means:

```lua
-- WRONG — wipes out every other door/warp already in Celadon City
mod.content.maps:patch("CELADON_CITY", {
  warps = { { destMap = "CELADON_MY_SHOP", destWarp = 0, x = 12, y = 4 } },
})

-- RIGHT — appends to the existing vanilla warps array
mod.content.maps:patch("CELADON_CITY", {
  warps = { __append = {
    { destMap = "CELADON_MY_SHOP", destWarp = 0, x = 12, y = 4 },
  } },
})
```

If a warp into a new building works but *other* doors in the same town stop working (or the whole town becomes un-enterable/exitable), this — a bare list assignment instead of `__append` — is the first thing to check.

Also don't reach for `mod.content.maps:register("CELADON_CITY", ...)` to "add" something to an existing vanilla map — `register` is for new ids; re-registering an existing one either errors on duplicate id or (per `record` semantics' whole-record replacement) blows away the rest of the vanilla map definition. Always `:patch()` an existing map.

## 5. End-to-end recipe for "new building in an existing town"

1. Get the exact current `warps` array length for the target vanilla map (Celadon City) before writing any index — don't hand-count from memory.
2. `mod.content.maps:register()` the interior map (tileset, width/height, blocks sized to `width*height`, `index >= 1000`).
3. Give the interior map its own `warps` entry (index 0 is fine if it's the only one) pointing back to `destMap = "CELADON_CITY"`, `destWarp = <the index from step 1>`.
4. `mod.content.maps:patch("CELADON_CITY", { warps = { __append = { ... } } })` adding the door, with `destWarp = 0` (or whatever index the interior's exit warp is at) and `x, y` = the door tile's position in the city.
5. Make sure the door tile itself (`blocks` at that `x,y` on Celadon City) is a door/entrance block from the tileset — the warp entry existing doesn't by itself guarantee the tile looks/behaves like a walkable entrance. **[inferred — verify: does stepping onto that tile trigger the warp regardless of block id, or does the block need a specific "warp" flag/type?]**
6. Run `modkit validate` (catches `blocks` length mismatches and similar structural errors, per Tutorial 07).
7. Manual test, both directions:
   - Walk into the new building from Celadon City → confirm correct interior + landing tile/facing.
   - Walk back out → confirm you land back at the correct door tile in Celadon City, facing the expected direction.
   - Walk through a few *other*, pre-existing Celadon City doors → confirm they still work (this is the check that catches the `__append`-vs-replace mistake).
   - Save inside the new building, reload → confirm state holds.

## 6. Symptom → likely cause

| Symptom | Likely cause |
|---|---|
| Other doors in the same town stopped working after adding the new one | Patched `warps` with a bare array instead of `{ __append = {...} } `— wiped the vanilla list |
| New door does nothing when walked into | Warp entry missing/wrong `x,y`, or tile isn't actually reachable/registered as an entrance **[inferred — confirm trigger mechanism]** |
| Walking through the new door sends you to the wrong place in the target map, or into a totally unrelated map | `destWarp` index is wrong — most likely miscounted the destination map's existing `warps` array length |
| Interior warp back outside also misbehaves | Same index problem, mirrored — the interior's `destWarp` must equal wherever the door entry actually landed inside Celadon City's array after `__append` |
| `modkit validate` fails on the new interior map | `blocks` array length != `width * height` |
| Game errors / crashes on entering the warp | `destWarp` index out of range for the destination map's `warps` array |

## 7. Open questions to nail down empirically next time (not documented on the wiki as fetched)

- Whether warp-triggering is purely `x,y`-presence-in-the-`warps`-list driven, or whether the underlying tile also needs a specific block type/flag.
- What determines player facing direction on arrival (not a field in the `warps` schema — may be implicit/engine-determined based on approach direction, unlike the scripted `warp mapId, x, y, facing` command which does take an explicit `facing` param).
- Whether `objects` (also a plain list, also `record` semantics) has the same `__append` requirement when adding a single NPC/object to an existing vanilla map — treat it the same way as `warps` until proven otherwise, since it's the same registry/semantics.

## Sources
- [Tutorial 07 — New Map](https://github.com/bryanthaboi/gen1recomp/wiki/Tutorial-07-New-Map)
- [Cookbook — World and Scripting](https://github.com/bryanthaboi/gen1recomp/wiki/Cookbook-World-And-Scripting)
- [Reference — Registries](https://github.com/bryanthaboi/gen1recomp/wiki/Reference-Registries)
- [Concepts — Registries](https://github.com/bryanthaboi/gen1recomp/wiki/Concepts-Registries)
- [Reference — Commands](https://github.com/bryanthaboi/gen1recomp/wiki/Reference-Commands) (scripted `warp` command, distinct from the static `warps` map field)

# gen1recomp — Custom NPC & Dialogue Playbook (notes-to-self)

Sourced from [Tutorial-06](https://github.com/bryanthaboi/gen1recomp/wiki/Tutorial-06-NPC-And-Dialogue). Quotes are cited; anything not directly documented is marked **[inferred — verify before relying on it]**.

## 1. Two ways to add an NPC — pick based on permanence

| | Static (map patch) | Dynamic (runtime spawn) |
|---|---|---|
| How | `mod.content.maps:patch(mapId, { objects = { __append = {...} } })` | `mod.world:spawnNpc(mapId, {...})` inside a `map.entered` event handler |
| Persists across saves | Yes — it's data on the map record | **No — not serialized**, respawned each time the handler fires |
| Use for | A permanent shopkeeper, the shop clerk, any NPC that should always be there | Conditional/event-driven NPCs (e.g. only appears after a flag is set) |

Since `objects` is a **list field on the `maps` record** (same `record`-semantics registry as `warps` — see `gen1recomp-warps-and-buildings-guide.md`), the same **`__append` vs bare-list gotcha applies**: patching `objects` with a plain array likely replaces the whole list and would delete every other object already on that map. **Always use `{ __append = { ... } }` when adding one NPC to an existing map**, never a bare array.

## 2. Static NPC object schema

```lua
mod.content.maps:patch("PALLET_TOWN", {
  objects = { __append = {
    { index = 90, x = 8, y = 7,
      sprite = "SPRITE_BEAUTY",
      movement = "STAY", range = "NONE",
      text = "TEXT_TUT6_NPC", name = "TUT6_NPC" },
  } },
})
```

Fields:
- `index` — unique identifier. **Use high numbers to avoid vanilla conflicts** — reusing a vanilla object index causes save-state conflicts.
- `x, y` — map tile coordinates.
- `sprite` — visual, e.g. `"SPRITE_BEAUTY"`.
- `movement` — `"STAY"` or `"WALK"`.
- `range` — movement bounds: `"ANY_DIR"`, `"UP_DOWN"`, `"LEFT_RIGHT"`, or a fixed facing (`"DOWN"`, `"UP"`, `"LEFT"`, `"RIGHT"`, `"NONE"`).
- `text` — the TEXT constant id this NPC's talk script is keyed to (e.g. `"TEXT_TUT6_NPC"`).
- `name` — NPC identifier, e.g. `"TUT6_NPC"`.

## 3. Dynamic (non-persistent) NPC

```lua
mod.events:on("map.entered", function(ev)
  if ev.mapId == "PALLET_TOWN" then
    mod.world:spawnNpc("PALLET_TOWN", {
      index = 91, x = 10, y = 9, sprite = "SPRITE_FISHER",
      movement = "STAY", range = "NONE", text = "TEXT_TUT6_NPC",
    })
  end
end)
```
Note this NPC **is not saved** — it's re-spawned every time `map.entered` fires for that map. Don't use this for anything that needs to persist (e.g. a shop clerk that should stay put after a purchase, or any state tied to talking to them once).

## 4. Dialogue: `map_scripts` registry, `compose` semantics

```lua
mod.content.map_scripts:register("PALLET_TOWN", {
  talk = {
    TEXT_TUT6_NPC = {
      { "face_player" },
      { "ask", "Want to hear a\\nsecret?" },
      { "jump_if_false", "no" },
      { "show_text", "The sign in this\\ntown is MODDED." },
      { "jump", "end" },
      { "label", "no" },
      { "show_text", "Suit yourself." },
    },
  },
})
```

`map_scripts` uses **`compose` semantics** — registrations accumulate into per-id chains rather than replacing each other (per Concepts-Registries), so `:register()` here is safe to call even when the map already has other scripted NPCs; it merges in rather than clobbering. This is the one registry in this project where `:register()` on an existing map id is the *normal*, expected call — unlike `maps` itself, which is `record` semantics and needs `:patch()` + `__append` for lists.

## 5. Script command reference relevant to NPC dialogue

| Command | Params | Notes |
|---|---|---|
| `face_player` | — | NPC turns to face the player |
| `face_npc` | — | player turns to face the NPC |
| `ask` | `textId, subs` | shows text with a YES/NO prompt, result goes into `lastCheck` |
| `jump_if_true` / `jump_if_false` | `target` | branches on `lastCheck` |
| `jump` | `target` | unconditional |
| `label` | `name` | jump target |
| `show_text` | `textId, subs` | plain text box |
| `choice` | `labels, opts` | N-way menu; sets `lastChoice` and `lastCheck` |
| `check_flag` / `set_flag` / `clear_flag` | `name` | story-flag branching, e.g. "only offer this dialogue once" |
| `give_item` / `take_item` | `itemId, count[, gotText]` | |
| `give_money` | `amount` | |
| `open_mart` | `textConst` | opens a shop menu — see `gen1recomp-shop-guide.md` for how a clerk NPC uses this |

## 6. Common pitfalls (as documented, plus one inferred from the warps precedent)

1. **Numeric jump targets break easily.** `{ "jump", 6 }` (a row number) breaks the moment rows are inserted/reordered above it. **Always use label strings** (`{ "jump", "no" }` + `{ "label", "no" }`), never row indices.
2. **Index collisions.** Reusing a vanilla `objects[].index` causes save-state conflicts. Pick a clearly-out-of-range number for every NPC you add.
3. **Talk entries don't chain.** Multiple mods (or multiple registrations) targeting the same `TEXT_*` key inside `talk` don't run in sequence — **"highest priority wins."** If an NPC's dialogue isn't showing what you expect, check whether something else is registered against the same TEXT constant at higher priority, rather than assuming your script has a bug.
4. **[inferred]** Patching `objects` on an existing map without `__append` likely wipes the map's other NPCs/objects, mirroring the confirmed `warps` behavior — verify this the same way: add an NPC, then confirm every other NPC on that map still exists before moving on.

## Sources
- [Tutorial 06 — NPC and Dialogue](https://github.com/bryanthaboi/gen1recomp/wiki/Tutorial-06-NPC-And-Dialogue)
- [Reference — Registries](https://github.com/bryanthaboi/gen1recomp/wiki/Reference-Registries) / [Concepts — Registries](https://github.com/bryanthaboi/gen1recomp/wiki/Concepts-Registries) (semantics: `record` vs `compose`)
- [Reference — Commands](https://github.com/bryanthaboi/gen1recomp/wiki/Reference-Commands)

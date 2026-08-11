# gen1recomp — Shop / Mart Playbook (notes-to-self)

Sourced from [Cookbook-Items-And-Balls](https://github.com/bryanthaboi/gen1recomp/wiki/Cookbook-Items-And-Balls), [Reference-Commands](https://github.com/bryanthaboi/gen1recomp/wiki/Reference-Commands), and [Concepts-Data-Model](https://github.com/bryanthaboi/gen1recomp/wiki/Concepts-Data-Model). Quotes are cited; anything not directly documented is marked **[inferred — verify before relying on it]**.

## 1. Where shop inventory actually lives — this is the non-obvious part

A "shop" is **not** its own registry. Per Concepts-Data-Model, a `text_pointers` entry has fields **`text`, `label`, `asm`, `mart` (a list of item ids — how shop stock is data), `nurse`, `pc`, `cableClub`.**

**Marts are `mart` lists on clerk TEXT entries in the `text_pointers` deep registry.** Concretely: the shop's stock is attached to the same TEXT constant used for the clerk's talk text — not to the NPC object, not to the map.

```lua
mod.content.items:register("MODBERRY", {
  id = "MODBERRY", name = "MODBERRY", price = 250, tossable = true,
})

mod.content.text_pointers:patch("ViridianMart", {
  TEXT_VIRIDIANMART_CLERK = { mart = { __append = { "MODBERRY" } } },
})
```

## 2. ⚠️ Conflicting documentation on list-patch semantics here — verify empirically before trusting either

Two different wiki pages say different things about how `:patch()` treats list fields on `text_pointers`:

- **Cookbook-Items-And-Balls (specific, about `mart` itself):** *"`__append` extends the shelf, a bare list replaces it."* — i.e. same append-gotcha as the `maps` registry's `warps`/`objects` fields.
- **Concepts-Registries (general, about `deep`-semantics registries, which `text_pointers` is one of):** *"both `:register()` and `:patch()` merge into the key, with **lists appending**"* rather than replacing — implying a bare list would be safe to append here specifically because `text_pointers` is `deep`, not `record`.

These two statements contradict each other for this exact case. **Don't guess — test it directly**: patch a mart with a bare list (no `__append`), reload, and check whether the vanilla stock is still present. Use `__append` regardless of the test result (it's the safe choice either way — either it's required, or it's a no-op because appending is already the default). Only try the bare-list form if there's a specific reason to want replace-not-append behavior, and confirm the outcome before shipping.

## 3. Wiring an NPC to actually open the shop

The NPC itself is a normal map object (see `gen1recomp-npc-guide.md`) whose `text` field points at the mart's TEXT constant. Its `talk` script (in `map_scripts`) calls the `open_mart` command instead of / in addition to `show_text`:

```lua
mod.content.map_scripts:register("CELADON_CITY", {
  talk = {
    TEXT_MY_SHOP_CLERK = {
      { "face_player" },
      { "show_text", "Welcome! Take a\\nlook around." },
      { "open_mart", "TEXT_MY_SHOP_CLERK" },
    },
  },
})
```

`open_mart(textConst)` — **"open a mart by TEXT constant"** — reads the `mart` list attached to that same TEXT entry in `text_pointers` and opens the buy/sell UI. The docs don't spell out explicit purchase/sell script commands separately; **purchasing is handled inside the `open_mart` screen itself**, not via additional script rows.

## 4. Item record fields relevant to a shop

From the `items` registry (partial — full schema not given on the pages fetched):
- `id`, `name` — identifiers.
- `price` — **[inferred]** likely both the buy price shown in the mart and the base for any sell-price calculation (Gen 1 sell price is typically half buy price) — verify in-game rather than assuming.
- `tossable` — whether the item can be discarded from the bag.
- `ball` — set on a ball item to link it to a `balls` registry entry (see below) — only relevant if the shop also sells a custom Poké Ball.
- `effect` — links to an `item_effects` registry entry if the item does something when used (not required just to be purchasable).

## 5. Custom Poké Ball, if the shop sells one

```lua
mod.content.items:register("MODBALL", {
  id = "MODBALL", name = "MODBALL", price = 500, ball = "MODBALL",
})
mod.content.balls:register("MODBALL", {
  randMax = 120, hpFactor = 10, wobbleFactor = 120,
  tossAnim = "ULTRATOSS_ANIM", flicker = true,
})
```
Ball catch-rate parameters use "the real Gen 1 math"; set `autoCatch = true` for Master Ball-style guaranteed catches, or supply `attempt = fn` to override the catch calculation entirely.

## 6. End-to-end recipe: new shop with a custom NPC clerk

1. Register any new sellable items first (`mod.content.items:register(...)`).
2. Decide which existing TEXT constant this shop's clerk will use, or define a new one consistent with the project's TEXT-naming convention (check a couple of vanilla mart entries in `text_pointers` for the pattern, e.g. `TEXT_<LOCATION>_CLERK`).
3. Patch `text_pointers` to attach the `mart` item list to that TEXT constant — use `__append` (§2).
4. Add the clerk as a map object (`gen1recomp-npc-guide.md` §1–2), with `text` set to that same TEXT constant.
5. Register a `talk` script in `map_scripts` for that TEXT constant, ending in `{ "open_mart", "<TEXT_CONST>" }`.
6. `modkit validate`, then test in-game: talk to the clerk, confirm the shelf shows exactly the intended items (vanilla + new, not just new — this catches the append-vs-replace question from §2), buy one, confirm bag/money update, save/reload.

## 7. Open questions to verify next time (not settled by the docs as fetched)

- Whether `mart` list patches actually need `__append` or not (§2) — resolve empirically, don't assume from either doc page alone.
- Whether sell price is auto-derived from `price` or needs a separate field.
- Whether a shop can be given per-NPC dynamic stock (e.g. rotating inventory) or if `mart` is strictly static list data — nothing in the fetched docs suggests a dynamic/conditional mart path; if that's wanted, it likely has to be done by patching `mart` at runtime via an event hook, which would need testing.

## Sources
- [Cookbook — Items and Balls](https://github.com/bryanthaboi/gen1recomp/wiki/Cookbook-Items-And-Balls)
- [Reference — Commands](https://github.com/bryanthaboi/gen1recomp/wiki/Reference-Commands) (`open_mart`)
- [Concepts — Data Model](https://github.com/bryanthaboi/gen1recomp/wiki/Concepts-Data-Model) (`text_pointers` entry fields)
- [Concepts — Registries](https://github.com/bryanthaboi/gen1recomp/wiki/Concepts-Registries) (semantics conflict noted in §2)

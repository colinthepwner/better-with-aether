# Changes in this fork

This is an unofficial fork of [Better with Aether](https://github.com/bta-team-port/better-with-aether)
by the bta-team-port team, modified to run on **BTA 8.0.1** instead of 7.3_04. Everything in the mod
itself is their work; this file records what was changed, as LGPL-3.0 requires of a modified version.

I am not affiliated with the Aether team, the BTA team, or the Better with Aether team. Please use
the official release instead if one exists for your BTA version.

## Scope

The port is mechanical: it follows BTA 8.0.1's API where it moved, and does not change gameplay,
balance, recipes, worldgen or art. Two behaviours that 8.0 dropped were restored to match 7.3, and
they are called out below.

## Pervasive API changes

- `BlockLogic` / `BlockModel` take `TilePosc pos` instead of `int x, int y, int z`
- Physics moved to JOML: `Vec3` → `Vector3d`/`Vector3dc`, `AABB` → `AABBd`/`AABBdc`
- `Tessellator` is a narrow interface; `TessellatorGeneral` is threaded through as a parameter
  instead of the removed `Tessellator.instance` global
- `HitResult` split into `Tile`/`Entity`/`Clip` subtypes
- `Material.stone` → `Materials.STONE`; `mc.gameSettings.x` → static `GameSettings.X`
- Armour slots are `IArmorShape`, not `int`
- No fixed-function GL — everything goes through `GLRenderer` (matrix stack, alpha test, colour,
  state enums). Rotations are radians where `glRotatef` took degrees.
- DragonFly is bundled in the game jar, and mob models must be BlockBench `.geo.json`; the old
  hand-written `ModelBase`/`ModelBiped`/`Cube` classes are gone
- The Aether's dimension id is **3** (was 9). BTA 8.0 requires the dimension id space to be
  contiguous, so this is not configurable in practice — **existing 7.3 Aether saves will not carry
  over**, since dimension data lives in `world/dimensions/<id>`.

## Bugs found and fixed during the port

Each of these compiled cleanly against 8.0 and silently did nothing, which is why they are listed:

- **38 dead overrides across 21 block classes.** `BlockInterface` keeps `@Deprecated`
  int-coordinate methods that forward to renamed `TilePosc` abstracts (`onBlockRightClicked` →
  `onInteracted`, `onEntityWalking` → `onEntityWalkedOn`, and eight more). `Block` delegates to
  `BlockLogic` using only the modern names, so overrides on the legacy names were unreachable. This
  had disabled all three machine GUIs, the locked chest, mimic, dungeon door and sign right-click,
  aercloud walking and bouncing, block placement metadata, and the skyroot-tool harvest bonuses.
- **All 22 mobs rendered the vanilla player skin.** 8.0 added `Mob.setTextureIdentifier()`, which
  also derives `basePath`/`defaultTexture`/`variantJsonPath`; assigning the `textureIdentifier`
  field directly leaves those pointing at `assets/minecraft/textures/entity/char/`.
- **Grass, tall grass and leaves rendered grey.** `Colorizers.registerColorizers()` clears the
  colorizer list and runs again on every reload, dropping the mod's colorizers before they were set
  up; lookups then returned white, and the affected textures are greyscale by design.
- **The ambrosium torch had no block models**, so it fell back to `minecraft:block/missing`.
- Aercloud, quicksoil, gravitite and dungeon-trap logic were dead for the same shim reason above.

## Restored behaviours

- **Accessory protection.** 8.0's `IArmorWearing.getTotalProtectionAmount` bounds its loop to
  `getNumArmorSlots()`, which for a player is the four vanilla armour shapes, so gloves and combat
  pendants stopped contributing protection. Restored to match 7.3.
- **Steel piston floating gravitite.** `BlockLogicPistonBaseSteel` no longer exists; reimplemented
  against the shared `BlockLogicPistonBase`.
- **Flower jar Aether dirt**, reimplemented against 8.0's DragonFly-model-based jar.

## Testing

There is an in-game harness at `teamport/aether/agent/AetherAgent.java`, enabled with
`./gradlew runClient -Paether.agent=1`. It drives the client through world creation, both inventory
screens and a portal trip to the Aether, then asserts things a launch alone cannot prove: that mob
textures and models resolve to real assets rather than fallbacks, that colorizers produce a real
colour, that sound ids resolve, that aerclouds damp and bounce, and that the machine screens open.
Read the `SUMMARY: N failure(s)` line for the verdict.

`check_mixins.ps1` is a static audit of every mixin injection target, which is much faster than
finding a stale target by launching.

**This has not been playtested.** The automated checks pass; that is not the same as the mod being
known-good.

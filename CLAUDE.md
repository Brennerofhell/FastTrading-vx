# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

FastTrading-vx is a **NeoForge** Minecraft mod (Minecraft `1.21.1`, NeoForge `21.1.240`) that adds a button to the villager trading GUI to repeat the currently selected trade until it's no longer available. It's a NeoForge port of the upstream Fabric mod [FastTrading](https://github.com/bendy1234/FastTrading) (itself a fork of SpeedTrading by ModsByLeo); the trading-GUI logic is unchanged from upstream, only the mod-loader integration differs. Mod id: `fasttradingvx`, base package: `io.brennerofhell.fasttradingvx`.

## Commands

- Build the mod jar: `./gradlew build` (output: `build/libs/fasttradingvx-<version>.jar`)
- Run a NeoForge client with the mod loaded (for manual/in-game testing — there is no automated test suite): `./gradlew runClient`
- Regenerate templated metadata (`neoforge.mods.toml`) after changing `gradle.properties`: `./gradlew generateModMetadata` (also runs automatically on IDE sync/build)

Mod/Minecraft/NeoForge/dependency versions are all set in `gradle.properties`, not `build.gradle`.

## Architecture

**Mixin-based integration.** Since this mod only needs to attach behavior to vanilla screens/handlers, almost everything is done via Mixin rather than NeoForge event hooks. Mixins are registered in `src/main/resources/fasttradingvx.mixins.json` and live under `mixin/`:
- `MerchantScreenMixin` injects into vanilla `MerchantScreen` — captures the player inventory on construction, adds the `SpeedTradeButton` widget on `init`, and ticks it from `containerTick`. It implements `MerchantScreenHooks` (the "duck interface" in `duck/`) so the GUI code (`gui/SpeedTradeButton`) can call back into screen internals (`fasttradingvx$computeState`, `fasttradingvx$autofillSellSlots`, `fasttradingvx$performTrade`, etc.) without depending on Mixin internals itself.
- `KeyboardMixin` / `MouseMixin` inject into `KeyboardHandler`/`MouseHandler` to force-update the mod's custom `KeyMapping`s (see `ModKeyBindings`) on every key/mouse event, since GLFW scancodes can't otherwise be polled outside that path. Both skip updating when a text field or a clickable widget/slot is focused/hovered, to avoid stealing input from vanilla UI.
- `HandledScreenAccessor` / `KeyBindingAccessor` are Mixin `@Accessor`/`@Invoker` interfaces exposing otherwise-private vanilla members (hovered slot lookup, key mapping click count) needed by the mixins above.

**Trade-repeat state machine.** `SpeedTradeButton` (a custom `AbstractButton`) owns a small phase machine (`Phase.INACTIVE` → `AUTOFILL` → `TRADE` → back to `AUTOFILL`...), driven every screen tick. Actual timing/pacing is delegated to `SpeedTradeTimer`, a static accumulator incremented once per client tick (`FastTrading.onClientTick`) by `1 / ModConfig.ticksBetweenActions`; the button drains it in a `while (shouldDoAction())` loop so multiple actions can fire in one tick if configured fast enough. Each iteration calls back through `MerchantScreenHooks` to autofill the buy slots and click the result slot, and re-validates state (offer still selected, still affordable, still in stock) via `PlayerInventoryUtil` before continuing.

**Config.** `ModConfig` extends `MidnightConfig` (from the external MidnightLib dependency) and is initialized/registered in `FastTrading`'s constructor, including wiring NeoForge's config-screen extension point directly (there is no Mod Menu equivalent on NeoForge). Config lives at `config/fasttradingvx.json` in-game. `AutofillBehavior` (`DEFAULT`/`STRICT`) and `TradeBlockBehavior` (`DAMAGEABLE`/`UNSTACKABLE`/`DISABLED`) are enums referenced from both the mixin and the button/tooltip code.

**Templated metadata.** `src/main/templates/META-INF/neoforge.mods.toml` is a template, not the final file — `build.gradle`'s `generateModMetadata` task expands `${...}` placeholders from `gradle.properties` (mod id/name/version/license, MC/NeoForge version ranges) into `build/generated/sources/modMetadata`, which is added as a resources source dir. Edit the template + `gradle.properties`, never a generated copy.

## Releases

Built jars are attached to GitHub Releases (tag format `vX.Y.Z+<mc_version>[-neoforge]`) and the current build is also committed directly into `build/libs/` (normally gitignored) so it's browsable/downloadable straight from the repo tree.

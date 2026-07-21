# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A NeoForge client mod for Minecraft 26.2 that adds a button to the villager trading GUI
to repeat the currently selected trade until it's no longer available. Package
`io.brennerofhell.fasttradingvx`, mod id `fasttradingvx`.

Lineage: [ModsByLeo/SpeedTrading](https://github.com/ModsByLeo/SpeedTrading) (original,
Fabric) → [bendy1234/FastTrading](https://github.com/bendy1234/FastTrading) (updated
Fabric fork) → this repo (NeoForge port + rebrand, forked via `gh repo fork`). `main` is
the actively developed branch; `fabric-legacy` is a frozen, untouched mirror of the
Fabric source as originally forked, kept only for history/credit-chain reference — never
port fixes onto it.

## Build / run / verify

```
./gradlew build           # compile + package; produces build/libs/fasttradingvx-<version>.jar
./gradlew compileJava     # faster feedback loop, skips resource processing/packaging
./gradlew runClient       # launches a real dev client (downloads/decompiles vanilla on
                           # first run, several minutes; fast afterwards). This is the
                           # only way to actually verify GUI/trading behavior — nothing
                           # about the merchant-screen interaction can be confirmed by
                           # reading code or compiling alone.
```

There is no test suite (`compileTestJava`/`test` are `NO-SOURCE`) — this is a small GUI
mod verified by building cleanly + manual `runClient` sessions, not automated tests.

**Verifying a change touching Minecraft/NeoForge APIs**: don't rely on general Minecraft
knowledge for exact class/method names/signatures — this project tracks a specific,
very recent MC/NeoForge version. Ground truth is the actual decompiled vanilla source,
available locally after any build at
`build/moddev/artifacts/minecraft-patched-<neo_version>-merged.jar` (a real jar,
`unzip`/`jar tf` it, or extract a specific file with `unzip -o -q <jar> <path>` into a
scratch dir and read it directly). This has repeatedly caught real API differences from
assumption (e.g. `AbstractWidget.mouseClicked` bails out early when `active == false`,
`MerchantMenu`'s single shared buy/result-slot design) — always check this jar before
trusting memory for anything MC/NeoForge-API-shaped.

**Version pins** (in `gradle.properties`): `minecraft_version` / `neo_version` must
match each other (check https://projects.neoforged.net/neoforged/neoforge for valid
pairs — this project intentionally pins to whatever NeoForge line matches the upstream
Fabric fork's MC version, even when that NeoForge line is still `-beta`, since matching
upstream's feature set matters more here than tooling stability). `midnightlib_version`
must have a matching `-neoforge` classifier for the same MC version on the Modrinth
maven (`https://api.modrinth.com/v2/project/midnightlib/version`) — verify before
bumping either version, don't assume availability.

## Architecture

### The duck-interface + mixin pattern (the core of this mod)

Vanilla's `MerchantScreen` has no extension points for this, so the mod reaches into it
via Mixin. The pattern, used consistently:

- **`duck/MerchantScreenHooks.java`** — a plain interface declaring everything the GUI
  code needs from the screen (`fasttradingvx$computeState`, `fasttradingvx$getTradeOffer`,
  etc. — the `fasttradingvx$` prefix is just a naming convention to avoid accidental
  collisions with real Minecraft members, not a mod-id namespace). Also declares the
  `State` enum used throughout (`CAN_PERFORM`, `OUT_OF_STOCK`, ...) — note tooltip lang
  keys are built by lowercasing the enum name (`"fasttradingvx.tooltip." +
  state.name().toLowerCase()`), so renaming a `State` value means renaming its lang key too.
- **`mixin/MerchantScreenMixin.java`** — `@Mixin(MerchantScreen.class)`, implements that
  interface by shadowing into vanilla fields/methods (`@Shadow private int shopItem;`,
  `@Shadow protected abstract void postButtonClick();`). This is where all real trade
  logic lives (state computation, autofill, performing the trade) and where the
  `SpeedTradeButton` widget gets injected into the screen (`@Inject` on `init`).
- **`gui/SpeedTradeButton.java`** — the actual widget (tick-driven state machine +
  tooltip rendering), talks to the screen only through the `MerchantScreenHooks`
  interface, never assumes anything about vanilla internals directly.
- Other mixins (`KeyboardMixin`, `MouseMixin`, `HandledScreenAccessor`,
  `KeyBindingAccessor`) exist solely to make the custom "override trade block" keybind
  responsive inside GUI screens (vanilla's input handling doesn't normally deliver
  keybind state changes while a screen has focus) — unrelated to the trading logic itself.

Only two consumers of `MerchantScreenHooks` exist in the whole repo (`SpeedTradeButton`,
`MerchantScreenMixin`) — it's safe to reshape its methods freely when extending behavior,
no hidden callers elsewhere.

### One shared slot triple — the constraint that shapes everything else

`MerchantMenu` has exactly **one** set of buy/result slots (indices 0, 1, 2) — there is
no per-trade slot state. Selecting a different trade row (`shopItem`) just changes what
recipe that same slot triple is matched against; the only place `shopItem` is ever
written is the vanilla trade-row click handler in `MerchantScreen.init()`, which calls
`postButtonClick()` (returns any existing slot 0/1 items to the inventory, then re-fills
for the newly selected trade, then sends `ServerboundSelectTradePacket` to the server).
Any feature that wants to act on "a specific trade" while the player might be looking at
a *different* one (e.g. the trade-pinning feature) must never fight this — the correct
pattern is to check whether the target trade is the one currently live (`shopItem`) and
do nothing at all otherwise, not to redirect the shared slots to a non-selected trade.
Getting this wrong doesn't crash or error, it just silently fights the player over the
same three slots (flicker, or a manual trade's items getting overwritten) — this class of
bug has to be reasoned through by hand or caught by a live `runClient` pass, not by tests.

### Config

`config/ModConfig.java` uses MidnightLib's `MidnightConfig` (loader-agnostic API,
`MidnightConfig.init(MOD_ID, ModConfig.class)` in `FastTrading`'s constructor). The
in-game config screen is wired up via NeoForge's native
`ModContainer.registerExtensionPoint(IConfigScreenFactory.class, ...)` — there is no Mod
Menu integration (no NeoForge equivalent exists; the upstream Fabric version used it).

### Loader-glue files

`src/main/templates/META-INF/neoforge.mods.toml` is a *template* (Groovy `${}` 
placeholders filled from `gradle.properties` by the `generateModMetadata` Gradle task
defined in `build.gradle`), not a static resource — edit the template, not a generated
copy. `src/main/resources/META-INF/accesstransformer.cfg` is picked up automatically by
ModDevGradle at that default path (no Gradle wiring needed). Mixins are registered via
the toml's `[[mixins]]` block, not any Gradle-side config.

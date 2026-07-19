# Fast Trading vX

Adds a button to the villager trading GUI to repeat the current trade until it's no longer available (again).

This is a **NeoForge** mod (ported from the upstream Fabric mod, see credits below).

## Installation

Requires [NeoForge](https://neoforged.net/) `26.2.0.25-beta+` for Minecraft `26.2`, plus [MidnightLib](https://modrinth.com/mod/midnightlib) (NeoForge build) as a dependency.

## Configuration

This mod can be configured in-game via the mod list's "Config" button, or by editing the configuration file at `config/fasttradingvx.json`.

### Config Options
- **ticksBetweenActions**: Number of ticks between actions (Default: `1`, Min: `0.025`).
- **autofillBehavior**: How to autofill the trade (Default: `DEFAULT`, Options: `DEFAULT`, `STRICT`).
- **tradeBlockBehavior**: When to block speed trading (Default: `DAMAGEABLE`, Options: `DAMAGEABLE`, `UNSTACKABLE`, `DISABLED`).

## Differences from FastTrading (Fabric)

- Runs on NeoForge instead of Fabric Loader/Fabric API.
- Mod id changed from `fasttrading` to `fasttradingvx`, package changed to `io.brennerofhell.fasttradingvx`.
- Config file moved to `config/fasttradingvx.json`; in-game config screen is now registered directly via NeoForge's own config-screen extension point instead of Mod Menu (which has no NeoForge equivalent).
- Trading GUI/tooltip logic is unchanged from FastTrading.

## Credits

FastTrading-vx is a NeoForge port and continuation of [FastTrading](https://github.com/bendy1234/FastTrading) by bendy1234, which is itself an updated fork of [SpeedTrading](https://github.com/ModsByLeo/SpeedTrading) by ModsByLeo. Full credit for the original mod concept and the trading-GUI logic goes to ModsByLeo, ADudeCalledLeo, and bendy1234 — this fork's contribution is the NeoForge port and package rebrand.

Licensed under MIT, same as upstream.

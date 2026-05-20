# Extension Readiness

## Best Supported Extension

The extension this design supports best is adding a smarter bot strategy.

The original code mixed bot decision-making into the main game flow. The refactored version introduces `PlayerStrategy`, with `BotStrategy` as the current implementation. This gives bot behavior a clear replacement point.

## Where The Change Would Go

A smarter bot could be added by creating a new class such as `SmartBotStrategy` that implements `PlayerStrategy`.

The main files involved would be:

- `PlayerStrategy`
- `BotStrategy`
- a new `SmartBotStrategy`
- `TurnController`
- `MoveSelector`
- `TurnResolver`

The new strategy would choose which card to play and which color to call after a wild. It could reuse `Card`, `CardRank`, `CardColor`, and `CardRules` instead of duplicating card parsing or legality checks.

Possible improvements include:

- avoiding wild cards until they are more useful,
- choosing colors based on the bot's remaining hand,
- preferring cards that reduce hand score,
- or choosing cards that leave more legal follow-up plays.

## What Still Makes Change Difficult

A smarter bot is now easier to add, but deeper rule changes are still harder.

`GameState` is still a broad mutable facade over players, piles, scores, and turn state. A more advanced bot might want a read-only snapshot of the game, such as other players' hand sizes or discard history, but that is not currently separated cleanly.

Action effects are also still tied to `CardRank` and `ActionEffects`, which mutate `GameState` directly. This is acceptable for the current rules, but configurable rule variants such as stackable draw cards would still require changes across rule and turn-resolution code.

## Summary

The design is best prepared for a smarter bot strategy. That extension can be added mostly by implementing `PlayerStrategy` and injecting the new strategy into the existing controller flow. The design still resists deeper rule variants because rule effects and mutable game state are still coupled.
# Final Project Supported Rules

This project implements a text-only UNO variant based on
`Final_Project_UNO_rules_reference.md`.

## Implemented Rules

| Rule | Status | Notes |
| --- | --- | --- |
| Correct deck composition | Implemented | 108 cards: four colors, number cards, Skip, Reverse, Draw Two, four Wild, and four Wild Draw Four. |
| Legal play validation | Implemented | Matches by active color, number, action type, or wild card. |
| Skip | Implemented | The next player loses a turn. |
| Reverse | Implemented | Direction changes with 3+ players; with 2 players it acts like Skip. |
| Draw Two | Implemented | The next player draws two cards and loses a turn. |
| Wild | Implemented | Player chooses the active color. Bots choose the color they hold most. |
| Wild Draw Four | Implemented | Player chooses color; next player draws four and loses a turn. |
| Draw/pass | Implemented | A player may draw; a legal drawn card may be played immediately. Bots play legal drawn cards automatically. |
| UNO call and penalty | Implemented | Bots call automatically. Humans are prompted; a missed call draws two penalty cards immediately. |
| Round scoring | Implemented | Winner scores remaining opponent cards: numbers at face value, actions 20, wilds 50. |
| Multi-round target | Implemented | `--target-score N` plays until a player reaches the target, with a 100-round safety cap. |

## Documented Variants

- Starting discard cards are redrawn until the first up card is a number card.
- Wild Draw Four challenge rules are not implemented.
- Draw-card stacking is not implemented.
- Only one human player is supported in the CLI.
- All hands are visible in the terminal.
- A human may choose to draw even while holding a legal card.
- Target-score mode uses the requested target instead of forcing the official 500-point target.

## Commands

```powershell
mvn clean verify
java -jar target/uno-cli.jar --bots 3 --games 5 --quiet --seed 123
java -jar target/uno-cli.jar --bots 3 --target-score 100 --quiet --seed 123
java -jar target/uno-cli.jar --human --bots 2 --games 1
```

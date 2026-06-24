# Final Project Report

## Implemented Rules

The final project implements the main UNO rule menu: classic 108-card deck
composition, legal play validation, Skip, Reverse, Draw Two, Wild, Wild Draw
Four, draw/pass behavior, UNO call with missed-call penalty, round scoring, and
multi-round target-score play.

Intentional variants are documented in `docs/rules-supported.md`: no Wild Draw
Four challenges, no draw-card stacking, one human player plus bots, visible
hands in the CLI, and number-card-only starting discards.

## CLI Play

The CLI can play fixed-round sessions:

```powershell
java -jar target/uno-cli.jar --bots 3 --games 5 --quiet --seed 123
```

It can also play until a score target:

```powershell
java -jar target/uno-cli.jar --bots 3 --target-score 100 --quiet --seed 123
```

Interactive play uses compact card codes such as `R5`, `YS`, `BR`, `G+2`,
`W`, and `W4`. Human players can enter a card index, a card code, or `draw`.
When a human reaches one card, the CLI prompts for an UNO call; missing it
draws two penalty cards.

## Architecture

The game keeps CLI interaction outside the core rule checks. `Card`,
`CardRules`, `CardRank`, `MoveSelector`, `TurnResolver`, and
`GameSessionController` are testable without console input. Final work also
narrows action-card effects behind `TurnEffectContext`, so Skip, Reverse, Draw
Two, and Wild Draw Four can be tested without exposing all of `GameState`.

Assignment 5 infrastructure remains intact: Maven, executable shaded JAR,
SLF4J/Logback logging, Flyway/Hibernate/H2 persistence, report modes, and
Docker builds/runs.

## Tests Added

Final tests cover:

- safety-limit regression seeds `1` and `2`
- bot Reverse selection
- 108-card deck composition
- legal play validation
- Skip, Reverse, Draw Two, Wild, and Wild Draw Four effects
- draw/pass behavior
- UNO call and missed-call penalty
- target-score sessions and argument validation
- action effects through a narrow context

The original 53 characterization checks still pass.

## Limitations

The project remains a text-only local CLI game. It does not implement Wild Draw
Four challenges, stacking draw cards, jump-in, 7-0, hidden hands, network play,
or multiple human players. Target-score mode has a 100-round cap to protect
automated runs.

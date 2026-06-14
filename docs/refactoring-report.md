# Refactoring Report

## Overview

The original program implemented the UNO game almost entirely inside one large `Main` class. It used global mutable state, primitive string card values, duplicated rule checks, direct console input/output inside gameplay logic, and a long turn loop that mixed parsing, validation, card effects, scoring, and turn advancement.

The goal of this refactor was to improve the design while preserving the existing behavior of the game. I did not rewrite the project from scratch. Instead, I extracted responsibilities gradually, added characterization tests, and repeatedly checked that the game still compiled and behaved the same during seeded bot runs.

The refactor intentionally preserved the implemented game rules and quirks, including:

- all hands being visible in the terminal,
- human players being allowed to type `draw` even when they already hold a legal card,
- invalid index input causing a penalty card and turn loss,
- illegal card play causing a penalty card and turn loss,
- bot players automatically playing a drawn card when it is legal,
- wild cards requiring a called color,
- the draw pile fallback behavior,
- seeded deterministic runs,
- and the existing command-line interface.

## Characterization Testing

Before and during the refactoring, I added characterization tests to describe the behavior of this implementation. These tests are not meant to define ideal UNO rules. They document what this project currently does so that refactoring can preserve it.

The characterization checks cover:

- matching by color,
- matching by number,
- matching by action type,
- wild and wild draw four legality,
- skip behavior,
- reverse behavior,
- two-player reverse behavior,
- draw two behavior,
- wild draw four behavior,
- drawing from the deck,
- bot auto-play of a legal drawn card,
- bot keeping an illegal drawn card,
- human drawing even while holding a legal card,
- invalid index penalty behavior,
- illegal card penalty behavior,
- scoring,
- input parsing,
- called color parsing,
- card parsing,
- and empty deck fallback behavior.

The checks are runnable through Maven with:

```sh
mvn test
```

The suite preserves all 53 characterization checks from the midterm.

## Incremental Refactoring Steps

The refactor was performed in small commits. After meaningful changes, I ran compilation, characterization tests, and deterministic bot games.

Important refactoring steps included:

1. Moving characterization tests out of `Main`.
2. Adding characterization tests for card rules and action effects.
3. Extracting card rule helpers into `CardRules`.
4. Introducing a `Card` value object.
5. Introducing `CardColor` and `CardRank`.
6. Moving deck behavior into `CardPiles`.
7. Moving scoring behavior into `ScoreCalculator` and `ScoreBoard`.
8. Extracting turn state into `TurnState`.
9. Extracting player state into `PlayerTable`.
10. Replacing parallel player lists with a `Player` object.
11. Extracting game setup into `TurnController`.
12. Separating move selection into `MoveSelector`.
13. Separating turn resolution into `TurnResolver`.
14. Separating console output behind `GameView` and `ConsoleView`.
15. Moving action effect behavior into `CardRank` and `ActionEffects`.
16. Introducing `PlayerStrategy` so bot behavior can be replaced more easily.
17. Reducing duplicated legality checks by routing play validation through `CardRules`.

## Design Improvements

### Centralized Card Legality

The original code repeated legal-play checks in more than one place. Bot card choice, the game loop, and helper methods each had similar conditional logic.

The refactored version centralizes this logic in `CardRules.isLegal(...)`. This reduces duplication and makes rule behavior easier to test directly.

### Clearer Card Representation

The original implementation represented cards only as strings such as `R5`, `GS`, and `W4`.

The refactored version introduces:

- `Card`
- `CardColor`
- `CardRank`

This keeps parsing, rank detection, color detection, number detection, and point calculation closer to the card concept instead of spreading those checks through the game loop.

### Separated Turn Responsibilities

The original `playGame` method handled setup, rendering, input, bot decisions, move validation, penalties, drawing, scoring, action effects, and turn advancement.

The refactored design separates this into:

- `TurnController` for overall game and turn orchestration,
- `MoveSelector` for human/bot move selection and draw handling,
- `TurnResolver` for resolving the selected move,
- `ActionEffects` and `CardRank` for action card effects,
- `ScoreCalculator` for winner score calculation.

This makes individual pieces easier to read and test.

### Separated Console Interaction

The original game printed directly from the main gameplay logic and read directly from the scanner.

The refactored version introduces:

- `GameView`
- `ConsoleView`

This separates the console display and prompts from most rule execution. It also allows tests to use fake views instead of real console input.

### Improved Player Representation

The original code used parallel lists for player names, human/bot flags, and hands. This required the same index to mean the same player across several lists.

The refactored version introduces a `Player` object, so each player owns their name, human flag, and hand together. `PlayerTable` now manages a list of `Player` objects instead of parallel lists.

### Extension Point for Bot Strategy

The refactored version introduces `PlayerStrategy`, with `BotStrategy` as the current implementation. This allows a smarter bot strategy to be added without rewriting turn resolution or card legality rules.

## Behavior Intentionally Preserved

The following behaviors were intentionally preserved because they are part of the implemented rules or existing CLI behavior:

- Human players may choose `draw` even if they have a legal card.
- Human players may choose whether to play a drawn legal card.
- Bot players automatically play a drawn legal card.
- Invalid index input causes a penalty card and turn loss.
- Illegal card play causes a penalty card and turn loss.
- Wild and wild draw four cards can always be played.
- Called color affects later card legality.
- Reverse with two players acts like a skip.
- Empty deck and empty discard fallback returns a `W` card.
- Final scores still print after quiet bot runs.
- The CLI commands and scripts remain the same.

## Verification

After refactoring steps, I repeatedly ran:

```sh
mvn clean compile
mvn test
mvn exec:java -Dexec.args="--bots 3 --games 5 --quiet --seed 123"
```

The final characterization test output is:

```text
Passed 53 characterization checks.
```

The deterministic seeded bot run still produces the same final score output used during refactoring:

```text
Final scores:
Bot1: 138
Bot2: 246
Bot3: 98
```

## Remaining Risks and Limitations

The refactor intentionally avoids a full rewrite, so some limitations remain.

`GameState` is still a broad mutable facade over players, piles, scoring, and turn state. This is better than global state in `Main`, but it still exposes many mutation methods.

Action effects are centralized, but they are currently implemented through `CardRank` and `ActionEffects`, which means action cards still mutate `GameState` directly. This is acceptable for the current project size, but more complex rule variants may require a separate rule/effect abstraction later.

The original custom checks now run through JUnit and Maven while preserving all 53 assertions. Maven provides standard test discovery and failure reporting without manual classpath setup.

The parser accepts some card strings that the deck does not normally generate, such as `R10`. This preserves helper behavior during refactoring, but the actual deck still only creates number cards from 0 through 9.

Some classes and methods remain package-private because the project uses a simple single-package structure. Maven now supplies the standard `src/main/java` and `src/test/java` layout without forcing an unrelated package redesign.

## Summary

The refactor moved the project away from a single procedural `Main` class toward clearer responsibilities. Card rules, card representation, turn selection, turn resolution, scoring, player state, view rendering, and bot strategy now have separate homes.

The result is not a complete redesign, but it is safer to change, easier to test, and better prepared for a focused extension than the original version.

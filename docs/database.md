# Game History Database

## Overview

Assignment 5 adds persistence without changing the implemented UNO rules.
Each CLI gameplay invocation is a game session, and each `--games` iteration
is a round. The complete session is saved in one transaction after gameplay
finishes.

The persistence stack is:

- Hibernate ORM 7.4.1.Final with Jakarta Persistence
- H2 2.4.240
- Flyway 12.8.1

Flyway is the schema authority. On startup it applies migrations from
`src/main/resources/db/migration`. Hibernate then validates the mappings with
`hibernate.hbm2ddl.auto=validate`; it does not create or alter tables.

## Schema

The initial migration creates five application tables:

| Table | Purpose |
| --- | --- |
| `players` | Display names and unique normalized names |
| `games` | Session timestamps and requested/completed round counts |
| `game_players` | Seats, final cumulative scores, and final-winner flags |
| `rounds` | Round timestamps, status, winner, and awarded points |
| `round_scores` | Per-player score before, delta, and score after each round |

Foreign keys connect every participant and score to a player. Unique
constraints prevent duplicate seats, duplicate players within a game,
duplicate round numbers, and duplicate player scores within a round.

Player names are normalized with `Locale.ROOT` for reuse and
case-insensitive win lookups. The first stored display spelling is retained.
Tied highest final scores mark every tied participant as a winner. A session
containing only safety-limit rounds has no final winner.

All application timestamps are UTC `Instant` values. Gameplay orchestration
accepts an injectable `Clock` so persistence tests can use deterministic
timestamps.

## Local Configuration

The default file database URL is:

```text
jdbc:h2:file:./data/uno-history
```

This produces H2 files under `data/`, which is excluded from Git. Override the
connection with environment variables:

```powershell
$env:UNO_DB_URL = 'jdbc:h2:file:./data/alternate-history'
$env:UNO_DB_USER = 'sa'
$env:UNO_DB_PASSWORD = ''
java -jar target/uno-cli.jar --bots 3 --games 5 --quiet
```

The defaults are embedded H2 development settings and contain no private
credentials.

## Build And Test

Run the complete clean verification:

```powershell
mvn clean verify
```

Persistence tests use a unique H2 in-memory database per test. They verify:

- Flyway tables, keys, relationships, and constraints
- Hibernate mapping validation
- complete aggregate persistence and reload
- case-insensitive player reuse
- rollback without partial history
- recent-game ordering and limits
- tied, unknown, and case-insensitive win counts
- highest-score ordering and limits
- report argument parsing and rendering
- the seeded final-project `123`, `237`, `0` five-round session and `Bot2` winner

## Reports

Package before running the JAR commands:

```powershell
mvn clean package
```

Play and persist a deterministic session:

```powershell
java -jar target/uno-cli.jar --bots 3 --games 5 --quiet --seed 123
```

Read recent sessions, newest first:

```powershell
java -jar target/uno-cli.jar --recent-games 10
```

Count final session wins:

```powershell
java -jar target/uno-cli.jar --player-wins Bot2
```

Read final cumulative scores, highest first:

```powershell
java -jar target/uno-cli.jar --highest-scores 10
```

Report limits are optional, default to `10`, and accept `1` through `100`.
Report modes are mutually exclusive and cannot be combined with gameplay
arguments. Unknown player names return zero wins.

## Docker Persistence

Build the multi-stage image:

```powershell
docker build -t uno-cli .
```

Create a named history volume:

```powershell
docker volume create uno-history
```

Write a session and read it from a later container:

```powershell
docker run --rm -v uno-history:/app/data uno-cli --bots 3 --games 5 --quiet --seed 123
docker run --rm -v uno-history:/app/data uno-cli --recent-games 10
docker run --rm -v uno-history:/app/data uno-cli --player-wins Bot2
docker run --rm -v uno-history:/app/data uno-cli --highest-scores 10
```

The mount is required for persistence across containers. Running
`docker run --rm uno-cli` uses a disposable anonymous volume and remains a
finite bot game.

## Reset

Stop any process using the database before resetting it.

For the default local database:

```powershell
Remove-Item data\uno-history.mv.db -ErrorAction SilentlyContinue
Remove-Item data\uno-history.trace.db -ErrorAction SilentlyContinue
```

Flyway recreates an empty schema on the next command.

For Docker:

```powershell
docker volume rm uno-history
docker volume create uno-history
```

Removing a database file or Docker volume permanently deletes its stored game
history.

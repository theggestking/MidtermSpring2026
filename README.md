# UNO CLI

This command-line UNO-like game uses Maven, Java 21, structured logging, an
executable JAR, Docker, and persistent game history. Flyway owns the schema,
Hibernate ORM maps it through Jakarta Persistence, and H2 supplies the local
file database.

One CLI gameplay invocation is stored as one game session. Each `--games`
iteration is stored as a round within that session.

## Requirements

- Java 21 or newer
- Maven 3.9 or newer
- Docker Desktop or another Docker engine for container commands
- WSL 2 enabled when Docker Desktop uses Linux containers on Windows

The project compiles with the Java 21 release target. No IDE or manual
classpath configuration is required.

## Build And Test

Compile:

```powershell
mvn clean compile
```

Run all tests:

```powershell
mvn test
```

The Maven suite runs the 53 preserved characterization assertions, logging
tests, isolated Flyway/Hibernate tests, repository transaction tests, seeded
persistence tests, and report tests.

Create the self-contained executable JAR:

```powershell
mvn clean package
```

The Maven Shade Plugin writes the executable application to
`target/uno-cli.jar`.

## Run Locally

Run through Maven:

```powershell
mvn exec:java '-Dexec.args=--bots 3 --games 5 --quiet'
```

Run the packaged JAR:

```powershell
java -jar target/uno-cli.jar --bots 3 --games 5 --quiet
```

Run an interactive game:

```powershell
java -jar target/uno-cli.jar --human --bots 2 --games 1
```

Supported gameplay arguments:

| Argument | Meaning |
| --- | --- |
| `--bots N` | Add `N` bot players |
| `--games N` | Play and persist `N` rounds |
| `--human` | Add one human player |
| `--quiet` | Hide turn-by-turn player output |
| `--seed N` | Use a deterministic random seed |
| `--help` | Print command usage |

UNO needs a total of two to four players.

Card input examples:

```text
R5   red 5
YS   yellow skip
BR   blue reverse
G+2  green draw two
W    wild
W4   wild draw four
draw draw a card
```

## Game History

The default database is:

```text
jdbc:h2:file:./data/uno-history
```

Flyway runs before Hibernate starts. Hibernate uses
`hibernate.hbm2ddl.auto=validate`, so Hibernate validates the migration-created
schema and never creates or updates it.

Read recent sessions:

```powershell
java -jar target/uno-cli.jar --recent-games 10
```

Count a player's final session wins, case-insensitively:

```powershell
java -jar target/uno-cli.jar --player-wins Bot2
```

Read the highest final cumulative session scores:

```powershell
java -jar target/uno-cli.jar --highest-scores 10
```

Limits default to `10` and must be from `1` through `100`. A report mode cannot
be combined with gameplay arguments.

Database configuration can be overridden without changing source:

| Environment variable | Default |
| --- | --- |
| `UNO_DB_URL` | `jdbc:h2:file:./data/uno-history` |
| `UNO_DB_USER` | `sa` |
| `UNO_DB_PASSWORD` | empty |

The embedded defaults are local H2 settings, not private credentials. The
generated `data/` directory is ignored by Git. See
[`docs/database.md`](docs/database.md) for the schema, reset process, tests,
and Docker details.

## Logging

Player-facing CLI and report output is written to stdout. SLF4J and Logback
write diagnostic events to stderr. Logged events include game starts, player
turns, played and drawn cards, invalid input, game endings, and session
endings. Logs do not include complete hands, local paths, or credentials.

## Docker

Build the image entirely from repository contents:

```powershell
docker build -t uno-cli .
```

Start the finite default bot game:

```powershell
docker run --rm uno-cli
```

Override the disposable default game:

```powershell
docker run --rm uno-cli --bots 3 --games 5 --quiet --seed 123
```

Use a named volume when history must survive between containers:

```powershell
docker volume create uno-history
docker run --rm -v uno-history:/app/data uno-cli --bots 3 --games 5 --quiet --seed 123
docker run --rm -v uno-history:/app/data uno-cli --recent-games 10
```

Run interactively with persistent history:

```powershell
docker run --rm -it -v uno-history:/app/data uno-cli --human --bots 2 --games 1
```

Without a named volume, the container still runs, but its H2 history is removed
with the container. The builder supplies Maven and Java 21. The runtime image
contains Java 21, `uno-cli.jar`, and an empty `/app/data` mount point.

## Optional Shell Scripts

On systems with a POSIX shell, the compatibility shortcuts delegate to Maven:

```sh
scripts/compile.sh
scripts/test.sh
scripts/run.sh --bots 3 --games 5 --quiet
```

The Maven commands above are the primary cross-platform workflow.

## Project Documentation

- `docs/database.md`: persistence design and operations
- `docs/rules.html`: implemented game rules
- `docs/refactoring-report.md`: midterm refactoring report
- `docs/extension-readiness.md`: extension readiness analysis

# UNO CLI

This command-line UNO-like game uses Maven for compilation, testing,
packaging, and execution. It also includes structured diagnostic logging and a
Docker image.

## Requirements

- Java 21 or newer
- Maven 3.9 or newer
- Docker Desktop or another Docker engine for container commands
- WSL 2 enabled when using Docker Desktop with Linux containers on Windows

The project compiles with the Java 21 release target. It does not require an
IDE or manual classpath configuration.

## Build And Test

Compile the production code:

```powershell
mvn clean compile
```

Run all tests:

```powershell
mvn test
```

The Maven test lifecycle runs the 53 preserved characterization assertions and
the logging tests.

Create the self-contained executable JAR:

```powershell
mvn clean package
```

The packaged application is written to `target/uno-cli.jar`.

## Run Locally

Run through Maven:

```powershell
mvn exec:java -Dexec.args="--bots 3 --games 5 --quiet"
```

Run the packaged JAR:

```powershell
java -jar target/uno-cli.jar --bots 3 --games 5 --quiet
```

Run an interactive game:

```powershell
java -jar target/uno-cli.jar --human --bots 2 --games 1
```

Supported arguments:

| Argument | Meaning |
| --- | --- |
| `--bots N` | Add `N` bot players |
| `--games N` | Play `N` games |
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

## Logging

Player-facing CLI output is written to stdout. SLF4J and Logback write
diagnostic events at INFO level or higher to stderr. Logged events include game
starts, player turns, played and drawn cards, invalid input, game endings, and
session endings. Logs do not include complete player hands or machine-specific
paths.

## Docker

Build the image entirely from repository contents:

```powershell
docker build -t uno-cli .
```

Start the finite default bot game:

```powershell
docker run --rm uno-cli
```

Override the default arguments:

```powershell
docker run --rm uno-cli --bots 3 --games 5 --quiet --seed 123
```

Run interactively:

```powershell
docker run --rm -it uno-cli --human --bots 2 --games 1
```

The Docker builder supplies Maven and Java 21. The runtime image contains Java
21 and the self-contained `uno-cli.jar`; it does not depend on files outside
this repository.

## Optional Shell Scripts

On systems with a POSIX shell, the legacy shortcuts now delegate to Maven:

```sh
scripts/compile.sh
scripts/test.sh
scripts/run.sh --bots 3 --games 5 --quiet
```

Maven commands above are the primary cross-platform workflow.

## Project Documentation

- `docs/rules.html`: implemented game rules
- `docs/refactoring-report.md`: midterm refactoring report
- `docs/extension-readiness.md`: extension readiness analysis

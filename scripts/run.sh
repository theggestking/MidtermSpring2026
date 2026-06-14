#!/usr/bin/env sh
set -eu

mvn --batch-mode package
exec java -jar target/uno-cli.jar "$@"


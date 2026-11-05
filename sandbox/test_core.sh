#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
WORK="$(mktemp -d)"; trap 'rm -rf "$WORK"' EXIT
kotlinc "$ROOT/app/src/main/java/bo/genai/core/ConnectionModel.kt" "$ROOT/sandbox/ConnectionModelTest.kt" -include-runtime -d "$WORK/core-test.jar"
java -jar "$WORK/core-test.jar"

#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT
kotlinc "$ROOT/app/src/main/java/bo/genai/usbpowerbridge/UsbProbe.kt" \
    "$ROOT"/sandbox/usbprobe_stubs/*.kt -include-runtime -d "$TMP/usbprobe-test.jar"
java -jar "$TMP/usbprobe-test.jar"

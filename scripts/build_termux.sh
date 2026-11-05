#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail
umask 077
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG="$ROOT/build_logs/compilacion_$(date +%Y%m%d_%H%M%S).log"
mkdir -p "$(dirname "$LOG")"
cd "$ROOT"
python3 scripts/validar_modo_real.py > "$LOG" 2>&1 || { echo "VALIDACION=ERROR LOG=$LOG"; exit 1; }
command -v java >/dev/null 2>&1 || { echo "APK=NO_GENERADO FALTA=JAVA"; exit 1; }
command -v gradle >/dev/null 2>&1 || { echo "APK=NO_GENERADO FALTA=GRADLE"; exit 1; }
[ -d "${ANDROID_HOME:-/NON_EXISTENT}" ] || [ -d "${ANDROID_SDK_ROOT:-/NON_EXISTENT}" ] || {
 echo "APK=NO_GENERADO FALTA=ANDROID_SDK"; exit 1;
}
if gradle --no-daemon --console=plain :app:assembleDebug >> "$LOG" 2>&1; then
  APK="$ROOT/app/build/outputs/apk/debug/app-debug.apk"
  if [ -s "$APK" ]; then
    echo "COMPILACION=SUCCESS"
    sha256sum "$APK"
    echo "APK=$APK"
    exit 0
  fi
fi
echo "COMPILACION=FAILED LOG=$LOG"
exit 1

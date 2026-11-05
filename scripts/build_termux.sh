#!/usr/bin/env bash
# GENAI USB POWER BRIDGE: construcción local controlada, sin instalación automática.
set -Eeuo pipefail
umask 077
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOGS="$ROOT/build_logs"
mkdir -p "$LOGS"
LOG="$LOGS/build_$(date +%Y%m%d_%H%M%S).log"
exec 3>&1
exec >> "$LOG" 2>&1
BUILD_SUCCEEDED=0
TMP=''
on_exit() {
  local rc="$?"
  if [[ -n "$TMP" && -d "$TMP" ]]; then rm -rf "$TMP"; fi
  if [[ "$rc" -eq 0 && "$BUILD_SUCCEEDED" == 1 && -s "$ROOT/app/build/outputs/apk/debug/app-debug.apk" ]]; then
    printf 'APK=GENERADO; ESTADO=COMPILADO_SIN_INSTALAR\nLOG=%s\n' "$LOG" >&3
  else
    printf 'APK=NO_GENERADO_EN_ESTA_EJECUCION; ESTADO=BLOQUEADO; CODIGO=%s\nLOG=%s\n' "$rc" "$LOG" >&3
  fi
}
trap on_exit EXIT
printf 'GENAI USB POWER BRIDGE | Compilación controlada v0.2.0\nREGISTRO=%s\n' "$LOG" >&3
error() { printf 'BLOQUEO: %s\n' "$*" >&2; exit 1; }
check() { command -v "$1" >/dev/null 2>&1 || error "falta comando $1"; }
printf '=== GENAI INDUSTRIES | BUILD ANDROID CONTROLADO ===\n'
printf 'PROYECTO=%s\nREGISTRO=%s\n' "$ROOT" "$LOG"
check java
check python
check sha256sum
java -version
printf '\n[1] Pruebas de lógica local (no validan Android ni hardware)\n'
(
  cd "$ROOT"
  if command -v kotlinc >/dev/null 2>&1; then
    bash sandbox/test_core.sh
    bash sandbox/test_usbprobe_stubs.sh
  else
    printf 'AVISO=kotlinc CLI no instalado; Gradle podrá compilar Kotlin. Prueba JVM local omitida.\n'
  fi
  python sandbox/simulate.py
  python sandbox/file_operations_dryrun.py
  python sandbox/verify_project.py
)
printf '\n[2] Entorno Android\n'
SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [[ -z "$SDK" ]]; then
  for candidate in "$HOME/android-sdk" "$HOME/Android/Sdk" "$HOME/sdk" "${PREFIX:-/nonexistent}/share/android-sdk"; do
    if [[ -d "$candidate/platforms" ]]; then SDK="$candidate"; break; fi
  done
fi
[[ -n "$SDK" && -d "$SDK" ]] || error 'SDK Android no localizado. Configura ANDROID_HOME con ruta a SDK y vuelve a ejecutar.'
[[ -f "$SDK/platforms/android-35/android.jar" ]] || error "falta Android SDK Platform 35 en $SDK/platforms/android-35. Instálala con sdkmanager o Android Studio."
export ANDROID_HOME="$SDK"
export ANDROID_SDK_ROOT="$SDK"
printf 'ANDROID_HOME=%s\n' "$SDK"

printf '\n[3] Preparar Gradle Wrapper 8.9 si falta\n'
if [[ ! -f "$ROOT/gradle/wrapper/gradle-wrapper.jar" || ! -x "$ROOT/gradlew" ]]; then
  check gradle
  TMP="$(mktemp -d)"
  printf "rootProject.name = 'genai-wrapper-setup'\n" > "$TMP/settings.gradle"
  printf '// Proyecto aislado sin plugins externos\n' > "$TMP/build.gradle"
  (cd "$TMP" && gradle --no-daemon wrapper --gradle-version 8.9 --distribution-type bin)
  [[ -f "$TMP/gradle/wrapper/gradle-wrapper.jar" ]] || error 'Gradle no generó el wrapper.'
  mkdir -p "$ROOT/gradle/wrapper"
  cp "$TMP/gradle/wrapper/gradle-wrapper.jar" "$ROOT/gradle/wrapper/"
  cp "$TMP/gradle/wrapper/gradle-wrapper.properties" "$ROOT/gradle/wrapper/"
  cp "$TMP/gradlew" "$ROOT/gradlew"
  chmod 700 "$ROOT/gradlew"
  rm -rf "$TMP"
  TMP=''
fi
# Se usa wrapper 8.9 por compatibilidad con AGP 8.7.3; no cambiar automáticamente plugins.
if ! grep -q 'gradle-8\.9-bin\.zip' "$ROOT/gradle/wrapper/gradle-wrapper.properties"; then
  error 'Wrapper existente no usa Gradle 8.9; revisar la configuración manualmente.'
fi
printf '\n[4] Compilación debug; no se instalará el APK\n'
(cd "$ROOT" && ./gradlew --no-daemon --console=plain :app:assembleDebug)
APK="$ROOT/app/build/outputs/apk/debug/app-debug.apk"
[[ -s "$APK" ]] || error 'Gradle no creó app-debug.apk.'
sha256sum "$APK" | tee "$LOGS/APK_SHA256.txt"
if command -v apksigner >/dev/null 2>&1; then
  apksigner verify --verbose "$APK" || error 'Firma de APK no válida.'
else
  printf 'AVISO=apksigner no disponible; verificar firma antes de instalar.\n'
fi
printf 'APK_RUTA=%s\nSHA256=%s\n' "$APK" "$(sha256sum "$APK" | awk '{print $1}')" >&3
BUILD_SUCCEEDED=1
printf 'COMPILACION=COMPLETADA\nAPK=%s\nLOG=%s\nINSTALACION_AUTOMATICA=NO\n' "$APK" "$LOG"

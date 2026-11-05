#!/usr/bin/env bash
set -Eeuo pipefail
umask 077
if [ "${1:-}" != "--publicar" ]; then
  echo "USO=bash scripts/PUBLICAR_TELEMETRIA_V05_TERMUX.sh --publicar"
  exit 2
fi
TARGET='luis171987/OTG-POWER-BRIDGE'
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BASE="$HOME/OTG_POWER_BRIDGE"
mkdir -p "$BASE"
WORK="$(mktemp -d "$BASE/PUBLISH_V05_XXXXXXXX")"
LOG="$WORK/registro.log"
fail() { echo "PUBLICACION=NO_COMPLETADA"; echo "MOTIVO=$1"; echo "REGISTRO=$LOG"; exit 1; }
for t in git gh python3; do command -v "$t" >/dev/null || fail "FALTA_$t"; done
python3 "$ROOT/scripts/validar_telemetria_v05.py" >"$WORK/validacion.log" 2>&1 || fail VALIDACION_FALLIDA
python3 "$ROOT/scripts/validar_modo_real.py" >>"$WORK/validacion.log" 2>&1 || fail MODO_REAL_FALLIDO
IDENTITY="$(gh api user --jq .login 2>>"$LOG")" || fail GH_NO_AUTENTICADO
[ "$IDENTITY" = 'luis171987' ] || fail CUENTA_INCORRECTA
OWNER="$(gh repo view "$TARGET" --json nameWithOwner --jq .nameWithOwner 2>>"$LOG")" || fail REPO_NO_ACCESIBLE
[ "$OWNER" = "$TARGET" ] || fail DESTINO_INCORRECTO
VIS="$(gh repo view "$TARGET" --json visibility --jq .visibility 2>>"$LOG")" || fail SIN_VISIBILIDAD
[ "$VIS" = 'PUBLIC' ] || fail REPOSITORIO_NO_PUBLICO
DEFAULT_BRANCH="$(gh repo view "$TARGET" --json defaultBranchRef --jq .defaultBranchRef.name 2>>"$LOG")" || fail SIN_RAMA
[ "$DEFAULT_BRANCH" = 'main' ] || fail RAMA_INESPERADA
cd "$WORK"
gh repo clone "$TARGET" repo -- --depth 1 >>"$LOG" 2>&1 || fail CLONACION_FALLIDA
REMOTE_URL="$(git -C repo remote get-url origin)"
case "$REMOTE_URL" in
  "https://github.com/$TARGET"|"https://github.com/$TARGET.git"|"git@github.com:$TARGET.git") ;;
  *) fail REMOTO_INCORRECTO ;;
esac
[ -z "$(git -C repo status --porcelain)" ] || fail REPO_NO_LIMPIO
FILES=(
  'app/src/main/java/bo/genai/usbpowerbridge/MainActivity.kt'
  'app/src/main/java/bo/genai/usbpowerbridge/SafeDocumentOps.kt'
  'app/src/main/java/bo/genai/usbpowerbridge/UsbProbe.kt'
  'app/src/main/java/bo/genai/usbpowerbridge/RealStorage.kt'
  'app/src/main/java/bo/genai/usbpowerbridge/TelemetryFormats.kt'
  'app/build.gradle.kts'
  'docs/TELEMETRIA_REAL_V0_5_0.md'
  'scripts/validar_telemetria_v05.py'
  'scripts/PUBLICAR_TELEMETRIA_V05_TERMUX.sh'
  'tests/TelemetryFormatsTest.kt'
)
for item in "${FILES[@]}"; do
  [ -f "$ROOT/$item" ] || fail "FUENTE_FALTANTE:$item"
  mkdir -p "repo/$(dirname "$item")"
  cp -- "$ROOT/$item" "repo/$item"
done
python3 - "$WORK/repo/README.md" "$WORK/repo/.github/workflows/android-debug.yml" "$ROOT/README.md" <<'PY' >>"$LOG" 2>&1
from pathlib import Path
import sys
readme,workflow,source=map(Path,sys.argv[1:4])
s=readme.read_text()
section=source.read_text()
start='## 📊 Panel de telemetría real V0.5.0'
end='## 📢 Comparte el repositorio'
assert end in section and start in section
new=section.split(start,1)[1].split(end,1)[0]
if start in s:
    assert end in s[s.index(start):]
    s=s[:s.index(start)]+start+new+s[s.index(end,s.index(start)):]
else:
    assert s.count(end)==1
    s=s.replace(end,start+new+end,1)
s=s.replace('v0.4.0-real-candidate','v0.5.0-real-candidate')
s=s.replace('## 🔌 Panel Android real · Conectar / Desconectar · v0.4.0','## 🔌 Panel Android real · Conectar / Desconectar · v0.5.0')
readme.write_text(s)
w=workflow.read_text()
assert 'android-actions/setup-android@v4' in w
needle='      - name: Compilar APK debug (NO release pública)'
step='''      - name: Validar panel real V0.5.0
        run: python3 scripts/validar_telemetria_v05.py
'''
if step not in w:
    assert w.count(needle)==1
    w=w.replace(needle,step+needle)
workflow.write_text(w)
PY
cd repo
git add -- README.md .github/workflows/android-debug.yml "${FILES[@]}" || fail GIT_ADD_FALLO
# Actualizar checksums solo de archivos rastreados dentro de OTG.
python3 - <<'PY' >>"$LOG" 2>&1
from pathlib import Path
import subprocess,hashlib
root=Path.cwd()
tracked=subprocess.check_output(['git','ls-files','-z']).decode().split('\0')
records=[]
for rel in sorted(x for x in tracked if x and x!='SHA256SUMS.txt'):
    p=root/rel
    if p.is_file(): records.append(f'{hashlib.sha256(p.read_bytes()).hexdigest()}  ./{rel}\n')
(root/'SHA256SUMS.txt').write_text(''.join(records),encoding='utf-8')
PY
git add -- SHA256SUMS.txt || fail SHA_LIST_FALLO
git diff --cached --check >"$WORK/diffcheck.log" 2>&1 || fail DIFF_INVALIDO
# Control de ruta explícito: ningún fichero fuera de este proyecto.
git diff --cached --name-only >"$WORK/paths.txt"
python3 - "$WORK/paths.txt" <<'PY' >>"$LOG" 2>&1
from pathlib import Path
import sys
allowed={
 'README.md','.github/workflows/android-debug.yml','SHA256SUMS.txt',
 'app/src/main/java/bo/genai/usbpowerbridge/MainActivity.kt',
 'app/src/main/java/bo/genai/usbpowerbridge/SafeDocumentOps.kt',
 'app/src/main/java/bo/genai/usbpowerbridge/UsbProbe.kt',
 'app/src/main/java/bo/genai/usbpowerbridge/RealStorage.kt',
 'app/src/main/java/bo/genai/usbpowerbridge/TelemetryFormats.kt',
 'app/build.gradle.kts','docs/TELEMETRIA_REAL_V0_5_0.md',
 'scripts/validar_telemetria_v05.py','scripts/PUBLICAR_TELEMETRIA_V05_TERMUX.sh',
 'tests/TelemetryFormatsTest.kt',
}
paths=Path(sys.argv[1]).read_text().splitlines()
assert set(paths)<=allowed, 'Cambio fuera de la lista permitida'
print('ARCHIVOS_PUBLICADOS=',len(paths))
PY
if git diff --cached --quiet; then
  echo 'PUBLICACION=YA_ACTUALIZADO'
  echo "REPO=https://github.com/$TARGET"
  exit 0
fi
git -c user.name='Luis Carlos Moreno Jiménez' \
 -c user.email='luis171987@users.noreply.github.com' \
 commit -m 'feat: telemetria real de energia, USB, almacenamiento y progreso' >>"$LOG" 2>&1 || fail COMMIT_FALLIDO
git push origin HEAD:refs/heads/main >>"$LOG" 2>&1 || fail PUSH_RECHAZADO
COMMIT="$(git rev-parse HEAD)"
REMOTE_HEAD="$(gh api "repos/$TARGET/commits/main" --jq .sha 2>>"$LOG")" || fail NO_SE_PUDO_VERIFICAR_REMOTO
[ "$COMMIT" = "$REMOTE_HEAD" ] || fail COMMIT_REMOTO_DIFERENTE
echo '=== OTG POWER BRIDGE | V0.5.0 ==='
echo 'PUBLICACION=PUSH_COMPLETADO'
echo "REPOSITORIO=$TARGET"
echo 'VISIBILIDAD=PUBLIC'
echo "COMMIT=${COMMIT:0:12}"
echo 'APK=CI_PENDIENTE'
echo 'OTROS_REPOSITORIOS=SIN_CAMBIOS'
echo "REGISTRO=$LOG"

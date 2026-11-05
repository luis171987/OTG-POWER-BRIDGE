#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail
umask 077
[ "${1:-}" = '--publicar' ] || { echo 'USO=bash scripts/PUBLICAR_UI_V06_TERMUX.sh --publicar'; exit 2; }
TARGET='luis171987/OTG-POWER-BRIDGE'
EXPECTED_HEAD='beed51f0e0c3'
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BASE="$HOME/OTG_POWER_BRIDGE"
mkdir -p "$BASE"
WORK="$(mktemp -d "$BASE/UI_V06_PUBLISH_XXXXXXXX")"
LOG="$WORK/publicacion.log"
fail(){ echo 'PUBLICACION=NO_COMPLETADA'; echo "MOTIVO=$1"; echo "REGISTRO=$LOG"; exit 1; }
for t in git gh python3; do command -v "$t" >/dev/null || fail "FALTA_$t"; done
python3 "$ROOT/scripts/validar_ui_v06.py" >"$WORK/tests.log" 2>&1 || fail VALIDACION_UI_FALLIDA
python3 "$ROOT/scripts/validar_modo_real.py" >>"$WORK/tests.log" 2>&1 || fail VALIDACION_REAL_FALLIDA
[ "$(gh api user --jq .login 2>>"$LOG")" = 'luis171987' ] || fail IDENTIDAD_GITHUB_INVALIDA
[ "$(gh repo view "$TARGET" --json nameWithOwner --jq .nameWithOwner 2>>"$LOG")" = "$TARGET" ] || fail REPOSITORIO_DIFERENTE
[ "$(gh repo view "$TARGET" --json visibility --jq .visibility 2>>"$LOG")" = 'PUBLIC' ] || fail VISIBILIDAD_INESPERADA
[ "$(gh repo view "$TARGET" --json defaultBranchRef --jq .defaultBranchRef.name 2>>"$LOG")" = 'main' ] || fail RAMA_INESPERADA
cd "$WORK"
gh repo clone "$TARGET" repo -- --depth 1 >>"$LOG" 2>&1 || fail CLONACION_FALLIDA
remote="$(git -C repo remote get-url origin)"
case "$remote" in
  "https://github.com/$TARGET"|"https://github.com/$TARGET.git"|"git@github.com:$TARGET.git") ;;
  *) fail REMOTO_INCORRECTO ;;
esac
[ -z "$(git -C repo status --porcelain)" ] || fail ARBOL_LOCAL_NO_LIMPIO
head="$(git -C repo rev-parse HEAD)"
case "$head" in "$EXPECTED_HEAD"*) ;; *) fail "CAMBIO_REMOTO_DESDE_V05:${head:0:12}" ;; esac
FILES=(
  'README.md'
  'app/build.gradle.kts'
  'app/src/main/java/bo/genai/usbpowerbridge/MainActivity.kt'
  'app/src/main/java/bo/genai/usbpowerbridge/SafeDocumentOps.kt'
  'app/src/main/java/bo/genai/usbpowerbridge/BridgeScreens.kt'
  'app/src/main/res/drawable-nodpi/brand_banner.webp'
  'assets/ui_aprobada/00_tablero_cyber_cockpit_aprobado.jpg'
  'assets/ui_aprobada/01_transferencias.jpg'
  'assets/ui_aprobada/02_almacenamiento.jpg'
  'assets/ui_aprobada/03_herramientas.jpg'
  'assets/ui_aprobada/04_ajustes.jpg'
  'docs/FRONTEND_CYBER_COCKPIT_V06.md'
  'scripts/validar_ui_v06.py'
  'scripts/PUBLICAR_UI_V06_TERMUX.sh'
  '.github/workflows/android-debug.yml'
)
for item in "${FILES[@]}"; do
  [ -f "$ROOT/$item" ] || fail "FUENTE_FALTANTE:$item"
  mkdir -p "repo/$(dirname "$item")"
  cp -- "$ROOT/$item" "repo/$item"
done
cd repo
git add -- "${FILES[@]}" || fail STAGING_FALLIDO
python3 - <<'PY' >>"$LOG" 2>&1
from pathlib import Path
import subprocess, hashlib
root=Path.cwd()
tracked=subprocess.check_output(['git','ls-files','-z']).decode().split('\0')
records=[]
for rel in sorted(i for i in tracked if i and i!='SHA256SUMS.txt'):
    if (root/rel).is_file():
        records.append(f'{hashlib.sha256((root/rel).read_bytes()).hexdigest()}  ./{rel}\n')
(root/'SHA256SUMS.txt').write_text(''.join(records))
PY
git add -- SHA256SUMS.txt || fail STAGING_HASH_FALLIDO
git diff --cached --check >"$WORK/diffcheck.log" 2>&1 || fail DIFF_INCORRECTO
git diff --cached --name-only >"$WORK/paths.txt"
python3 - "$WORK/paths.txt" "${FILES[@]}" <<'PY' >>"$LOG" 2>&1
from pathlib import Path
import sys
allowed=set(sys.argv[2:])|{'SHA256SUMS.txt'}
actual=set(Path(sys.argv[1]).read_text().splitlines())
assert actual<=allowed, f'Cambios no autorizados: {actual-allowed}'
print('ARCHIVOS_CAMBIADOS=',len(actual))
PY
if git diff --cached --quiet; then
  echo 'PUBLICACION=YA_ACTUALIZADA'
  exit 0
fi
git -c user.name='Luis Carlos Moreno Jiménez' \
  -c user.email='luis171987@users.noreply.github.com' \
  commit -m 'feat(ui): Cyber Cockpit nativo de cinco pantallas con datos reales' >>"$LOG" 2>&1 || fail COMMIT_FALLIDO
git push origin HEAD:refs/heads/main >>"$LOG" 2>&1 || fail PUSH_RECHAZADO
sha="$(git rev-parse HEAD)"
[ "$(gh api "repos/$TARGET/commits/main" --jq .sha 2>>"$LOG")" = "$sha" ] || fail VERIFICACION_REMOTA_FALLIDA
echo '=== OTG POWER BRIDGE V0.6.0 ==='
echo 'PUBLICACION=PUSH_COMPLETADO'
echo "COMMIT=${sha:0:12}"
echo "REPO=https://github.com/$TARGET"
echo 'APK=NO_PUBLICADO_CI_PENDIENTE'
echo 'OTROS_REPOSITORIOS=SIN_CAMBIOS'
echo "LOG=$LOG"

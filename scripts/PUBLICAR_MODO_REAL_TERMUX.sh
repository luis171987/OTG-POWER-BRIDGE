#!/usr/bin/env bash
set -Eeuo pipefail
umask 077

if [ "${1:-}" != "--publicar" ]; then
  echo "USO=bash scripts/PUBLICAR_MODO_REAL_TERMUX.sh --publicar"
  exit 2
fi
TARGET="luis171987/OTG-POWER-BRIDGE"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
HOME_SAFE="$HOME/OTG_POWER_BRIDGE"
mkdir -p "$HOME_SAFE"
WORK="$(mktemp -d "$HOME_SAFE/PUBLISH_REAL_XXXXXXXX")"
LOG="$WORK/publicacion.log"

fail() {
  echo "PUBLICACION=NO_COMPLETADA"
  echo "ERROR=$1"
  echo "REGISTRO=$LOG"
  exit 1
}

for tool in gh git python3; do
  command -v "$tool" >/dev/null 2>&1 || fail "FALTA_$tool"
done

python3 "$ROOT/scripts/validar_modo_real.py" >"$WORK/validacion.log" 2>&1 || fail "FUENTE_NO_VALIDADA"

IDENTITY="$(gh api user --jq .login 2>>"$LOG")" || fail "GITHUB_NO_AUTENTICADO"
[ "$IDENTITY" = "luis171987" ] || fail "GITHUB_CUENTA_NO_AUTORIZADA"

REMOTE="$(gh repo view "$TARGET" --json nameWithOwner --jq .nameWithOwner 2>>"$LOG")" || fail "REPO_INACCESIBLE"
[ "$REMOTE" = "$TARGET" ] || fail "REPOSITORIO_INCORRECTO"
VISIBILITY="$(gh repo view "$TARGET" --json visibility --jq .visibility 2>>"$LOG")" || fail "REPO_VISIBILIDAD_INACCESIBLE"
[ "$VISIBILITY" = "PUBLIC" ] || fail "REPOSITORIO_NO_PUBLICO"
BRANCH="$(gh repo view "$TARGET" --json defaultBranchRef --jq .defaultBranchRef.name 2>>"$LOG")" || fail "RAMA_NO_VERIFICADA"
[ "$BRANCH" = "main" ] || fail "RAMA_NO_ES_MAIN"

cd "$WORK"
gh repo clone "$TARGET" repo -- --depth 1 >>"$LOG" 2>&1 || fail "CLONACION_FALLIDA"
REMOTE_URL="$(git -C repo remote get-url origin)"
case "$REMOTE_URL" in
  "https://github.com/$TARGET"|"https://github.com/$TARGET.git"|"git@github.com:$TARGET.git") ;;
  *) fail "ORIGIN_NO_ES_EL_REPO_OTG" ;;
esac
[ -z "$(git -C repo status --porcelain)" ] || fail "CLON_NO_LIMPIO"

# Lista explícita de los archivos exclusivos del proyecto. No hay copias del $HOME.
FILES=(
  app/src/main/java/bo/genai/usbpowerbridge/MainActivity.kt
  app/src/main/java/bo/genai/usbpowerbridge/RealStorage.kt
  app/build.gradle.kts
  docs/MODO_REAL_ANDROID.md
  docs/ESTADO_Y_LIMITES.md
  README_BUILD_ES.md
  assets/05_diseno_desconectado_aprobado.png
  assets/06_diseno_conectado_aprobado.png
  scripts/validar_modo_real.py
  scripts/patch_readme.py
  scripts/build_termux.sh
  scripts/PUBLICAR_MODO_REAL_TERMUX.sh
)
for f in "${FILES[@]}"; do
  [ -f "$ROOT/$f" ] || fail "ARCHIVO_FALTANTE_EN_PAQUETE"
  mkdir -p "repo/$(dirname "$f")"
  cp "$ROOT/$f" "repo/$f"
done

python3 "$ROOT/scripts/patch_readme.py" "repo/README.md" "$ROOT/README.md" >>"$LOG" 2>&1 || fail "README_CAMBIO_NO_SEGURO"

# Eliminar solamente el simulador legado del repositorio de OTG.
git -C repo rm -r -q --ignore-unmatch -- sandbox app/src/main/java/bo/genai/core/ConnectionModel.kt >>"$LOG" 2>&1 || fail "ELIMINACION_LEGADO_FALLIDA"

# Preservar las correcciones previas de CI; añadir validación fuente real.
python3 - "$WORK/repo/.github/workflows/android-debug.yml" <<'PY' >>"$LOG" 2>&1
from pathlib import Path
import sys
p=Path(sys.argv[1]); t=p.read_text()
if 'android-actions/setup-android@v3' in t:
    raise SystemExit('Workflow remoto usa SDK v3: revisar antes de publicarlo')
needle='      - name: Compilar APK debug (NO release pública)'
step='''      - name: Verificar modo real sin motor de simulación
        run: python3 scripts/validar_modo_real.py
'''
if step not in t:
    if t.count(needle)!=1:
        raise SystemExit('Estructura del workflow remoto inesperada')
    t=t.replace(needle, step+needle)
p.write_text(t)
PY

cd repo
git add -- README.md .github/workflows/android-debug.yml "${FILES[@]}" || fail "GIT_ADD_FALLO"
# El proyecto heredado tiene SHA256SUMS.txt: actualizarlo tras retirar el simulador.
python3 - "$WORK/repo" <<'PYHASH' >>"$LOG" 2>&1
from pathlib import Path
import hashlib, subprocess, sys
root = Path(sys.argv[1])
paths = subprocess.check_output(['git','ls-files','-z'], cwd=root).decode().split('\0')
records=[]
for rel in sorted(p for p in paths if p and p != 'SHA256SUMS.txt'):
    file=root / rel
    if file.is_file():
        records.append(f'{hashlib.sha256(file.read_bytes()).hexdigest()}  ./{rel}\n')
(root/'SHA256SUMS.txt').write_text(''.join(records), encoding='utf-8')
PYHASH
git add -- SHA256SUMS.txt || fail "CHECKSUMS_NO_ACTUALIZADOS"
# Las eliminaciones ya están en el índice por git rm.
git diff --cached --check >"$WORK/diffcheck.log" 2>&1 || fail "DIFF_INCORRECTO"

git diff --cached --name-only >"$WORK/paths.txt"
python3 - "$WORK/paths.txt" <<'PY' >>"$LOG" 2>&1
from pathlib import Path
import sys
paths=[x for x in Path(sys.argv[1]).read_text().splitlines() if x]
allow={
 'README.md','.github/workflows/android-debug.yml','SHA256SUMS.txt',
 'app/src/main/java/bo/genai/usbpowerbridge/MainActivity.kt',
 'app/src/main/java/bo/genai/usbpowerbridge/RealStorage.kt',
 'app/src/main/java/bo/genai/core/ConnectionModel.kt',
 'app/build.gradle.kts','docs/MODO_REAL_ANDROID.md',
 'docs/ESTADO_Y_LIMITES.md','README_BUILD_ES.md',
 'assets/05_diseno_desconectado_aprobado.png',
 'assets/06_diseno_conectado_aprobado.png',
 'scripts/validar_modo_real.py','scripts/patch_readme.py',
 'scripts/build_termux.sh','scripts/PUBLICAR_MODO_REAL_TERMUX.sh'
}
if any(not (x in allow or x.startswith('sandbox/')) for x in paths):
    raise SystemExit('CAMBIO_FUERA_DEL_PROYECTO_PREVISTO')
print('ARCHIVOS_EN_COMMIT=', len(paths))
PY

if git diff --cached --quiet; then
  echo "PUBLICACION=YA_ACTUALIZADO"
  echo "REPO=https://github.com/$TARGET"
  echo "LOG=$LOG"
  exit 0
fi

git -c user.name='Luis Carlos Moreno Jiménez' \
  -c user.email='luis171987@users.noreply.github.com' \
  commit -m 'feat: panel OTG real con conectar, desconectar y SAF' >>"$LOG" 2>&1 || fail "COMMIT_FALLIDO"
git push origin HEAD:refs/heads/main >>"$LOG" 2>&1 || fail "PUSH_RECHAZADO_O_AUTENTICACION"
COMMIT="$(git rev-parse HEAD)"
HEAD_REMOTE="$(gh api "repos/$TARGET/commits/main" --jq .sha 2>>"$LOG")" || fail "NO_VERIFICABLE_REMOTAMENTE"
[ "$COMMIT" = "$HEAD_REMOTE" ] || fail "HEAD_REMOTO_NO_COINCIDE"

echo "=== OTG POWER BRIDGE | PUBLICACION ==="
echo "PUBLICACION=PUSH_COMPLETADO"
echo "REPOSITORIO=$TARGET"
echo "VISIBILIDAD=PUBLIC"
echo "COMMIT=${COMMIT:0:12}"
echo "APK=CI_PENDIENTE"
echo "OTROS_REPOSITORIOS=SIN_CAMBIOS"
echo "REGISTRO=$LOG"

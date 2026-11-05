#!/data/data/com.termux/files/usr/bin/bash
# OTG POWER BRIDGE - publicación conservadora (cuenta autorizada exclusivamente).
set -Eeuo pipefail
umask 077
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OWNER='luis171987'
NAME='OTG-POWER-BRIDGE'
TARGET="$OWNER/$NAME"
DESC='OTG POWER BRIDGE — app Android de GENAI INDUSTRIES: diagnóstico de carga y USB, archivos mediante SAF y tutorial visual. Prototipo.'
[[ "${1:-}" == '--publicar' ]] || { echo 'MODO=SOLO_LECTURA'; echo 'USO=bash scripts/PUBLICAR_GITHUB_TERMUX.sh --publicar'; exit 0; }
for dep in git gh; do command -v "$dep" >/dev/null 2>&1 || { echo "ERROR=FALTA_$dep"; exit 3; }; done
gh auth status >/dev/null 2>&1 || { echo 'ERROR=GITHUB_NO_AUTENTICADO (ejecutar gh auth login)'; exit 4; }
ACTUAL="$(gh api user --jq .login 2>/dev/null)" || { echo 'ERROR=GITHUB_API_NO_DISPONIBLE'; exit 4; }
[[ "$ACTUAL" == "$OWNER" ]] || { echo "ERROR=CUENTA_DISTINTA ($ACTUAL); SE_REQUIERE=$OWNER"; exit 5; }
[[ -f "$ROOT/assets/SOCIAL_PREVIEW_OTG_POWER_BRIDGE_1280x640.jpg" ]] || { echo 'ERROR=FALTA_IMAGEN_SOCIAL'; exit 5; }
[[ -f "$ROOT/README.md" ]] || { echo 'ERROR=FALTA_README'; exit 5; }
# Únicamente archivos públicos previstos. Nada de fotos privadas, logs, APK inexistente ni credenciales.
PUBLIC_FILES=( README.md README_BUILD_ES.md README_PUBLICAR_ES.md .gitignore build.gradle.kts settings.gradle.kts gradle.properties SHA256SUMS.txt assets app docs sandbox scripts .github )
for f in "${PUBLIC_FILES[@]}"; do [[ -e "$ROOT/$f" ]] || { echo "ERROR=ARCHIVO_FALTANTE:$f"; exit 5; }; done
if [[ -n "$(find "$ROOT" -type f \( -name '*.jks' -o -name '*.keystore' -o -name '*.pem' -o -name '*.p12' -o -name '.env' -o -name 'id_rsa' \) -print -quit)" ]]; then
  echo 'ERROR=MATERIAL_SECRETO_DETECTADO'; exit 6
fi
GIT_ID="$(gh api user --jq .id 2>/dev/null)" || exit 5
SET_IDENTITY() {
  git config user.name 'Luis Carlos Moreno Jiménez'
  git config user.email "${GIT_ID}+${OWNER}@users.noreply.github.com"
}
# Si existe, se actualiza sin borrar otros archivos o el historial remoto.
if gh repo view "$TARGET" --json nameWithOwner >/dev/null 2>&1; then
  VIS="$(gh repo view "$TARGET" --json visibility --jq .visibility)"
  [[ "$VIS" == 'PUBLIC' ]] || { echo 'ERROR=EL_REPOSITORIO_EXISTENTE_NO_ES_PUBLICO'; exit 7; }
  TMP="$(mktemp -d "${TMPDIR:-$HOME}/otg_publicar_XXXXXXXX")"
  trap 'rm -rf "$TMP"' EXIT
  gh repo clone "$TARGET" "$TMP/work" -- --quiet >/dev/null
  cd "$TMP/work"
  SET_IDENTITY
  DEFAULT="$(gh repo view "$TARGET" --json defaultBranchRef --jq '.defaultBranchRef.name')"
  [[ -n "$DEFAULT" && "$DEFAULT" != 'null' ]] || { echo 'ERROR=REPO_SIN_RAMA_PRINCIPAL'; exit 8; }
  git checkout "$DEFAULT" >/dev/null 2>&1 || { echo 'ERROR=RAMA_PRINCIPAL_NO_DISPONIBLE'; exit 8; }
  for f in "${PUBLIC_FILES[@]}"; do
    if [[ -d "$ROOT/$f" ]]; then
      mkdir -p "$f"; cp -R "$ROOT/$f/." "$f/"
    else
      mkdir -p "$(dirname "$f")"; cp "$ROOT/$f" "$f"
    fi
  done
  git add -- "${PUBLIC_FILES[@]}"
  if git diff --cached --quiet; then echo 'GITHUB=PUBLICO_SIN_CAMBIOS'; echo "REPO=https://github.com/$TARGET"; exit 0; fi
  git commit -m 'docs: logo social, tutorial e identidad OTG POWER BRIDGE' >/dev/null
  if git push origin "HEAD:$DEFAULT" >/dev/null 2>&1; then
    echo 'GITHUB=ACTUALIZADO_FAST_FORWARD'
  else
    # Si hay una regla de protección en main, enviar rama/PR sin forzar.
    BRANCH="actualizar-otg-social-$(date +%Y%m%d-%H%M%S)"
    git push -u origin "HEAD:refs/heads/$BRANCH" >/dev/null || { echo 'ERROR=PUSH_NO_AUTORIZADO'; exit 9; }
    gh pr create --repo "$TARGET" --base "$DEFAULT" --head "$BRANCH" --title 'OTG POWER BRIDGE: identidad y tutorial público' --body 'Actualiza logo oficial, imagen Social Preview, documentación, app y tutorial. No incluye APK de producción.' >/dev/null
    echo 'GITHUB=PR_CREADA_PENDIENTE_DE_FUSION'
  fi
else
  # El error al consultar no certifica inexistencia; API lo resolverá al crear o rechazará sin modificar otros repos.
  [[ ! -d "$ROOT/.git" ]] || { echo 'ERROR=HISTORIAL_GIT_LOCAL_EXISTENTE'; exit 10; }
  cd "$ROOT"
  git init -q -b main
  SET_IDENTITY
  git add -- "${PUBLIC_FILES[@]}"
  [[ -n "$(git diff --cached --name-only)" ]] || { echo 'ERROR=SIN_ARCHIVOS_PUBLICABLES'; exit 10; }
  git commit -qm 'feat: OTG POWER BRIDGE preview Android y tutorial visual oficial'
  gh repo create "$TARGET" --public --description "$DESC" --source=. --remote=origin --push >/dev/null || {
    echo 'ERROR=CREACION_O_PUSH_FALLIDO (revisar salida de gh y estado remoto)'; exit 11;
  }
  echo 'GITHUB=REPOSITORIO_PUBLICO_CREADO'
fi
# La descripción ayuda a la vista previa del enlace.
gh repo edit "$TARGET" --description "$DESC" >/dev/null 2>&1 || true
echo "REPO=https://github.com/$TARGET"
echo "SOCIAL_PREVIEW=SUBIR_MANUALMENTE_EN_SETTINGS_SOCIAL_PREVIEW"
echo 'APK_PUBLICADO=NO; COMPILACION_Y_PRUEBAS_PENDIENTES'

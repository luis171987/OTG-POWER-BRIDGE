#!/usr/bin/env python3
"""Suma sección real sin sobrescribir otros cambios del README remoto."""
from pathlib import Path
import sys

if len(sys.argv) != 3:
    raise SystemExit('Uso: patch_readme.py README_REMOTO README_DE_ESTA_VERSION')
remote, reference = map(Path, sys.argv[1:])
text = remote.read_text(encoding='utf-8')
ref = reference.read_text(encoding='utf-8')
start = '<!-- OTG:REAL:START -->'
end = '<!-- OTG:REAL:END -->'
if ref.count(start) != 1 or ref.count(end) != 1:
    raise SystemExit('Referencia del README sin sección real única')
section = start + ref.split(start, 1)[1].split(end, 1)[0] + end
if start in text or end in text:
    if text.count(start) != 1 or text.count(end) != 1:
        raise SystemExit('Marcadores REAL inconsistentes en README remoto')
    a, tail = text.split(start, 1)
    _, b = tail.split(end, 1)
    text = a + section + b
else:
    before = '## 📢 Comparte el repositorio en WhatsApp y Facebook'
    if before not in text:
        raise SystemExit('README remoto cambió; no se sobrescribirá')
    text = text.replace(before, section + '\n\n' + before, 1)
text = text.replace(
    'prototipo Android `v0.3.1-preview` · código fuente disponible · **APK público todavía no publicado**',
    'código fuente `v0.4.0-real-candidate` · interfaz sin simulador · **APK público de lanzamiento todavía no publicado**'
)
text = text.replace(
    'abre OTG POWER BRIDGE, cambia del modo de simulación al modo real y comprueba',
    'abre OTG POWER BRIDGE y comprueba'
)
text = text.replace('selecciona **Autorizar carpeta de la memoria USB**', 'pulsa **Conectar**')
text = text.replace('en modo real, habilita los cambios de archivos;', 'habilita los cambios de archivos;')
text = text.replace('**Copiar archivo del teléfono a la carpeta autorizada (SHA-256)**', '**Copiar archivo a carpeta · SHA-256**')
text = text.replace(
    '**Esta función aún requiere compilación y pruebas en un teléfono real.**',
    '**El código 0.4.0 debe compilarse y validarse con archivos de prueba en un teléfono real.**'
)
remote.write_text(text, encoding='utf-8')

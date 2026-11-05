#!/usr/bin/env python3
"""Revisión estática offline: NO equivale a compilación ni a prueba en teléfono."""
from pathlib import Path
import sys
import xml.etree.ElementTree as ET
root = Path(__file__).resolve().parents[1]
manifest = root / "app/src/main/AndroidManifest.xml"
ET.parse(manifest)
raw = manifest.read_text(encoding="utf-8")
main = (root / "app/src/main/java/bo/genai/usbpowerbridge/MainActivity.kt").read_text(encoding="utf-8")
probe = (root / "app/src/main/java/bo/genai/usbpowerbridge/UsbProbe.kt").read_text(encoding="utf-8")
ops = (root / "app/src/main/java/bo/genai/usbpowerbridge/SafeDocumentOps.kt").read_text(encoding="utf-8")
checks = {
  "sin_acceso_global": "MANAGE_EXTERNAL_STORAGE" not in raw and "READ_EXTERNAL_STORAGE" not in raw,
  "sin_red": "INTERNET" not in raw,
  "sin_root": "su -c" not in main,
  "sandbox_por_defecto": "mutableStateOf(true)" in main,
  "cambios_desactivados_inicio": "var writeEnabled by remember { mutableStateOf(false) }" in main,
  "diagnostico_usb": "object UsbProbe" in probe and "UsbManager" in probe,
  "SAF": "OpenDocumentTree" in main,
  "copia_verificada": "MessageDigest.isEqual" in ops,
  "editor_utf8_estricto": "onMalformedInput(CodingErrorAction.REPORT)" in ops,
  "APK_no_incluido": not any(root.rglob("*.apk")),
}
for key, value in checks.items():
    print(f"{key}={'PASS' if value else 'FAIL'}")
print(f"RESULTADO={sum(checks.values())}/{len(checks)} (solo estático)")
sys.exit(0 if all(checks.values()) else 1)

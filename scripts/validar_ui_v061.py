#!/usr/bin/env python3
"""Validaciones estaticas: NO sustituyen compilar, ejecutar ni probar OTG fisico."""
from pathlib import Path
import hashlib
import sys

r=Path(__file__).resolve().parents[1]
app=r/'app/src/main/java/bo/genai/usbpowerbridge'
ui=(app/'BridgeScreens.kt').read_text(encoding='utf-8')
main=(app/'MainActivity.kt').read_text(encoding='utf-8')
safe=(app/'SafeDocumentOps.kt').read_text(encoding='utf-8')
gradle=(r/'app/build.gradle.kts').read_text(encoding='utf-8')
ci=(r/'.github/workflows/android-debug.yml').read_text(encoding='utf-8')
resource=r/'app/src/main/res/drawable-nodpi/cyber_cockpit_backdrop.webp'
checks = {
  'version_code_separado': 'versionCode = 8' in gradle and '0.6.1-cyber-cockpit-candidate' in gradle,
  'instalacion_preview_paralela': 'applicationIdSuffix = ".preview"' in gradle,
  'cinco_pantallas_reales': all(f'Page.{p}' in ui for p in ('HOME','TRANSFER','STORAGE','TOOLS','SETTINGS')),
  'transiciones_nativas': 'AnimatedContent(' in ui and 'slideInHorizontally' in ui and 'slideOutHorizontally' in ui,
  'preferencias_de_movimiento': 'getSharedPreferences("otg_visual_preferences"' in ui,
  'fondo_optimo_nativo': resource.is_file() and resource.stat().st_size < 500000,
  'bisel_y_pantalla': all(x in ui for x in ('private fun NeonPanel','private fun BrandHero','private fun CyberMiniMonitor')),
  'medidores_dinamicos': 'private fun CircleGauge' in ui and 'animateFloatAsState' in ui,
  'flujo_sin_datos_simulados': 'val dataActive = data.usb.devices.isNotEmpty() || data.removable' in ui and 'val powerActive = data.power.externalPower' in ui,
  'transferencia_vinculada_telemetria': 'TelemetryFormats.percent(it.copiedBytes, it.totalBytes)' in ui,
  'potencia_no_fingida': 'Sin telemetría' in ui and 'data.power.percent' in ui,
  'api_android_usb': 'UsbProbe.read(context)' in main,
  'bateria_android': 'Intent.ACTION_BATTERY_CHANGED' in main,
  'saf_documentos': 'ActivityResultContracts.OpenDocumentTree()' in main,
  'copia_sha256_preservada': 'SafeDocumentOps.copyWithVerification' in main and 'MessageDigest.isEqual' in safe,
  'sin_datos_demo': 'SIMULADOR LÓGICO' not in ui+main and 'POWER_BANK_FAKE' not in ui,
  'proyecto_aislado': 'bo.genai.usbpowerbridge' in ui and 'vigilant' not in ui.lower(),
  'ci_android': 'android-actions/setup-android@v4' in ci and 'scripts/validar_ui_v061.py' in ci,
  'artefacto_distinto_v061': 'OTG-POWER-BRIDGE-v061-debug-candidato' in ci,
  'logo_oficial_preservado': (r/'app/src/main/res/drawable-nodpi/brand_banner.webp').is_file(),
}
for k,v in checks.items(): print(f'{k}={"OK" if v else "FAIL"}')
print(f'CHECKS={sum(checks.values())}/{len(checks)}')
print('TEST_TIPO=ESTATICO_NO_COMPILACION')
sys.exit(0 if all(checks.values()) else 1)

#!/usr/bin/env python3
"""Regresiones estáticas V0.6.3. No acreditan instalación ni compatibilidad USB física."""
from pathlib import Path
import sys

root = Path(__file__).resolve().parents[1]
ui = (root / 'app/src/main/java/bo/genai/usbpowerbridge/BridgeScreens.kt').read_text(encoding='utf-8')
gradle = (root / 'app/build.gradle.kts').read_text(encoding='utf-8')
ci = (root / '.github/workflows/android-debug.yml').read_text(encoding='utf-8')
assets = root / 'app/src/main/res/drawable-nodpi'

def section(a, b):
    return ui.split(a, 1)[1].split(b, 1)[0]

gauge = section('private fun CircleGauge(', 'private fun LiveDiagram(')
diagram = section('private fun LiveDiagram(', 'private fun InstrumentCell(')
home = section('private fun HomePage(', 'private fun CyberMiniMonitor(')
storage = section('private fun StoragePage(', 'private fun ToolsPage(')
checks = {
    'version_code_10': 'versionCode = 10' in gradle,
    'version_name_v063': '0.6.3-cyber-cockpit-precision-candidate' in gradle,
    'paquete_preview_independiente': 'applicationIdSuffix = ".previewv063"' in gradle,
    'cinco_pantallas': all('Page.' + x in ui for x in ('HOME', 'TRANSFER', 'STORAGE', 'TOOLS', 'SETTINGS')),
    'navegacion_animada': 'AnimatedContent(' in ui and 'slideInHorizontally' in ui,
    'gauge_responsivo': 'Box(modifier, contentAlignment = Alignment.Center)' in gauge,
    'gauge_sin_tamano_fijo_interno': 'Box(modifier.size(132.dp)' not in gauge,
    'gauge_compacto': 'compact: Boolean = false' in gauge and 'if (compact) 21.sp' in gauge,
    'gauges_reducidos': 'Modifier.size(81.dp), compact = true' in ui,
    'almacenamiento_ocupado': '"$storagePercent% OCUPADO"' in home,
    'almacenamiento_libre': 'libres en datos' in home and 'phone.available' in home,
    'storage_porcentaje_ocupado': 'ESPACIO OCUPADO:' in ui,
    'storage_statfs': 'StatFs(context.filesDir.absolutePath)' in ui,
    'diagrama_en_columna': 'Column(Modifier.fillMaxWidth()' in diagram,
    'diagrama_nodos_separados': 'Box(Modifier.fillMaxWidth().height(132.dp))' in diagram,
    'diagrama_nota_externa': 'ESQUEMA ILUSTRATIVO' in diagram and 'verticalArrangement = Arrangement.spacedBy(8.dp)' in diagram,
    'nodos_flexibles': 'CyberNode(' in diagram and 'Modifier.weight(1f)' in diagram,
    'sin_reporte_power_bank_inventado': 'SIN TELEMETRÍA' in home,
    'no_afirma_ruta_hub': 'no confirma el recorrido' in diagram,
    'flujos_condicionados': 'if (animated && usbObserved)' in diagram and 'if (animated && data.simultaneous)' in diagram,
    'telemetria_bateria': 'data.power.percent' in ui,
    'usb_sistema': 'data.usb.devices.size' in ui,
    'transferencia_bytes_reales': 'tx.copiedBytes' in ui and 'SystemClock.elapsedRealtime()' in ui,
    'transferencia_sin_simulaciones': 'SIN MUESTRAS' in ui,
    'SAF_conexion_real': 'actions.connect' in ui and 'actions.disconnect' in ui,
    'SHA256_funcional': 'actions.checksum' in ui,
    'sin_root_ni_permisos_excesivos': 'No hay root, ADB ni permiso global' in ui,
    'recursos_v062_preservados': all((assets / x).is_file() for x in [
        'cyber_hero_v062.webp', 'cyber_otg_core_v062.webp', 'cyber_cockpit_backdrop.webp',
        *(f'cyber_nav_{p}_v062.webp' for p in ('home','transfer','storage','tools','settings'))]),
    'CI_validaciones_v063': 'validar_ui_v063.py' in ci and 'validar_modo_real.py' in ci,
    'CI_artifact_preview': 'OTG-POWER-BRIDGE-v063-debug-candidato' in ci,
    'CI_sin_release': 'upload-artifact@v4' in ci and 'gh release' not in ci.lower() and 'create-release' not in ci.lower(),
    'auth_por_pr': 'pull_request:' in ci,
    'sin_datos_ficticios': 'POTENCIA 2.38' not in ui and '10,000 mAh' not in ui,
    'accesibilidad_navegacion': 'contentDescription = "Ir a ${page.caption}"' in ui,
    'identidad_oficial': 'GENAI INDUSTRIES' in ui and 'OTG POWER BRIDGE' in ui,
}
if (root / 'app/src/main/java/bo/genai/usbpowerbridge/MainActivity.kt').exists():
    main = (root / 'app/src/main/java/bo/genai/usbpowerbridge/MainActivity.kt').read_text()
    checks['nucleo_android_real'] = 'UsbProbe.read(context)' in main
if (root / 'app/src/main/java/bo/genai/usbpowerbridge/SafeDocumentOps.kt').exists():
    safe = (root / 'app/src/main/java/bo/genai/usbpowerbridge/SafeDocumentOps.kt').read_text()
    checks['integridad_SAF'] = 'MessageDigest.isEqual' in safe
for name, value in checks.items():
    print(f'{name}={"OK" if value else "FALLO"}')
print(f'CHECKS={sum(checks.values())}/{len(checks)}')
print('ALCANCE=ESTATICO_NO_COMPILACION_NI_PRUEBA_USB')
sys.exit(0 if all(checks.values()) else 1)

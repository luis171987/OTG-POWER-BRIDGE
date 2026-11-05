#!/usr/bin/env python3
"""Comprobaciones estáticas de la UI V0.6.2. No sustituyen compilación ni pruebas físicas."""
from pathlib import Path
import sys
root=Path(__file__).resolve().parents[1]
ui=(root/'app/src/main/java/bo/genai/usbpowerbridge/BridgeScreens.kt').read_text()
gradle=(root/'app/build.gradle.kts').read_text()
workflow=(root/'.github/workflows/android-debug.yml').read_text()
assets=root/'app/src/main/res/drawable-nodpi'
art=['cyber_hero_v062.webp','cyber_otg_core_v062.webp']+[f'cyber_nav_{p}_v062.webp' for p in ('home','transfer','storage','tools','settings')]
checks={
    'version_v062':'versionCode = 9' in gradle and '0.6.2-cyber-cockpit-fidelity-candidate' in gradle,
    'paquete_preview_independiente':'applicationIdSuffix = ".previewv062"' in gradle,
    'frontend_compose_vivo':'internal fun BridgeInterface(data: BridgeUiData, actions: BridgeActions)' in ui,
    'cinco_pantallas':all('Page.'+x in ui for x in ('HOME','TRANSFER','STORAGE','TOOLS','SETTINGS')),
    'navegacion_por_imagen_aprobada':all('R.drawable.cyber_nav_'+p+'_v062' in ui for p in ('home','transfer','storage','tools','settings')),
    'encabezado_cyber_aprobado':'R.drawable.cyber_hero_v062' in ui,
    'componentes_interactivos':'private fun NeonButton' in ui and '.clickable' in ui,
    'panel_3d_vectorial':'private fun NeonPanel' in ui and 'StrokeJoin.Round' in ui,
    'gauge_telemetria':'private fun CircleGauge' in ui and 'data.power.percent' in ui,
    'velocidad_real':'private fun TransferSpeedPlot' in ui and 'tx.copiedBytes' in ui and 'SystemClock.elapsedRealtime()' in ui,
    'sin_telemetria_bank':'SIN TELEMETRÍA' in ui,
    'animaciones_solo_estado_real':'if (animated && dataActive)' in ui and 'if (animated && powerActive)' in ui,
    'transiciones_paginas':'AnimatedContent(' in ui and 'slideInHorizontally' in ui,
    'procedimiento_saf':'actions.connect' in ui and 'actions.disconnect' in ui,
    'operaciones_integridad':'actions.checksum' in ui and 'TransferSummary' in ui,
    'artes_presentes':all((assets/x).is_file() for x in art),
    'artes_optimizadas':all((assets/x).stat().st_size<300000 for x in art),
    'fondo_existente':(assets/'cyber_cockpit_backdrop.webp').is_file(),
    'ci_rama_pr':'pull_request:' in workflow and 'validar_ui_v062.py' in workflow,
    'ci_artefacto_separado':'OTG-POWER-BRIDGE-v062-debug-candidato' in workflow,
    'api_real_preservada':not (root/'app/src/main/java/bo/genai/usbpowerbridge/MainActivity.kt').exists() or 'UsbProbe.read(context)' in (root/'app/src/main/java/bo/genai/usbpowerbridge/MainActivity.kt').read_text(),
    'saf_real_preservado':not (root/'app/src/main/java/bo/genai/usbpowerbridge/SafeDocumentOps.kt').exists() or 'MessageDigest.isEqual' in (root/'app/src/main/java/bo/genai/usbpowerbridge/SafeDocumentOps.kt').read_text(),
    'sin_touch_otros_proyectos':'VIGILANT' not in str(root),
}
for key,ok in checks.items():print(f'{key}={"OK" if ok else "FALLO"}')
print(f'CHECKS={sum(checks.values())}/{len(checks)}')
print('TEST=ESTATICO_NO_COMPILACION')
sys.exit(0 if all(checks.values()) else 1)

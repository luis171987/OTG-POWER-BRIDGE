#!/usr/bin/env python3
"""Verificación estática del frontend Android sin ejecutar ni simular hardware."""
from pathlib import Path
import sys
base=Path(__file__).resolve().parents[1]
source=base/'app/src/main/java/bo/genai/usbpowerbridge'
main=(source/'MainActivity.kt').read_text()
ui=(source/'BridgeScreens.kt').read_text()
safe=(source/'SafeDocumentOps.kt').read_text()
manifest=(base/'app/src/main/AndroidManifest.xml').read_text()
workflow=(base/'.github/workflows/android-debug.yml').read_text()
build=(base/'app/build.gradle.kts').read_text()
checks={
 'cinco_pantallas':all(f'Page.{p}' in ui for p in ('HOME','TRANSFER','STORAGE','TOOLS','SETTINGS')),
 'menu_funcional':'BottomNavigation(currentPage)' in ui and 'currentPage = it' in ui,
 'logo_oficial':(base/'app/src/main/res/drawable-nodpi/brand_banner.webp').exists(),
 'tema_neon_escalable':'Brush.verticalGradient' in ui and 'Canvas(' in ui,
 'conectar_saf':'ActivityResultContracts.OpenDocumentTree()' in main and 'folderPicker.launch(null)' in main,
 'desconectar_sesion':'fun closeSession()' in main and 'releasePersistableUriPermission' in main,
 'bateria_real':'Intent.ACTION_BATTERY_CHANGED' in main,
 'usb_real':'UsbProbe.read(context)' in main,
 'no_porcentaje_falso':'No disponible' in ui and 'Power bank (%)' in ui,
 'transferencia_real':'SafeDocumentOps.copyWithVerification' in main and 'progress = TransferDisplay' not in main,
 'checksum_real':'fun checksum(' in safe and 'resolver.openInputStream(file.uri)' in safe,
 'copia_con_sha256':'MessageDigest.isEqual(originalHash.digest(), copiedHash.digest())' in safe,
 'respaldo_eliminacion':'deleteAfterBackup' in safe and 'Confirmar eliminación' in main,
 'preferencias_locales':'getSharedPreferences(' in ui,
 'sin_motor_mock':'SIMULADOR LÓGICO' not in main+ui and 'ConnectionModel' not in main+ui,
 'version_06':'versionCode = 7' in build and '0.6.0-visual-candidate' in build,
 'paquete_propio':'bo.genai.usbpowerbridge' in manifest or '.MainActivity' in manifest,
 'ci_android_v4':'android-actions/setup-android@v4' in workflow,
 'ci_validacion_nueva':'scripts/validar_ui_v06.py' in workflow,
 'cinco_imagenes':all((base/f'assets/ui_aprobada/{name}').is_file() for name in (
  '00_tablero_cyber_cockpit_aprobado.jpg','01_transferencias.jpg','02_almacenamiento.jpg',
  '03_herramientas.jpg','04_ajustes.jpg')),
 'sin_version_release_falsa':'APK V0.6.0 todavía no compilado ni publicado' in (base/'README.md').read_text(),
}
for name, ok in checks.items():
    print(f'{name}={"OK" if ok else "FAIL"}')
print(f'CHECKS={sum(checks.values())}/{len(checks)}')
sys.exit(0 if all(checks.values()) else 1)

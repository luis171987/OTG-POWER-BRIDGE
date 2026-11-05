from pathlib import Path
import sys
root = Path(__file__).resolve().parent.parent
src = (root / 'app/src/main/java/bo/genai/usbpowerbridge/MainActivity.kt').read_text()
manifest = (root / 'app/src/main/AndroidManifest.xml').read_text()
readme = (root / 'README.md').read_text()
checks = {
    'no_mock_engine': not (root / 'app/src/main/java/bo/genai/core/ConnectionModel.kt').exists(),
    'no_sandbox_folder': not (root / 'sandbox').exists(),
    'connect_action_real': 'ActivityResultContracts.OpenDocumentTree()' in src and 'folderPicker.launch(null)' in src,
    'disconnect_session': 'fun closeSession()' in src,
    'no_simulation_toggle': 'var demo by remember' not in src and 'SIMULADOR LÓGICO' not in src,
    'battery_system': 'Intent.ACTION_BATTERY_CHANGED' in src,
    'usb_system': 'UsbProbe.read(context)' in src,
    'saf_copy_sha256': 'SafeDocumentOps.copyWithVerification' in src,
    'physical_limit_documented': 'no activa físicamente USB-PD' in readme,
    'brand_applied': 'OTG POWER BRIDGE' in manifest and (root / 'app/src/main/res/drawable-nodpi/brand_logo.webp').is_file(),
}
for key, value in checks.items():
    print(f'{key}={"OK" if value else "FAIL"}')
print(f'CHECKS={sum(checks.values())}/{len(checks)}')
sys.exit(0 if all(checks.values()) else 1)

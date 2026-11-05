from pathlib import Path
import sys
root = Path(__file__).resolve().parents[1]
files = {n: (root / f'app/src/main/java/bo/genai/usbpowerbridge/{n}').read_text() for n in ['MainActivity.kt','UsbProbe.kt','RealStorage.kt','SafeDocumentOps.kt','TelemetryFormats.kt']}
main=files['MainActivity.kt']; usb=files['UsbProbe.kt']; storage=files['RealStorage.kt']; ops=files['SafeDocumentOps.kt']; fmt=files['TelemetryFormats.kt']
checks={
 'real_phone_battery': 'Intent.ACTION_BATTERY_CHANGED' in main,
 'no_false_bank_percent': '—%  ·  No disponible' in main and 'power bank no comunica su batería' in main,
 'actual_usb_id': all(x in usb for x in ['manufacturerName','productName','vendorId','productId']),
 'volume_statfs': all(x in storage for x in ['StorageManager','StatFs','availableBytes']),
 'unknown_capacity_fallback': 'No disponible' in fmt,
 'live_transfer_callback': 'progress?.invoke(written, total)' in ops,
 'sha256_verification': 'MessageDigest.isEqual(originalHash.digest(), copiedHash.digest())' in ops,
 'partial_rollback': 'created.delete()' in ops,
 'progress_ui': 'PROGRESO DE TRANSFERENCIA REAL' in main and 'LinearProgressIndicator' in main,
 'saf_connection': 'ActivityResultContracts.OpenDocumentTree()' in main,
 'no_fake_simulator': 'SIMULADOR LÓGICO' not in main and not (root/'sandbox').exists(),
 'new_version': 'versionCode = 6' in (root/'app/build.gradle.kts').read_text(),
}
for k,v in checks.items(): print(f'{k}={"OK" if v else "FALLO"}')
print(f'CHECKS={sum(checks.values())}/{len(checks)}')
sys.exit(0 if all(checks.values()) else 1)

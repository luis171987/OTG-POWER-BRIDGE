# OTG POWER BRIDGE V0.5.0 — telemetría verificada

- Teléfono: `ACTION_BATTERY_CHANGED` entrega nivel y origen de alimentación; la actualización depende del sistema Android.
- Power bank: **no hay API universal** que publique el porcentaje por el cable de alimentación. Si el fabricante no proporciona protocolo de telemetría compatible, la app mostrará **No disponible**; no inferir desde el porcentaje del teléfono ni desde LEDs.
- USB: `UsbManager.deviceList` puede publicar VID/PID, fabricante, producto y descriptor de almacenamiento. El controlador del teléfono puede ocultar unidades montadas al API de USB host.
- Almacenamiento: `StorageManager` intenta relacionar el volumen SAF con su UUID; desde Android 11 consulta `StorageVolume.directory` y `StatFs` cuando sea posible. A falta de acceso, muestra **No disponible**. Un medio extraíble puede ser USB o microSD.
- Conector físico USB-A, USB-C o micro-USB: Android normalmente no expone el tipo del conector de la memoria; no inventarlo.
- Progreso: bytes copiados / tamaño comunicado por proveedor SAF; si el tamaño no es accesible, indicador indeterminado. El resultado **verificado** solo aparece después de comparar SHA-256 de origen y copia. Cualquier fallo intenta eliminar únicamente el destino parcial.
- Botones Conectar y Desconectar: administran una autorización SAF; no actúan sobre el suministro PD ni expulsan dispositivos físicamente.
- **Pruebas de hardware pendientes:** carga y OTG simultáneos, transferencia con un archivo prescindible, múltiples marcas/volúmenes, desconexión accidental, SAF restringido.

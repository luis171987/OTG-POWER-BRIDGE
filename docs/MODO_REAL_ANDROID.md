# OTG POWER BRIDGE 0.4.0 — Funcionamiento REAL (sin simulación)

**Desarrollador:** GENAI INDUSTRIES — Luis Carlos Moreno Jiménez.

## Qué hace la aplicación

- **Conectar:** abre el selector de carpetas de Android. Cuando el usuario autoriza una carpeta real, la app comprueba que sea legible y habilita un explorador de documentos. No fuerza una conexión eléctrica USB.
- **Desconectar:** cierra la sesión de archivos y solicita liberar los permisos SAF persistidos que obtuvo. No apaga el hub, no interrumpe la carga y **no expulsa** físicamente la memoria.
- **Indicadores:** consulta el porcentaje de batería y las señales de alimentación/carga del sistema Android. Lee USB Host y los dispositivos enumerables con `UsbManager`; una memoria montada puede no figurar en esta lista.
- **Almacenamiento:** la autorización mediante `OpenDocumentTree` es real. Para distinguir almacenamiento primario de volúmenes extraíbles, consulta el identificador SAF y la lista de volúmenes Android. Un volumen extraíble puede ser **USB o microSD**; no se adjudica un fabricante sin evidencia.
- **Transferencia:** permite seleccionar un archivo real con el selector Android y copiarlo a la carpeta autorizada. La app verifica contenido mediante SHA-256, conserva el original y crea archivos con nombre nuevo.
- **Otras operaciones:** explorador, edición de texto UTF-8 guardando una copia, renombrado y eliminación tras creación de una copia verificada. Escritura deshabilitada inicialmente.

## Compatibilidad y límites físicos

1. La app no puede convertir un power bank en un hub ni activar USB-PD o modo host que falte en el hardware.
2. El indicador "Carga + extraíble: Ambos observados" se muestra únicamente si Android informa alimentación externa y está accesible una carpeta situada en un volumen extraíble reconocido. Esto **no certifica USB-PD** ni distingue por sí solo USB de microSD.
3. El hub debe ofrecer una ruta de datos USB y entrada de alimentación apropiadas. En cada teléfono hay que comprobar simultaneidad real.
4. Android 11+ limita la selección de algunas carpetas raíz. En esos casos selecciona una subcarpeta del pendrive autorizable por el sistema.
5. La compilación CI de un **APK debug** no equivale a una validación en hardware. No publicar como estable hasta completar instalación, pruebas de archivo de ejemplo y compatibilidad física.

## Prueba real en Android, no destructiva

1. Instalar el APK debug candidato **solo de una ejecución CI exitosa**.
2. Abrir la app; verificar que la batería real coincide con la que muestra Android.
3. Conectar la memoria al teléfono o a un hub OTG compatible. Pulsar **Conectar** y seleccionar una subcarpeta de la unidad en el selector de Android.
4. Comprobar **Conectado a carpeta** y el nombre del origen; si aparece "almacenamiento interno", elegir otra carpeta.
5. Conectar alimentación al hub, verificar que Android indica entrada externa **y** que los archivos siguen accesibles. Si no es así, detener la prueba: hardware no confirmado.
6. Activar escritura. Copiar **un archivo de prueba** desde el teléfono y verificar que el archivo nuevo y el original se abren correctamente.
7. Pulsar **Desconectar**, esperar a que termine cualquier operación, y expulsar el USB usando los controles del sistema si existen.

**Seguridad:** sin root, sin ADB, sin permisos globales de almacenamiento y sin uso del dispositivo SUNSHINE SS-905D.

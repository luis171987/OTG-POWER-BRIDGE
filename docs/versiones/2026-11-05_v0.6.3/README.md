# OTG POWER BRIDGE v0.6.3 · archivo publicado, aceptación incompleta

Publicada en GitHub el **2026-11-05T23:20:33Z**; revisión estática del archivo realizada el **2026-11-06 UTC**. La fecha de esta revisión no es una captura ni una prueba física.

- [Release v0.6.3](https://github.com/luis171987/OTG-POWER-BRIDGE/releases/tag/v0.6.3), tag resuelto al commit [6a8ac407169aa0d7cfa4ff1c1dc5df5544542718](https://github.com/luis171987/OTG-POWER-BRIDGE/commit/6a8ac407169aa0d7cfa4ff1c1dc5df5544542718).
- [OTG-POWER-BRIDGE.apk](https://github.com/luis171987/OTG-POWER-BRIDGE/releases/download/v0.6.3/OTG-POWER-BRIDGE.apk), **7.304.400 bytes**. Android mínimo 26 (8.0), target SDK 35; paquete `bo.genai.usbpowerbridge`, versionName `0.6.3`, versionCode `10`, leídos del manifest binario.
- SHA-256 del archivo descargado y comparado con el digest de GitHub: `2fce8d4def837e4b0734c4a72f4d768b3de1451f5453fd2860a3a75a50a67aab`.
- Firma **APK Signature Scheme v2 / RSA PKCS#1 SHA-256**: firma y digest de contenido comprobados criptográficamente mediante inspección estática. Huella SHA-256 del certificado: `2c4fb3ea1e4152365ad934282df57579ede8235d5e110597485a28a80a95d131`. No se ha acreditado por esta comprobación la identidad del titular ni la continuidad de esta clave en otras versiones.
- No se instaló ni ejecutó el APK durante la revisión.

## Fuente y relación con CI

[CI del tag: run42128808304 intento1](https://github.com/luis171987/OTG-POWER-BRIDGE/actions/runs/42128808304), [job127599814454](https://github.com/luis171987/OTG-POWER-BRIDGE/actions/runs/42128808304/job/127599814454): 37+10 condiciones estáticas, compilación debug/release e integridad ZIP aprobadas. El workflow genera la variante release sin firma.

El APK publicado se comparó con `app-release-unsigned.apk` del [run42123871904, intento1](https://github.com/luis171987/OTG-POWER-BRIDGE/actions/runs/42123871904), artefacto `13459874571`, fuente `cd8b3773c55133db2081079bc975cd87637aea93`. Ese commit y el tag tienen el mismo árbol Git `af035a8d749a78da7e0e72c635d1848a30f130eb`. Las **120 entradas ZIP ajenas a la firma coinciden por SHA-256**. Se excluyeron únicamente los archivos de firma `META-INF/*.SF/*.RSA/*.DSA/*.EC` y `META-INF/MANIFEST.MF`. El digest de firma v2 se verificó sobre el archivo completo conforme a ese esquema.

Comandos de build observados en Actions: `gradle --no-daemon --console=plain :app:assembleDebug` y `gradle --no-daemon --console=plain :app:assembleRelease`. Los artefactos debug y release-unsigned permanecen separados de este APK de Releases y no se anuncian como instaladores de distribución.

## Aceptación y galería pendientes

El CI ejecuta validadores de presencia de código y compilación, no las ocho comprobaciones de `tests/TelemetryFormatsTest.kt`, tests de interfaz o pruebas físicas. [La documentación del propietario](https://github.com/luis171987/OTG-POWER-BRIDGE/blob/6a8ac407169aa0d7cfa4ff1c1dc5df5544542718/docs/RELEASE_V063.md) declara pruebas en teléfono; no incorpora un registro reproducible por SHA/dispositivo ni capturas reales de la entrega. Esa declaración se conserva como procedencia documental, sin convertirla en prueba ejecutada por esta revisión.

Las imágenes de diseño actuales permanecen en sus rutas originales y conservan su etiqueta conceptual. **Capturas reales de v0.6.3: pendientes.** No se fabrican tomas ni se reutilizan imágenes de otra versión como evidencia de ejecución.

Antes de considerar completa la aceptación: acreditar pruebas de interfaz y transferencia con datos de prueba, compatibilidad física de cada combinación y varias capturas reales del SHA correspondiente; consultar [el protocolo del proyecto](https://github.com/luis171987/OTG-POWER-BRIDGE/blob/6a8ac407169aa0d7cfa4ff1c1dc5df5544542718/docs/MODO_REAL_ANDROID.md). Una publicación marcada como final en GitHub no satisface por sí sola esas puertas.

## Acceso a archivos

[Manifiesto completo del tag](archivos.json): 71 blobs del árbol real no truncado, con rutas, tamaños, identificadores Git y enlaces por archivo. [Código fuente completo ZIP](https://github.com/luis171987/OTG-POWER-BRIDGE/archive/6a8ac407169aa0d7cfa4ff1c1dc5df5544542718.zip) · [árbol Git](https://github.com/luis171987/OTG-POWER-BRIDGE/tree/6a8ac407169aa0d7cfa4ff1c1dc5df5544542718).

Los Git blob SHA no son checksums SHA-256 de instaladores. El APK y los binarios grandes permanecen en Releases/Actions. Se conserva el [índice histórico](../README.md) y las previews anteriores.

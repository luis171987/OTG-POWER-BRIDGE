# OTG POWER BRIDGE V0.6.3 — distribución pública

Proyecto independiente de GENAI INDUSTRIES, autor Luis Carlos Moreno Jiménez.

- Código base: `review/cyber-cockpit-v063` (`8dce6a1050cb`).
- Identificador público: `bo.genai.usbpowerbridge` (sin `.preview`).
- Versión pública: `0.6.3`, versionCode `10`, Android 8+.
- El CI entrega una **APK RELEASE SIN FIRMA**. Nunca es instalador público.
- La firma estable se realiza exclusivamente en el dispositivo del propietario. **No subir keystore ni contraseñas a GitHub**.
- Publicar únicamente APK firmada, comprobada con apksigner, AAPT y SHA-256.
- La simultaneidad de carga y USB-OTG depende del teléfono, hub y alimentación.
- Las prestaciones USB/SAF y SHA-256 fueron comprobadas en teléfono por el propietario; el CI verifica construcción, no hardware.
- Diferentes certificados impiden actualizar las antiguas APK debug por encima. Resguardar los datos antes de una instalación limpia.

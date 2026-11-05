# OTG POWER BRIDGE 0.6.1 — reconstrucción nativa Cyber Cockpit

**Estado:** candidato de revisión, **no** versión pública verificada.

## Cambios

- Capa gráfica nativa con biseles metálicos luminosos, fondo tecnológico optimizado, medidores circulares y paneles de instrumentación.
- Transiciones animadas al cambiar Tablero, Transferencias, Memoria, Herramientas y Ajustes. Pueden desactivarse en Ajustes.
- Diagrama del estado observado de Android, conexiones USB detectadas por el sistema y fuente de carga; representa **topología**, no mide flujo eléctrico o tráfico USB.
- Panel de transferencia usa la sesión y el progreso reales; sin copia activa muestra «—».
- Separación mantenida entre UI Compose y `MainActivity.kt`/`SafeDocumentOps.kt`. Sin cambios intencionales en lógica de archivos.
- Versión candidata `0.6.1-cyber-cockpit-candidate`, `versionCode=8`. El APK `debug` usa `applicationIdSuffix=.preview`: puede instalarse **junto a** la V0.6.0 sin quitarla ni afectar sus datos. Al publicar una versión definitiva deberá firmarse con clave estable y sin ese sufijo.
- Conservación del logotipo 3D oficial y de las cinco imágenes aprobadas en el repositorio (referencias para el diseño, no capturas de telemetría real).

## Limitaciones técnicas

No reproduce píxel por píxel la ilustración 8K; es una interfaz funcional nativa inspirada en ella. Android no ofrece telemetría universal de porcentaje, potencia o marca de power banks. La carga simultánea exige hardware compatible. El movimiento del diagrama indica estado lógico observado, no comunicación USB en tiempo real. Un `debug APK` de GitHub Actions puede estar firmado con un certificado de depuración distinto al anterior.

## Comprobación en dispositivo antes de fusionar a main

1. Ejecutar Actions en PR y obtener el artefacto **OTG-POWER-BRIDGE-v061-debug-candidato**; verificar firma, hash, versión y estructura del APK.
2. Instalar APK como candidato paralelo (`bo.genai.usbpowerbridge.preview`); **no desinstalar la V0.6.0**. Si Android indica conflicto de paquete, detenerse y revisar el manifiesto.
3. Tomar captura real del Tablero en Honor Magic7 Lite y compararla con el objetivo visual 3D.
4. Recorrer las cinco pestañas varias veces; verificar transiciones y rendimiento; desactivar movimiento en Ajustes y repetir.
5. Conectar USB/hub compatibles, confirmar dispositivos reales visibles y que fuente/batería no muestren datos ficticios.
6. Permitir carpeta SAF, seleccionar archivo, copiar, verificar SHA-256, revisar cancelación y desconectar sesión.
7. Verificar sin USB y sin carpeta permisos (no falsificar conexiones).
8. Registrar defectos, aprobar diseño; solo entonces fusionar la PR. No publicar GitHub Release hasta verificar APK de distribución firmado con clave estable.

**Proyecto exclusivo:** `luis171987/OTG-POWER-BRIDGE`.
**Creador:** GENAI INDUSTRIES, Luis Carlos Moreno Jiménez, Santa Cruz de la Sierra, Bolivia.

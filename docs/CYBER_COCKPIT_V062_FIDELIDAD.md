# OTG POWER BRIDGE — Cyber Cockpit V0.6.2 (candidato)

**Autor:** GENAI INDUSTRIES — Luis Carlos Moreno Jiménez. Proyecto exclusivo `luis171987/OTG-POWER-BRIDGE`.

## Alcance

- Diseño visual nativo basado en cinco modelos **aprobados**: Tablero, Transferencias, Memoria, Herramientas, Ajustes.
- Encabezado 3D y cinco barras de navegación tomados de las imágenes aprobadas, sin cifras ficticias.
- Componentes dinámicos implementados con Jetpack Compose: botones nativos accesibles, medidores de batería y volumen, métricas SAF, líneas de flujo condicionadas a observaciones reales, y monitor gráfico de velocidad solo cuando hay bytes medidos.
- Mejoras de marcos 3D metalizados, iluminación azul/cian/naranja, fondos y ornamentación de capas.
- Identificador Android único de pruebas: `bo.genai.usbpowerbridge.previewv062`. Convive con V0.6.0 y V0.6.1 sin desinstalarlas.
- Funciones de archivos y sistema originales **inalteradas**: `MainActivity.kt`, `SafeDocumentOps.kt`, permisos SAF, copia con SHA-256, diagnósticos.
- No se agregan controles ficticios de reanudación, expulsión de hardware, pruebas eléctricas, lectura del % del power bank o escaneo de malware inexistente.

## Restricciones explícitas

Las pantallas de referencia son ilustraciones conceptuales de alta calidad; **no son capturas de una app funcional**. No hay garantía técnica de identidad píxel a píxel ni de resolución 8K en un teléfono. Android mostrará recursos reescalados a su resolución y densidad; las métricas son componentes nativos para lectura e interacción, con verificación de compilación y captura real antes de fusionar.

## Puertas de calidad

1. Pruebas estáticas V062 y modo real original.
2. `gradle :app:assembleDebug` en GitHub Actions; inspeccionar resultado real, firmas, manifest y hash.
3. Instalar APK **previewv062** y validar cinco pantallas en HONOR Magic7 Lite.
4. Probar conexión sin USB, conexión USB host, carga, SAF, copia y hash; documentar los casos no disponibles.
5. Comparar capturas de pantalla con las cinco referencias aprobadas. Corregir si aún no cumple.
6. No fusionar `main`, no publicar GitHub Releases, no anunciar como APK final hasta aprobación explícita.

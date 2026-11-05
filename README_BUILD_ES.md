# Compilación local — OTG POWER BRIDGE

Versión del código: **v0.3.0-preview**. Android 8.0+ (API 26), compileSdk/targetSdk 35. AGP 8.7.3, Kotlin 2.0.21, Java 17, Gradle 8.9.

## Desde Termux

1. Descomprime el ZIP en una carpeta de usuario con suficiente espacio libre.
2. Verifica que `java`, `python`, `sha256sum`, SDK Android API 35 y `gradle` o wrapper estén disponibles.
3. Ejecuta `bash scripts/build_termux.sh` desde la carpeta raíz.
4. Lee únicamente el resumen en pantalla; los errores quedan en `build_logs/`.
5. **No instales** si no existe `app/build/outputs/apk/debug/app-debug.apk` y no se ha verificado la firma.

No se generan ni publican APK Release firmados desde este paquete. Los cambios de visuales o README no sustituyen una prueba de hardware. Ninguna herramienta publica en GitHub de forma automática.

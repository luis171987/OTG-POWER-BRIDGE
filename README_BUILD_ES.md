# Compilar OTG POWER BRIDGE 0.4.0 — modo real

El código contiene la interfaz Kotlin/Compose **sin simulador**. El APK no se incorpora al ZIP: debe compilarse y comprobarse con Android SDK 35, Java 17 y Gradle 8.9 en CI o entorno Android preparado.

GitHub Actions: `.github/workflows/android-debug.yml` usa `android-actions/setup-android@v4` y `:app:assembleDebug`. El artefacto resultante es **debug/candidato**, no lanzamiento público estable.

Alternativa: `bash scripts/build_termux.sh` desde la carpeta del proyecto. Este comando verifica el entorno y utiliza las dependencias instaladas; no instala nada por su cuenta.

La edición aprobada cambia la app sin cambiar su `applicationId` (`bo.genai.usbpowerbridge`), incrementando versionCode 5. Los APK debug generados en distintos servidores pueden utilizar distintas claves de firma y **no siempre permiten actualizar una instalación anterior**. Antes de desinstalar, conserva cualquier archivo importante y anota que se perderán las autorizaciones SAF de la aplicación. No desinstales por defecto sin comprobar la firma.

Para publicar código del repositorio de manera independiente: `bash scripts/PUBLICAR_MODO_REAL_TERMUX.sh --publicar`, ejecutado sobre el ZIP completo de este paquete tras autenticarse con GitHub CLI como `luis171987`.

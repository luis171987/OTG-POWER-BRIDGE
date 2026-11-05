# Preview 0.5.0-real-candidate · beed51f0e0c3

**Estado: validación incompleta, sin tag, Release o instalador estable.** Observación de GitHub: 2026-11-05 UTC. Esta fecha no es una captura ni una prueba física.

| Plataforma | Entrega | Evidencia |
| --- | --- | --- |
| Android | APK debug candidato, artefacto temporal de CI | [Ejecución 41991766174, intento 1](https://github.com/luis171987/OTG-POWER-BRIDGE/actions/runs/41991766174) · [artefacto 13398068748](https://github.com/luis171987/OTG-POWER-BRIDGE/actions/runs/41991766174/artifacts/13398068748) |
| Código fuente | Commit completo y árbol Git | [ZIP del SHA](https://github.com/luis171987/OTG-POWER-BRIDGE/archive/beed51f0e0c329aa973259cd4ed5b19c0ebc818c.zip) · [árbol](https://github.com/luis171987/OTG-POWER-BRIDGE/tree/beed51f0e0c329aa973259cd4ed5b19c0ebc818c) · [manifiesto](archivos.json) |

Fuente exacta: [beed51f0e0c329aa973259cd4ed5b19c0ebc818c](https://github.com/luis171987/OTG-POWER-BRIDGE/commit/beed51f0e0c329aa973259cd4ed5b19c0ebc818c). El proyecto declara Android 8.0+ (`minSdk 26`), `targetSdk/compileSdk 35`, versión `0.5.0-real-candidate`, versionCode 6. Estos requisitos no certifican compatibilidad con un teléfono o hub concreto.

El workflow compila con Java 17 y SDK API 35 configurados, ejecuta dos validadores estáticos (10/10 y 12/12) y comprueba el APK con `test -s` y `unzip -tq`. Comando de compilación observado: `gradle --no-daemon --console=plain :app:assembleDebug`. [Configuración exacta](https://github.com/luis171987/OTG-POWER-BRIDGE/blob/beed51f0e0c329aa973259cd4ed5b19c0ebc818c/.github/workflows/android-debug.yml).

El registro de CI publica SHA-256 del APK `db6c7bb170ac281115d3027c8d04773061f6ee74b88fac74e7584f23684af513`. Procedencia: [job 127125411443](https://github.com/luis171987/OTG-POWER-BRIDGE/actions/runs/41991766174/job/127125411443), paso «Verificar APK debug». Esta revisión no descargó ni volvió a calcular ese hash. El digest del ZIP del artefacto de Actions, registrado aparte en el manifiesto, no es el checksum del APK.

## Puertas pendientes

- Las ocho comprobaciones Kotlin de formato están en el repositorio pero no son una tarea ejecutada por este CI; `assembleDebug` no ejecuta una suite de tests.
- No se verificó la interfaz en emulador o dispositivo, ni transferencia, restauración ante errores, permisos SAF o compatibilidad de carga + OTG.
- Debe completarse el [protocolo del proyecto](https://github.com/luis171987/OTG-POWER-BRIDGE/blob/beed51f0e0c329aa973259cd4ed5b19c0ebc818c/docs/MODO_REAL_ANDROID.md) con datos de prueba antes de declarar una versión estable. No se instala ni se realiza prueba física desde esta revisión.
- Capturas reales de esta preview: **pendientes**. La galería actual es conceptual y mantiene esa etiqueta. No se usan imágenes de otras versiones como evidencia de ejecución.

## Archivos y conservación

El [manifiesto](archivos.json) enumera los 43 blobs del árbol Git completo del SHA funcional, con rutas, tamaños, identificadores Git y enlaces individuales; incluye código, documentación, tests y recursos públicos existentes. Los identificadores Git blob no son hashes SHA-256 de instaladores.

Los binarios de CI permanecen en Actions y caducan según su retención. No se incorporan APK al historial ni se elimina material anterior. No hay assets de Releases publicados en esta observación.

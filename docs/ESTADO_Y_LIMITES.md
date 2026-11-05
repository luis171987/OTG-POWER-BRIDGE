# Estado v0.4.0 — Modo real

- **Eliminado el simulador de la aplicación** y los escenarios simulados del repositorio. UI con botones Conectar/Desconectar y estados reales consultados a Android.
- Integradas lectura de batería, enumeración de USB Host y selección SAF de una carpeta, así como copia con SHA-256, edición de texto como nueva copia, renombrado y eliminación con respaldo previo, sujetas a permisos.
- **Conectar** inicia una sesión autorizada en la app, NO establece eléctricamente USB-PD ni cambia de modo host. **Desconectar** cierra la sesión sin expulsar físicamente el volumen.
- No se valida automáticamente el origen exacto del power bank; dispositivos extraíbles pueden ser pendrives USB o tarjetas SD.
- Compatibilidad carga + OTG con cada teléfono/hub: **pendiente de prueba física**.
- Compilación de esta versión, instalación en dispositivo y APK release firmado: **pendientes de verificación en CI y Android real**.

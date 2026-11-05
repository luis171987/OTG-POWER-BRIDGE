# OTG POWER BRIDGE · Cyber Cockpit V0.6.0

**Identidad:** GENAI INDUSTRIES · logotipo oficial aprobado, adaptado en formato panorámico sin cambiar sus motivos.

**Alcance de esta entrega:** actualización nativa de interfaz de la V0.5.0, con cinco pantallas Compose navegables y lógica de Android/SAF preservada. No es una prueba de carga + OTG simultáneos ni una certificación universal de hubs. Versión candidata, todavía sin compilación APK validada para V0.6.0.

## Mapeo entre imágenes aprobadas y acciones implementadas

| Pantalla | Acceso | Acción real disponible | Indicadores verídicos |
| --- | --- | --- | --- |
| Tablero / Cyber Cockpit | `Tablero` | Conectar (selector de carpeta), Desconectar (cierre sesión), Actualizar | batería Android, fuente externa comunicada, USB host/USB enumerados, árbol SAF, extraíble identificado |
| Transferir | `Transferir` | Seleccionar carpeta destino, habilitar escritura, seleccionar origen y copiar con SHA-256 | progreso de la copia iniciada, bytes, estimación de velocidad media, estado de hash |
| Memoria | `Memoria` | Elegir carpeta, recorrer archivos, seleccionar, editar texto como copia, duplicar, renombrar, respaldo y eliminación confirmada | StatFs de partición de datos; capacidad extraíble si Android la expone; identidad USB si se expone |
| Herramientas | `Herramientas` | Refrescar diagnósticos, hash SHA-256 del archivo seleccionado, cerrar sesión | estado batería, USB host, número de dispositivos, carpeta, extraíble |
| Ajustes | `Ajustes` | Habilitar/deshabilitar efectos animados, vista compacta e IDs técnicos (se guardan localmente) | permisos SAF, versión candidata, avisos reales |

## Límite deliberado: no simular electrónica

El dibujo de conexiones es una **visualización ilustrativa**: Android suele informar carga y almacenamiento, pero no puede identificar universalmente un hub o power bank ni medir su porcentaje o los vatios por la entrada USB-C. Se animan únicamente tramos correspondientes a señales observadas (energía externa y/o volumen extraíble). El acceso a una carpeta interna NO acredita que haya una memoria USB conectada. Los mockups aprobados incluyen números de ejemplo que NO serán importados como telemetría.

El selector de carpeta permite dispositivos Android de múltiples marcas, sujeto a permisos y restricciones del proveedor SAF. La interfaz usa componentes Compose escalables, no una imagen 8K inerte que aparenta tener botones funcionales. Las imágenes rasterizadas adjuntas se utilizan como referencia visual en la documentación.

## Calidad y pruebas pendientes

- Comprobación local de estructura, rutas y scripts.
- Compilación Android completa y pruebas de UI en GitHub Actions **pendientes hasta publicar el código candidato**, previa revisión.
- Pruebas físicas con un hub PD + datos compatible, pendiente.
- No publicar APK ni declarar que los datos de los mockups se obtuvieron de un teléfono real.

# OTG POWER BRIDGE — Cyber Cockpit V0.6.3 (candidato)

GENAI INDUSTRIES · Dirección: Luis Carlos Moreno Jiménez · Santa Cruz de la Sierra, Bolivia.

## Alcance aprobado

Correcciones de legibilidad, densidad y precisión de datos sobre la V0.6.2 aprobada visualmente. No implica aprobación funcional de hardware real. No modificar otras aplicaciones ni repositorios.

### Diferencias visibles

- **Diagrama USB / OTG / alimentación:** mantiene nodos dentro de un espacio propio, y la nota explicativa en un bloque separado, sin superposición; nodos flexibles para distintos tamaños de pantalla.
- **Tamaño de medidores:** los gauges compactos ya no fuerzan 132 dp en tarjetas estrechas. Ajustan texto y radio a 81 dp. Mejora en pantallas de menor ancho y orientación vertical.
- **Almacenamiento:** el porcentaje del gráfico representa explícitamente **espacio ocupado** de la partición de datos de Android; el espacio disponible se identifica como **libre**. Se muestra por separado la capacidad de la carpeta/volumen autorizado si Android la permite.
- **Números pequeños:** mejora el tamaño de rótulos de paneles, métricas y ajustes sin eliminar elementos interactivos.
- **Estados reales:** flujo de conexión ilustrativo, sin suponer la presencia de un hub ni medir físicamente potencia USB-PD; no se infiere porcentaje de power bank. El trazado de la rama eléctrica solo se anima ante la observación conjunta de alimentación externa y almacenamiento extraíble mediante SAF.

### Preservación

`MainActivity.kt`, `SafeDocumentOps.kt`, `AndroidManifest.xml`, `README.md`, iconos originales y recursos gráficos V0.6.2 **no se sustituyen**.

### Limitaciones y pruebas antes de lanzar

- La UI usa componentes nativos Jetpack Compose con arte 3D ornamental; un render ilustrativo 8K no equivale a pantalla nativa renderizada a 8K. La resolución real depende del dispositivo.
- No se promete conexión de carga y OTG simultáneos sin hardware compatible.
- Validar compilación GitHub Actions y ejecutar en HONOR Magic7 Lite: cinco pantallas, ausencia de solapamiento, toggles de Ajustes, rutas SAF, copia + SHA-256 con un archivo de prueba y dispositivo USB compatible.
- Verificar capturas tras rotación, tamaño de fuente aumentado y 2+ dispositivos en hub alimentado (si el hardware lo admite).
- La nueva build es **preview**: id `bo.genai.usbpowerbridge.previewv063`, `versionCode=10`. No es release ni sustituye V0.6.2. No firmar comercialmente ni anunciar descarga pública sin pruebas.

### Retirada

No se aplica `git merge`, ni `git push` a `main`, ni se cierra o altera la PR #2. La V0.6.3 vive en rama nueva derivada de V0.6.2 y se valida por PR propia.

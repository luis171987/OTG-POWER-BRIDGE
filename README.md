<div align="center">

<img src="assets/LOGOTIPO_OTG_POWER_BRIDGE_OFICIAL.jpg" alt="Logotipo oficial 3D de OTG POWER BRIDGE: símbolo USB, energía y archivos, en azul y naranja" width="360" />

# OTG POWER BRIDGE

### Conecta · Alimenta · Transfiere

**Aplicación Android creada por GENAI INDUSTRIES**  
**Creador y director:** Luis Carlos Moreno Jiménez — Santa Cruz de la Sierra, Bolivia.

**Estado:** prototipo Android `v0.3.1-preview` · código fuente disponible · **APK público todavía no publicado**

</div>

## 📢 Comparte el repositorio en WhatsApp y Facebook

![Imagen de vista previa social de OTG POWER BRIDGE, basada en el logo oficial de GENAI INDUSTRIES](assets/SOCIAL_PREVIEW_OTG_POWER_BRIDGE_1280x640.jpg)

**Repositorio público previsto:** [github.com/luis171987/OTG-POWER-BRIDGE](https://github.com/luis171987/OTG-POWER-BRIDGE) (el enlace funcionará después del primer `push`).

Para que este **logo aparezca como miniatura cuando se comparta el enlace**, el propietario debe cargar la imagen anterior en **GitHub → Settings → Social preview → Edit → Upload an image**. Este paso se realiza **una sola vez** tras crear el repositorio y **no puede sustituirse por poner una imagen en el README**. Consulta las [instrucciones de vista previa para WhatsApp y Facebook](docs/COMPARTIR_WHATSAPP_FACEBOOK.md). Las redes pueden conservar miniaturas en caché.

## ¿Para qué sirve?

**OTG POWER BRIDGE** ayuda a consultar la carga del teléfono, detectar dispositivos USB visibles y administrar archivos de carpetas autorizadas por el usuario. Su objetivo es facilitar transferencias cuando necesitas usar una memoria USB **mientras alimentas tu teléfono desde un power bank**, siempre que un **hub OTG con alimentación pasante** y tu Android permitan ambas funciones simultáneamente.

La app **no puede crear una ruta USB de datos por software** ni hacer compatible un dispositivo que físicamente no admite carga + OTG. La alimentación es un circuito físico; los archivos viajan por el hub y el bus USB, no por la app ni a través del power bank.

## 🎬 Caso de uso: trabajando con el teléfono y la batería casi agotada

> Imagina que estás trabajando desde el celular. Tienes fotografías, un contrato PDF y documentos que debes entregar en una memoria USB. El teléfono marca **8 % de batería** y no puedes esperar a que se recargue para terminar la transferencia. Necesitas conectar un power bank **sin desconectar el pendrive**.

**Solución propuesta:** un hub USB-C compatible con **entrada de carga PD** y **puerto USB de datos** permite conectar una batería externa y una memoria al teléfono por un solo conector USB-C. En modelos Android que admitan el funcionamiento simultáneo, OTG POWER BRIDGE ayuda a seleccionar la carpeta de destino y copiar archivos verificando su integridad.

### Imagen 1 — Ejemplo completo de la solución

![Escena 3D ilustrativa: teléfono Android con logo de OTG POWER BRIDGE, power bank blanco, hub USB-C gris y memoria conectados](assets/01_conexion_general_3d.png)

*Se distinguen **dos rutas independientes**: el **cable de alimentación** conecta el power bank a la entrada **PD IN** del hub; el **pendrive** se conecta a un **puerto USB DATA** del hub; el hub se conecta al teléfono. Imagen conceptual, no prueba de compatibilidad ni fotografía de equipos en funcionamiento.*

## 🔌 Tutorial de conexión física

### Imagen 2 — Identifica cada dispositivo y conéctalo

![Tutorial 3D: 1 power bank, 2 hub USB-C OTG con PD, 3 memoria USB, 4 teléfono Android](assets/02_tutorial_conexion_3d.png)

**Paso 1 — Prepara el teléfono.** Comprueba que dispone de puerto USB compatible con OTG. Abre la app sin conectar todavía la memoria y comprueba el estado de la batería. No todas las marcas admiten carga y datos OTG simultáneos.

**Paso 2 — Conecta el hub.** Inserta el conector principal del **hub USB-C OTG** en el teléfono. El hub debe anunciar **USB data / OTG** y, por separado, **PD IN / carga pasante**. Un divisor de carga o un adaptador exclusivamente eléctrico no sirve como sustituto de un hub de datos.

**Paso 3 — Conecta el pendrive al puerto de datos.** Utiliza USB-A o USB-C según la memoria. Una memoria **micro-USB** necesita un adaptador USB de datos compatible. **No conectes la memoria a PD IN.** Si Android muestra la unidad en el selector de archivos, habrá una vía de datos accesible.

**Paso 4 — Conecta el power bank a PD IN.** Utiliza la salida USB-C del power bank, un cable apropiado y la entrada de alimentación PD del hub. Deben ser eléctricamente compatibles. **No empalmes cables ni conectes el SUNSHINE SS-905D de reparación.** Confirma que Android indica alimentación externa y comprueba si el pendrive sigue accesible.

**Prueba de compatibilidad necesaria:** primero verifica acceso USB sin alimentación; luego conecta energía, observa que el teléfono realmente cargue y vuelve a comprobar el almacenamiento. Si cualquiera de las funciones deja de funcionar, esa combinación **no está validada**.

## 📱 Tutorial dentro de la aplicación

### Imagen 3 — Detectar, autorizar y transferir

![Pantallas conceptuales OTG POWER BRIDGE: diagnóstico de carga, USB, permisos del sistema y explorador de archivos](assets/03_tutorial_aplicacion_3d.png)

**1. Consultar energía:** abre OTG POWER BRIDGE, cambia del modo de simulación al modo real y comprueba el estado de batería que informa Android. La app **no mide directamente voltios, amperios, potencia PD ni capacidad del power bank**. Los valores eléctricos ilustrados en los renders son solo ejemplos visuales.

**2. Comprobar memoria:** pulsa **Actualizar diagnóstico USB**. La enumeración USB puede ser incompleta; la prueba definitiva de accesibilidad de archivos es que Android ofrezca la unidad en el selector de documentos.

**3. Autorizar carpeta:** selecciona **Autorizar carpeta de la memoria USB** y elige una carpeta del pendrive desde el selector de Android. Si el dispositivo no aparece, no atribuyas el fallo a la app sin revisar el hub, la alimentación y el formato del almacenamiento.

**4. Transferir un archivo del teléfono:** en modo real, habilita los cambios de archivos; pulsa **Copiar archivo del teléfono a la carpeta autorizada (SHA-256)**, selecciona el PDF, documento o fotografía mediante el selector de Android y espera la confirmación de la copia. El original permanece intacto y el contenido transferido se compara mediante **SHA-256**, según lo implementado en el código. **Esta función aún requiere compilación y pruebas en un teléfono real.**

**5. Expulsión:** al terminar, comprueba que la operación terminó antes de retirar el pendrive. Utiliza la opción de expulsión del sistema Android si está disponible. Desconectar la memoria durante una escritura puede corromper archivos.

## 🧭 Vista previa del frontend Android

### Imagen 4 — Panel principal del producto

![Diseño 3D conceptual de OTG POWER BRIDGE en teléfono Redmi: batería, USB, almacenamiento, explorador, diagnóstico, respaldo, sincronización y seguridad](assets/04_panel_android_3d.png)

*Diseño visual del frontend; no es una captura de una aplicación instalada. La interfaz implementada en el prototipo es más sencilla. Los módulos ilustrados que todavía no existen no deben interpretarse como funciones terminadas.*

| Panel | Función | Estado del prototipo |
| --- | --- | --- |
| **Energía** | Consultar porcentaje y estado de carga reportados por Android | Implementación inicial |
| **Diagnóstico USB** | Consultar USB host, dispositivos enumerados y VID/PID cuando Android los expone | Implementación inicial |
| **Explorar** | Navegar en carpetas autorizadas mediante Storage Access Framework (SAF) | Implementación inicial |
| **Copiar a USB** | Seleccionar archivo del sistema y copiarlo a carpeta autorizada con verificación SHA-256 | Código incorporado en v0.3.0; pendiente de probar en dispositivo |
| **Editar** | Abrir textos UTF-8 pequeños y guardar una copia nueva sin sobrescribir el original | Implementación inicial |
| **Respaldo** | Duplicar archivos con SHA-256 en la carpeta autorizada | Implementación inicial |
| **Seguridad** | Cambios desactivados por defecto y confirmación de operaciones destructivas | Implementación inicial |
| **Sincronizar automáticamente** | Sincronización bidireccional o programada | **No implementado** |

## 🌍 Compatibilidad universal por capacidades

Compatible **a nivel de diseño** con múltiples fabricantes Android (HONOR, Samsung, Redmi/Xiaomi, Motorola, Pixel y otros), hubs USB-C con funciones adecuadas, power banks de diversas marcas y pendrives **USB-A, USB-C o micro-USB mediante adaptador USB de datos**.

- **Android 8.0 o superior** es el mínimo definido por este proyecto (`minSdk 26`), **no** una certificación de funcionamiento en todos los teléfonos de esa versión.
- No existe garantía universal de carga simultánea + OTG: depende del firmware, el hardware USB del teléfono, el hub, la negociación de alimentación y los adaptadores.
- No se requiere root, ADB ni Termux para usar la app una vez compilada e instalada.
- La copia entre teléfono y USB requiere acceso concedido al archivo de origen y a la carpeta de destino, además de permisos efectivos de escritura del proveedor de documentos.
- Android puede exponer memorias USB desde el selector de archivos aunque no muestre el periférico en la lista de dispositivos USB accesible a la aplicación.

## 🔐 Privacidad y seguridad

Los archivos seleccionados se procesan en el dispositivo mediante permisos del sistema Android; no se necesita subir documentos a un servidor. Las operaciones que modifican archivos están desactivadas inicialmente. **La copia de respaldo dentro del mismo pendrive no sustituye un respaldo en otro almacenamiento.** No se recomienda utilizar documentos únicos e irremplazables antes de completar pruebas físicas y de software.

## 📦 Instalación y APK

**Por ahora no hay un APK público verificado para descargar.** El repositorio contiene código fuente de un **prototipo**, imágenes y documentación. Una vez que exista un APK compilado, firmado y probado, podrá publicarse como activo de una versión en **GitHub Releases** y aparecerá aquí el enlace HTTPS de descarga directa. No se anunciará como descargable antes de su publicación real.

Para compilar desde un entorno Android con Java, Android SDK API 35 y Gradle:

```bash
bash scripts/build_termux.sh
```

El script registra diagnósticos y no instala automáticamente el APK. Consulta [`README_BUILD_ES.md`](README_BUILD_ES.md).

## 🧪 Estado de pruebas

Se han ejecutado pruebas **lógicas/simuladas** en versiones anteriores; la versión `0.3.0-preview` incorpora cambios de marca y transferencia SAF. **No se ha compilado ni probado en hardware real en esta sesión.** Las imágenes son renders conceptuales. Las cifras y los estados ficticios mostrados en ellas son únicamente ilustrativos.

## Créditos

**OTG POWER BRIDGE** — Aplicación creada por **GENAI INDUSTRIES**.  
**Dirección y autoría:** Luis Carlos Moreno Jiménez, Santa Cruz de la Sierra, Bolivia.  
Logo 3D: **versión oficial aprobada por su autor**. La marca y sus recursos visuales no se deben sustituir por versiones de prueba.

**Referencias técnicas:** [Android — Storage Access Framework](https://developer.android.com/training/data-storage/shared/documents-files), [Android — USB host](https://developer.android.com/develop/connectivity/usb/host), [GitHub — vínculos de descarga de releases](https://docs.github.com/en/repositories/releasing-projects-on-github/linking-to-releases).

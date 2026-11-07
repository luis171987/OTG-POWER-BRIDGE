<div align="center">

<img src="assets/LOGOTIPO_OTG_POWER_BRIDGE_OFICIAL.jpg" width="180" alt="Logotipo oficial OTG POWER BRIDGE — GENAI INDUSTRIES">

# ⚡ OTG POWER BRIDGE

### CYBER COCKPIT 3D · ANDROID · VERSIÓN ESTABLE **0.6.3**

**Energía · Conectividad USB/OTG · Transferencia de archivos**

**GENAI INDUSTRIES** · Autor: **Luis Carlos Moreno Jiménez** · Santa Cruz de la Sierra, Bolivia 🇧🇴

[![Versión estable](https://img.shields.io/badge/versi%C3%B3n_estable-v0.6.3-0aa7ec?style=for-the-badge)](https://github.com/luis171987/OTG-POWER-BRIDGE/releases/tag/v0.6.3)
[![Android](https://img.shields.io/badge/Android-8%2B-28a745?style=for-the-badge)](https://github.com/luis171987/OTG-POWER-BRIDGE/releases/latest)

</div>

## 🎬 VIDEO OFICIAL — OTG POWER BRIDGE V0.6.3 (1 min 42 s)

**¿Estás viajando, la batería se está agotando y necesitás copiar archivos de una memoria USB?** Mirá primero esta demostración con narración femenina en español latinoamericano. Muestra el **HVE / hub OTG**, el **power bank** conectado a la entrada de alimentación del hub y las **memorias USB** conectadas a sus puertos; después presenta las cinco pantallas Cyber Cockpit.

<div align="center">

[![▶️ REPRODUCIR VIDEO OFICIAL OTG POWER BRIDGE — presentación v0.6.3](assets/ui_aprobada/00_tablero_cyber_cockpit_aprobado.jpg)](assets/videos/OTG_POWER_BRIDGE_V063_COMERCIAL_ESMX_102S.mp4)

<video src="https://github.com/luis171987/OTG-POWER-BRIDGE/raw/refs/heads/main/assets/videos/OTG_POWER_BRIDGE_V063_COMERCIAL_ESMX_102S.mp4" controls playsinline preload="metadata" poster="assets/ui_aprobada/00_tablero_cyber_cockpit_aprobado.jpg">
  <a href="https://github.com/luis171987/OTG-POWER-BRIDGE/raw/refs/heads/main/assets/videos/OTG_POWER_BRIDGE_V063_COMERCIAL_ESMX_102S.mp4">▶️ Reproducir el video oficial (MP4, con sonido)</a>
</video>

### ▶️ [REPRODUCIR / DESCARGAR EL VIDEO OFICIAL CON SONIDO](https://github.com/luis171987/OTG-POWER-BRIDGE/raw/refs/heads/main/assets/videos/OTG_POWER_BRIDGE_V063_COMERCIAL_ESMX_102S.mp4)

<sub>Video promocional de 102 segundos · 720 × 1280 · voz femenina es-MX · archivo MP4 almacenado en este repositorio.</sub>

</div>

> 🔊 **Para escuchar la narración, tocá Reproducir.** Por las restricciones de GitHub y de los navegadores, el README no puede imponer reproducción automática **con sonido**. El enlace de video funciona como alternativa cuando el reproductor integrado no está disponible.
>
> ⚠️ **Demostración promocional:** la carga simultánea con OTG solo funciona si tanto el teléfono como el HVE / hub USB-C con entrada PD son compatibles. La aplicación **no activa ni garantiza** esa capacidad física.

## 📥 [⬇️ DESCARGAR LA ÚLTIMA APK OFICIAL](https://github.com/luis171987/OTG-POWER-BRIDGE/releases/latest/download/OTG-POWER-BRIDGE.apk)

**[📦 Ver versión publicada y notas](https://github.com/luis171987/OTG-POWER-BRIDGE/releases/tag/v0.6.3)** · **[📖 Instalación y uso](#-instalación-en-android)**


> ✅ **VERSIÓN ACTUAL: V0.6.3**. Este README describe únicamente la versión estable publicada. Los prototipos anteriores se conservan en el historial de Git y la documentación técnica, **pero no son la descarga recomendada**.

## 🚀 ¿Qué es OTG POWER BRIDGE?

Una aplicación **nativa de Android (Kotlin / Jetpack Compose)** para visualizar información real de batería y USB, abrir carpetas autorizadas por Android, gestionar archivos y realizar copias verificadas mediante **SHA-256**. Su interfaz **Cyber Cockpit 3D** distribuye las herramientas en cinco paneles.

No hace falta root ni Termux para utilizar el APK instalado. **No crea funciones eléctricas inexistentes**: usar una memoria OTG mientras se carga el móvil solo es posible si el **teléfono y el hub USB-C con entrada de alimentación PD** lo permiten.

## 🔵 Cinco pantallas Cyber Cockpit — Versión 0.6.3

Los cinco diseños siguientes son **referencias gráficas de la interfaz aprobada**. Están enlazados a su imagen original para verlos con más nitidez. La app instalada contiene controles funcionales y datos de Android; los números que se vean en los diseños son ilustrativos, **no telemetría real ni capturas certificadas**.

### 1. Tablero

<a href="assets/ui_aprobada/00_tablero_cyber_cockpit_aprobado.jpg"><img src="assets/ui_aprobada/00_tablero_cyber_cockpit_aprobado.jpg" alt="Referencia visual Cyber Cockpit: Tablero" width="330"></a>

[🔍 Abrir imagen original a máxima resolución disponible](assets/ui_aprobada/00_tablero_cyber_cockpit_aprobado.jpg)

**📘 Tutorial del Tablero — primeros pasos**

Esta es la pantalla de inicio. Sirve para **abrir una sesión de archivos y comprobar lo que Android realmente detecta**, sin activar por sí misma la carga eléctrica.

1. **Prepará las conexiones:** conectá el **teléfono al HVE/hub USB-C OTG**; enchufá las memorias USB a sus **puertos de datos**; conectá el **power bank a la entrada de energía/PD IN** del hub, no a un puerto de datos. La carga y el acceso USB simultáneos solo funcionarán si **teléfono, hub y cables son compatibles**.
2. **Abrí la aplicación** y consultá **BATERÍA**: porcentaje y estado *CARGANDO / SIN CARGA* según Android. El panel **POWER BANK** indica **SIN TELEMETRÍA** porque Android normalmente no informa el porcentaje de esa batería externa.
3. Tocá **CONECTAR**. Se abrirá el **selector de carpetas de Android**. Entrá a la unidad USB o al almacenamiento del teléfono y elegí **una carpeta autorizable**; aceptá **Usar esta carpeta / Permitir** si Android lo solicita. En Android 11 o superior, la raíz de algunas unidades puede estar restringida: probá con una subcarpeta.
4. Comprobá **SESIÓN DE ARCHIVOS ACTIVA** y el nombre de la **carpeta autorizada**. *Conectar* aquí **no enchufa, alimenta ni monta** físicamente dispositivos: concede acceso documental a una carpeta.
5. Interpretá **HOST USB** (capacidad declarada), **DISPOSITIVOS** (equipos que la API USB permite enumerar) y **CARPETA** (acceso SAF autorizado). Que el contador de dispositivos marque cero **no demuestra** que una memoria sea inaccesible si Android permite verla en el selector.
6. Revisá el **diagrama de datos y energía**, la información de almacenamiento (**% ocupado**, no % libre) y la sección **Transferencia en tiempo real**; esta última muestra progreso solamente cuando se realiza una copia real.
7. Para trabajar, tocá **ABRIR CENTRO DE TRANSFERENCIAS**, **EXPLORAR MEMORIA USB / TELÉFONO**, **HERRAMIENTAS** o utilizá las pestañas inferiores. **ACTUALIZAR** vuelve a consultar información del sistema.
8. Al terminar, tocá **DESCONECTAR** para cerrar la sesión documental y liberar el permiso concedido. **No expulsa** la memoria USB: hacelo desde Android cuando la escritura haya finalizado.

**Qué esperar:** podés ver los datos disponibles del teléfono y acceder a la carpeta autorizada. **No esperes** que la app mida vatios USB-PD, porcentaje del power bank o fuerce la carga simultánea.


### 2. Transferencias

<a href="assets/ui_aprobada/01_transferencias.jpg"><img src="assets/ui_aprobada/01_transferencias.jpg" alt="Referencia visual Cyber Cockpit: Transferencias" width="330"></a>

[🔍 Abrir imagen original a máxima resolución disponible](assets/ui_aprobada/01_transferencias.jpg)

**📘 Tutorial de Transferencias — copiar un archivo con verificación**

La pantalla **Transferir** permite copiar **un archivo por operación** desde un origen elegido en Android hacia la **carpeta de destino autorizada**. El archivo original se conserva.

1. Elegí primero **dónde querés recibir el archivo**: pulsá **SELECCIONAR CARPETA** y, en Android, autorizá una carpeta del **teléfono** o de la **memoria USB**, según el sentido de la copia. Esa carpeta es el **DESTINO**.
2. Verificá que el panel indique **SESIÓN DE DESTINO ABIERTA** y muestre la carpeta correcta. Si cambiaste de unidad o carpeta raíz, volvé a seleccionarla; la app maneja una **sesión de carpeta principal a la vez**.
3. Activá **Habilitar escritura**. La opción está desactivada al principio para evitar modificaciones accidentales; solo permite intentar operaciones si Android y el proveedor documental concedieron acceso de escritura.
4. Tocá **+ SELECCIONAR Y COPIAR ARCHIVO**. En el selector del sistema **elegí el archivo de origen** (puede encontrarse en otra unidad accesible). No hay que moverlo manualmente ni convertirlo primero.
5. Esperá el resultado en **Transferencia real en curso**: archivo, estado, bytes o porcentaje **cuando el tamaño se conoce** y velocidad observada **cuando haya muestras**. **No desconectés** el cable ni la unidad durante la operación.
6. La aplicación crea un **archivo nuevo con nombre diferenciado** en el destino, vuelve a leerlo y compara el **SHA-256** de origen y copia. Solo si coinciden informa **Copia verificada (SHA-256)**; consultá también el **Registro de operaciones**.
7. Abrí la carpeta de destino desde **Memoria** o desde el gestor de archivos de Android y verificá que el documento se pueda abrir. Después podés adjuntarlo desde **Gmail, WhatsApp u otra aplicación**; el envío por esas apps **no se realiza dentro de OTG POWER BRIDGE**.

**USB → teléfono:** autorizá como destino una carpeta del teléfono; después elegí el archivo de la USB en el selector Android. **Teléfono → USB:** autorizá primero una carpeta de la USB como destino y elegí el archivo del teléfono como origen. En ambos casos la copia depende de que Android permita leer el origen y escribir en el destino.

**Límites reales:** no hay transferencia masiva de carpetas, **pausa/reanudación ni recuperación automática** si el teléfono pierde energía. Si una copia falla, la app intenta retirar el **archivo nuevo incompleto** sin borrar el original; volvé a comprobar el origen y repetí la operación desde el principio.


### 3. Memoria

<a href="assets/ui_aprobada/02_almacenamiento.jpg"><img src="assets/ui_aprobada/02_almacenamiento.jpg" alt="Referencia visual Cyber Cockpit: Memoria" width="330"></a>

[🔍 Abrir imagen original a máxima resolución disponible](assets/ui_aprobada/02_almacenamiento.jpg)

**📘 Tutorial de Memoria — explorar y administrar archivos**

La pestaña **Memoria** muestra información disponible del almacenamiento del teléfono, una carpeta autorizada y los dispositivos que Android pueda enumerar. **No confunde una microSD con una USB sin evidencia suficiente.**

1. Entrá en **Memoria** y tocá **SELECCIONAR CARPETA REAL**. El selector de Android permite elegir una carpeta del teléfono, pendrive o volumen compatible. Confirmá el acceso solicitado; si la raíz está bloqueada por Android, elegí una subcarpeta.
2. Mirá **Almacenamiento conectado**: nombre de carpeta, tipo de volumen y etiqueta, si Android los informa. **EXTRAÍBLE** puede indicar una **USB o microSD**, no una identificación eléctrica garantizada del pendrive.
3. Compará **Capacidad del teléfono** con **Volumen autorizado**. *Total, Usado y Disponible* son distintos; si Android no ofrece datos del volumen, aparecerán **no disponibles** en vez de cifras inventadas.
4. Bajá al **Explorador de archivos**. Tocá una carpeta para entrar, **‹ ATRÁS** para volver dentro del árbol autorizado y **ACTUALIZAR** para volver a consultar su contenido. Por seguridad y rendimiento se muestran como máximo los **primeros 150 elementos** de cada carpeta.
5. Tocá **un archivo** para seleccionarlo. Se abrirá **Archivo seleccionado**, con nombre y tamaño. **Autorizar cambios** está inicialmente apagado: activalo solo si realmente querés editar, duplicar, renombrar o respaldar y eliminar.
6. **Editar texto:** los archivos de texto UTF-8 admitidos (por ejemplo .txt, .md, .csv, .json) de **hasta 256 KiB** pueden abrir el editor. **GUARDAR COMO NUEVO ARCHIVO** crea una copia editada y verifica su contenido; **no sobrescribe el original**.
7. **DUPLICAR + SHA-256** genera otra copia en la carpeta actual y comprueba que coincida con el archivo elegido.
8. **RENOMBRAR** cambia el nombre del archivo seleccionado tras confirmar. Usá un nombre válido y recordá que esta acción sí modifica el original.
9. **RESPALDAR Y ELIMINAR** pide una confirmación explícita: primero crea una copia verificada en **la misma carpeta** y luego intenta borrar el original. **No es un respaldo independiente**: si falla físicamente el dispositivo, podés perder ambos. Hacé antes una copia en otra unidad.

**Para administrar otra memoria USB**, elegí una carpeta de esa unidad mediante **SELECCIONAR CARPETA REAL**. No se promete un administrador simultáneo de múltiples raíces SAF ni acceso a carpetas que Android no haya autorizado.


### 4. Herramientas

<a href="assets/ui_aprobada/03_herramientas.jpg"><img src="assets/ui_aprobada/03_herramientas.jpg" alt="Referencia visual Cyber Cockpit: Herramientas" width="330"></a>

[🔍 Abrir imagen original a máxima resolución disponible](assets/ui_aprobada/03_herramientas.jpg)

**📘 Tutorial de Herramientas — diagnóstico y comprobación SHA-256**

Esta sección ayuda a **consultar lo que Android expone** y a verificar archivos existentes; no realiza pruebas eléctricas ni controla físicamente los puertos.

1. Entrá en **Herramientas** desde la pestaña inferior o el Tablero.
2. En **Diagnóstico y telemetría en vivo**, consultá **BATERÍA**, **USB HOST** (capacidad declarada) y **EQUIPOS** (dispositivos enumerados por Android).
3. Revisá **Fuente de energía**, **Carga externa**, **Carpeta accesible**, **Extraíble identificado** y **Carga y extraíble**. Si aparece *Ambos observados*, significa que Android informa alimentación externa y un volumen extraíble autorizado; **no certifica USB-PD ni prueba que la unidad sea USB en vez de SD**.
4. Tocá **ACTUALIZAR DIAGNÓSTICO REAL** para volver a consultar los datos del dispositivo, especialmente después de conectar o retirar accesorios. **Potencia real (W)** figura como **no disponible** cuando Android no puede medirla.
5. Para comprobar un documento, primero visitá **Memoria**, autorizá su carpeta y **tocá el archivo**. Después volvé a **Herramientas** y pulsá **CALCULAR SHA-256**. El resultado aparecerá en el **Registro local de actividad**; el cálculo **solo lee el archivo**.
6. **CERRAR SESIÓN DE ARCHIVOS** termina el acceso documental cuando no exista una operación en curso. Esperá a finalizar las copias y utilizá la **expulsión segura de Android**, si está disponible, antes de retirar el pendrive.

**Interpretación:** ver un hash SHA-256 **no significa por sí solo** que un archivo sea seguro o esté libre de malware. Para comprobar una transferencia, buscá el resultado **Copia verificada (SHA-256)** que la propia operación de copia genera.


### 5. Ajustes

<a href="assets/ui_aprobada/04_ajustes.jpg"><img src="assets/ui_aprobada/04_ajustes.jpg" alt="Referencia visual Cyber Cockpit: Ajustes" width="330"></a>

[🔍 Abrir imagen original a máxima resolución disponible](assets/ui_aprobada/04_ajustes.jpg)

**📘 Tutorial de Ajustes — personalizar sin alterar la seguridad**

La pantalla **Ajustes** cambia principalmente la **presentación visual**. No puede modificar permisos de Android ni capacidades físicas del HVE/hub.

1. Entrá en **Ajustes** desde la barra de navegación inferior.
2. **Efectos de flujo animados:** activá o desactivá las transiciones entre pantallas y las animaciones del diagrama. Desactivarlos puede resultar más cómodo si preferís una interfaz sin movimiento.
3. **Vista compacta:** reduce espacios entre paneles para que entre más información en pantallas pequeñas.
4. **Ver identificadores técnicos:** muestra datos adicionales, como **VID/PID** de dispositivos USB y el identificador del volumen, **cuando Android los informa**.
5. Las tres preferencias visuales se **guardan localmente** dentro de la aplicación; al volver a abrirla, se recupera la configuración elegida.
6. Leé **Permisos y privacidad** y **Seguridad de los archivos**: el acceso a carpetas se solicita con el **selector SAF de Android**; la escritura arranca desactivada, se conserva el archivo de origen al copiar y **RESPALDAR Y ELIMINAR** exige confirmación. No se requiere **root ni ADB**.
7. En **Acerca del proyecto** podés consultar la identidad, el creador y las advertencias de compatibilidad. Para instalar o actualizar la aplicación, utilizá **únicamente el enlace de APK estable v0.6.3 de este README**; Ajustes no instala versiones automáticamente.

**Importante:** no existen aquí controles para forzar la carga del teléfono, conocer el porcentaje del power bank, acelerar la memoria USB o prometer recuperación automática de copias interrumpidas.


## 🧑‍🏫 Tutorial completo — del primer encendido al envío de un archivo

**Ejemplo realista:** vas viajando, tenés poca batería y necesitás copiar un documento de un pendrive al teléfono para mandarlo por WhatsApp o Gmail.

1. **Revisá el equipo:** teléfono Android compatible con USB Host/OTG, **HVE/hub USB-C** con puertos de datos y **entrada de alimentación PD IN**, memoria USB y power bank compatible. Prepará cables de datos/energía adecuados.
2. **Conectá el teléfono al HVE**, la **memoria USB al puerto de datos del HVE** y el **power bank a PD IN del HVE**. La alimentación y los datos simultáneos dependen del hardware; verificá en Android que el teléfono **realmente indique carga** y que el contenido USB siga siendo accesible.
3. **Abrí OTG POWER BRIDGE** y comprobá en **Tablero** el porcentaje de batería, el estado de carga y el acceso a archivos. Si no carga mientras usás USB, la aplicación **no puede corregir esa incompatibilidad física**.
4. **Elegí el destino:** como querés llevar el documento al teléfono, tocá **Transferir → SELECCIONAR CARPETA** y autorizá una **subcarpeta del almacenamiento interno** donde quieras recibirlo. La app necesita una carpeta de destino autorizada, no acceso global a toda la memoria.
5. Activá **Habilitar escritura** y tocá **+ SELECCIONAR Y COPIAR ARCHIVO**. En el selector Android buscá la **memoria USB** y elegí el documento de origen.
6. **Esperá a que termine.** La aplicación crea un archivo nuevo, conserva el original y compara **SHA-256** de ambos. Revisá **Copia verificada** y, si es posible, abrí el archivo resultante para comprobarlo.
7. Abrí **Gmail o WhatsApp** desde Android, adjuntá el archivo guardado en el teléfono y envialo. **OTG POWER BRIDGE no envía mensajes ni correos por su cuenta**.
8. **Para copiar en sentido contrario (teléfono → USB):** en **Transferir** autorizá **una carpeta del pendrive como destino**, habilitá escritura y seleccioná **un archivo del teléfono como origen**. Las mismas reglas de copia y verificación se aplican.
9. **Para usar otra memoria:** elegí su carpeta autorizable mediante **Memoria → SELECCIONAR CARPETA REAL** o **Transferir → SELECCIONAR CARPETA**. No se abre simultáneamente una sesión independiente para cada raíz; la navegación puede cambiar entre unidades accesibles desde el selector Android.
10. **Al finalizar**, comprobá que no haya una copia en curso, tocá **DESCONECTAR / CERRAR SESIÓN DE ARCHIVOS** y usá la opción de **expulsión segura de Android**, si existe, antes de sacar las memorias.

### 🛠️ Problemas frecuentes y qué hacer

| Si ocurre esto… | Probá lo siguiente |
|:--|:--|
| **No aparece una memoria USB** | Confirmá que el teléfono admite OTG, que el hub usa un puerto de datos y que Android reconoce la unidad. Revisá el selector del sistema; el contador USB de la app puede no representar todos los volúmenes montados. |
| **Android no permite seleccionar la raíz** | Entrá a una **subcarpeta** autorizable del pendrive. Android 11+ limita ciertas raíces y ubicaciones. |
| **El botón para copiar está deshabilitado** | Seleccioná primero la **carpeta de destino** y activá **Habilitar escritura**. Si Android no concedió escritura, elegí otra carpeta autorizable. |
| **No se ve el porcentaje del power bank ni los vatios** | Es normal: la aplicación no recibe esos datos de la mayoría de equipos por las APIs disponibles. |
| **El teléfono no carga mientras la memoria funciona** | Verificá que el **teléfono, el hub con PD IN y los cables** admitan carga y OTG simultáneos. No lo atribuyas a un botón de la app. |
| **Se cortó una transferencia** | Revisá el original y el espacio disponible, restablecé la conexión y **repetí la copia desde cero**. La versión 0.6.3 **no ofrece reanudación automática**. |
| **El archivo no aparece en Gmail o WhatsApp** | Comprobá en el gestor de archivos de Android la carpeta donde se creó la copia y adjuntalo desde la app de mensajería o correo. |
| **No veo todos los documentos de la carpeta** | El explorador muestra **hasta 150 elementos** por carpeta. Usá Android para organizar archivos en subcarpetas más pequeñas. |

> 🛡️ **Seguridad:** para archivos importantes, comprobá que el archivo nuevo se abre, guardá una copia independiente y evitá desconectar dispositivos durante la escritura. **No confundas** acceso SAF, USB Host y carga simultánea: son capacidades distintas.


## 🧭 Funciones de cada pantalla

| Pantalla | Qué ofrece |
|:--|:--|
| **Tablero** | Batería y estado de carga reales, estado USB/OTG accesible, conexión autorizada y resumen de transferencias. |
| **Transferencias** | Seleccionar archivos y destinos autorizados, copiar y comprobar su integridad con SHA-256; progreso real cuando existe una operación. |
| **Memoria** | Mostrar capacidad ocupada y libre disponible a través de Android, explorar carpetas autorizadas y operaciones documentales. |
| **Herramientas** | Diagnóstico USB/Host, identificación de dispositivos cuando se expone y comprobación SHA-256. |
| **Ajustes** | Preferencias de visualización, información y controles de seguridad. |

## 🔌 Conectar teléfono, power bank y memoria USB

**Necesitás:** teléfono Android con capacidades OTG compatibles, **hub USB-C con puertos de datos y entrada PD IN**, un pendrive USB-A/USB-C (o adaptador de datos apropiado) y una fuente de energía compatible.

1. Enchufá el **hub USB-C/OTG** al teléfono.
2. Conectá la **memoria USB** a un puerto **USB DATA** del hub.
3. Conectá el **power bank** a **PD IN** del hub, **si es compatible**.
4. Abrí **OTG POWER BRIDGE**, verificá batería, carga y dispositivos visibles.
5. En **Conectar**, autorizá una carpeta mediante el selector de Android (**SAF**).
6. Seleccioná el archivo y la carpeta de destino; ejecutá la copia y comprobá el resultado **SHA-256**.
7. Al terminar, verificá el fin de la escritura antes de retirar la memoria. **Desconectar** cierra la sesión de la app, no expulsa físicamente el dispositivo.

> ⚠️ **Compatibilidad:** USB Host no garantiza carga + OTG simultáneos. Android puede mostrar memorias en SAF sin enumerarlas en USB. El porcentaje del **power bank** y la potencia eléctrica se muestran como **no disponibles** si el hardware no ofrece telemetría. Usar un hub con varios puertos no garantiza que Android reconozca todas las unidades.

## 📲 Instalación en Android

**[➡️ DESCARGAR OTG-POWER-BRIDGE.apk — VERSIÓN ESTABLE](https://github.com/luis171987/OTG-POWER-BRIDGE/releases/latest/download/OTG-POWER-BRIDGE.apk)**

1. Abrí el enlace oficial anterior desde el teléfono.
2. Descargá **`OTG-POWER-BRIDGE.apk`**; es el instalador, **no** el archivo «Source code ZIP».
3. Abrilo con el gestor de archivos y autorizá la instalación desde esa aplicación únicamente si Android lo solicita.
4. No desactives Play Protect. Si una versión de prueba antigua tiene firma diferente, respaldá sus datos antes de desinstalarla.
5. Conectá los accesorios compatibles y concedé los permisos de acceso a archivos que necesites.

**Datos del APK oficial V0.6.3:** paquete `bo.genai.usbpowerbridge` · `versionCode 10` · Android mínimo definido por el proyecto: **8.0 (API 26)**.

**SHA-256 del APK publicado** (verificado en el lanzamiento):

```text
2fce8d4def837e4b0734c4a72f4d768b3de1451f5453fd2860a3a75a50a67aab
```

## 🛡️ Privacidad y precisión

- Los archivos se gestionan mediante permisos **SAF** del sistema; no es necesario enviarlos a un servidor para copiar entre carpetas autorizadas.
- Los indicadores muestran información disponible en Android. Si un dato no está accesible, la interfaz debe decirlo expresamente.
- La función de carga depende del hardware compatible; la app **no activa físicamente USB-PD** y **supervisa, no controla físicamente la alimentación**. La [verificación estática de v0.6.3](docs/versiones/2026-11-05_v0.6.3/README.md) no acredita por sí sola aceptación funcional ni física.
- **No se requiere root** para las funciones normales de la aplicación.

## 📚 Documentación e historial técnico

- [Lanzamiento estable V0.6.3](https://github.com/luis171987/OTG-POWER-BRIDGE/releases/tag/v0.6.3).
- [Ver todas las versiones publicadas](https://github.com/luis171987/OTG-POWER-BRIDGE/releases).
- [Tutorial de conexión física](docs/MODO_REAL_ANDROID.md).
- [Historial y auditorías del proyecto](docs/versiones/README.md) (si está disponible).

Los diseños y documentos antiguos siguen formando parte del **historial técnico del repositorio**; no se muestran como una versión actualmente instalable en esta portada.

## 📢 Compartir en Facebook y WhatsApp

**[🔗 Compartir el repositorio oficial](https://github.com/luis171987/OTG-POWER-BRIDGE)**

Para cambiar la miniatura del enlace se debe actualizar **GitHub → Settings → Social preview** con la portada Cyber Cockpit aprobada. El README y su primera imagen **no modifican automáticamente** esa miniatura. Facebook y WhatsApp pueden conservar imágenes anteriores en caché.

---

**OTG POWER BRIDGE © GENAI INDUSTRIES** · Creado por **Luis Carlos Moreno Jiménez** · Santa Cruz de la Sierra, Bolivia 🇧🇴

package bo.genai.usbpowerbridge

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Navy = Color(0xFF071525)
private val Panel = Color(0xFF132C44)
private val Sky = Color(0xFF54D7FF)
private val Orange = Color(0xFFFFAA40)
private val Green = Color(0xFF72E4A2)
private val Red = Color(0xFFFF7782)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Sky,
                    secondary = Orange,
                    background = Navy,
                    surface = Panel,
                    onSurface = Color.White,
                    onBackground = Color.White
                )
            ) { RealBridgeScreen() }
        }
    }
}

private data class TransferDisplay(
    val name: String,
    val totalBytes: Long?,
    val copiedBytes: Long = 0L,
    val stage: String = "Copiando",
    val verified: Boolean = false
)

private data class PowerInfo(
    val percent: Int = -1,
    val externalPower: Boolean = false,
    val charging: Boolean = false,
    val source: String = "Desconocida"
)

@Composable
private fun observePower(): PowerInfo {
    val context = LocalContext.current
    var state by remember { mutableStateOf(PowerInfo()) }
    DisposableEffect(context) {
        fun parse(intent: Intent?): PowerInfo {
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
            val batteryStatus = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val pct = if (level >= 0 && scale > 0) level * 100 / scale else -1
            val source = when (plugged) {
                BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                BatteryManager.BATTERY_PLUGGED_AC -> "Adaptador AC"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Inalámbrica"
                else -> "Sin fuente externa detectada"
            }
            return PowerInfo(pct, plugged != 0,
                batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING, source)
        }
        state = parse(context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)))
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) { state = parse(intent) }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        onDispose { context.unregisterReceiver(receiver) }
    }
    return state
}

@Composable
private fun RealBridgeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val power = observePower()
    var usb by remember { mutableStateOf(UsbProbe.read(context)) }
    val folders = remember { mutableStateListOf<DocumentFile>() }
    val root = folders.firstOrNull()
    val current = folders.lastOrNull()
    var accessible by remember { mutableStateOf(false) }
    var storageEvidence by remember { mutableStateOf(StorageEvidence("Sin carpeta seleccionada", null)) }
    var rootUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var writeAllowed by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var updateCounter by remember { mutableIntStateOf(0) }
    var items by remember { mutableStateOf<List<DocumentFile>>(emptyList()) }
    var selected by remember { mutableStateOf<DocumentFile?>(null) }
    var editor by remember { mutableStateOf("") }
    var editorReady by remember { mutableStateOf(false) }
    var renameDialog by remember { mutableStateOf(false) }
    var renameName by remember { mutableStateOf("") }
    var deleteDialog by remember { mutableStateOf(false) }
    var log by remember { mutableStateOf("Lista para detectar dispositivos reales. No se ha modificado ningún archivo.") }
    var transfer by remember { mutableStateOf<TransferDisplay?>(null) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    // El USB puede entrar o salir durante el uso; se vuelve a leer la API del sistema.
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                usb = UsbProbe.read(context)
                log = "Cambio de dispositivo USB observado. Comprueba el acceso a archivos."
            }
        }
        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        onDispose { context.unregisterReceiver(receiver) }
    }

    LaunchedEffect(root?.uri) {
        while (true) {
            usb = UsbProbe.read(context)
            rootUri?.let { activeUri ->
                storageEvidence = withContext(Dispatchers.IO) {
                    RealStorage.inspect(context, activeUri)
                }
            }
            accessible = withContext(Dispatchers.IO) {
                root?.let { runCatching { it.isDirectory && it.canRead() }.getOrDefault(false) } ?: false
            }
            delay(5000)
        }
    }
    LaunchedEffect(current?.uri, updateCounter, accessible) {
        items = if (current == null || !accessible) emptyList() else withContext(Dispatchers.IO) {
            runCatching {
                current.listFiles()
                    .sortedWith(compareBy<DocumentFile> { !it.isDirectory }.thenBy { it.name?.lowercase() })
                    .take(150)
            }.getOrDefault(emptyList())
        }
    }

    fun closeSession() {
        // Desconectar termina la sesión SAF de esta app; no apaga el puerto USB.
        val uri = rootUri
        folders.clear()
        selected = null
        editor = ""
        editorReady = false
        accessible = false
        storageEvidence = StorageEvidence("Sin carpeta seleccionada", null)
        writeAllowed = false
        transfer = null
        rootUri = null
        if (uri != null) {
            runCatching {
                context.contentResolver.releasePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }.onFailure {
                runCatching {
                    context.contentResolver.releasePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
            }
        }
        log = "Sesión de archivos cerrada. No se ha expulsado físicamente la memoria USB."
    }

    fun safely(title: String, action: () -> String) {
        if (busy || !writeAllowed || !accessible || current == null) {
            log = "Operación no iniciada: falta conexión accesible o autorización para escribir."
            return
        }
        busy = true
        scope.launch {
            val result = withContext(Dispatchers.IO) { runCatching(action) }
            log = result.fold({ "$title: $it" }, { "$title: ${it.message ?: "Error"}" })
            busy = false
            updateCounter++
        }
    }

    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri == null) {
            log = "Conexión cancelada: no se seleccionó carpeta."
        } else {
            val newRoot = DocumentFile.fromTreeUri(context, uri)
            val isValid = runCatching { newRoot != null && newRoot.isDirectory && newRoot.canRead() }
                .getOrDefault(false)
            if (!isValid || newRoot == null) {
                log = "Android no concedió acceso a la carpeta."
            } else {
                // Conservar permiso solo hasta que el usuario pulse Desconectar.
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }.onFailure {
                    runCatching {
                        context.contentResolver.takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    }
                }
                val previousUri = rootUri
                if (previousUri != null && previousUri != uri) {
                    runCatching {
                        context.contentResolver.releasePersistableUriPermission(
                            previousUri, Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    }.onFailure {
                        runCatching {
                            context.contentResolver.releasePersistableUriPermission(
                                previousUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        }
                    }
                }
                folders.clear()
                folders.add(newRoot)
                rootUri = uri
                storageEvidence = RealStorage.inspect(context, uri)
                accessible = true
                selected = null
                editorReady = false
                writeAllowed = false
                usb = UsbProbe.read(context)
                updateCounter++
                log = "Carpeta accesible desde Android: ${newRoot.name ?: "sin nombre"}."
            }
        }
    }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val dest = current
        if (uri == null) {
            log = "Selección de archivo cancelada."
        } else if (dest == null || !accessible || !writeAllowed) {
            log = "No se copió: conecta una carpeta y autoriza cambios."
        } else {
            val origin = DocumentFile.fromSingleUri(context, uri)
            if (origin == null || !origin.isFile) {
                log = "No se pudo leer el archivo seleccionado."
            } else {
                val fileName = origin.name ?: "archivo"
                val totalBytes = origin.length().takeIf { it > 0L }
                transfer = TransferDisplay(fileName, totalBytes)
                safely("Transferencia") {
                    try {
                        val copy = SafeDocumentOps.copyWithVerification(
                            context.contentResolver, dest, origin, "COPIA_VERIFICADA"
                        ) { copied, total ->
                            mainHandler.post {
                                transfer = TransferDisplay(fileName, total, copied, "Copiando")
                            }
                        }
                        mainHandler.post {
                            transfer = TransferDisplay(
                                fileName, totalBytes,
                                if (totalBytes != null) totalBytes else origin.length(),
                                "Copia verificada (SHA-256)", true
                            )
                        }
                        "${copy.name} creada y verificada con SHA-256. Original conservado."
                    } catch (e: Exception) {
                        mainHandler.post {
                            transfer = transfer?.copy(stage = "Error: copia no completada", verified = false)
                        }
                        throw e
                    }
                }
            }
        }
    }

    val session = root != null && accessible
    val removable = session && storageEvidence.removable == true
    val simultaneouslyObserved = removable && power.externalPower
    val statusTitle = if (session) "Conectado a carpeta" else "Desconectado"

    Column(
        modifier = Modifier.fillMaxSize().background(Navy)
            .verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.brand_logo),
            contentDescription = "Logotipo oficial OTG POWER BRIDGE",
            modifier = Modifier.fillMaxWidth().height(135.dp),
            contentScale = ContentScale.Fit
        )
        Text("OTG POWER BRIDGE", color = Color.White,
            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Text("GENAI INDUSTRIES · Diagnóstico y archivos reales", color = Sky,
            style = MaterialTheme.typography.bodyMedium)

        PanelCard {
            Text("●  $statusTitle", color = if (session) Green else Red,
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                if (session) "Acceso SAF concedido a una carpeta real."
                else "Pulsa Conectar para elegir una carpeta desde Android.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFD0E6F5)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { folderPicker.launch(null) },
                    enabled = !busy,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF086EA5))
                ) { Text("Conectar", color = Color.White) }
                OutlinedButton(
                    onClick = { closeSession() }, enabled = root != null && !busy,
                    modifier = Modifier.weight(1f)
                ) { Text("Desconectar") }
            }
            Text("Conectar abre el selector de carpetas. Desconectar cierra el acceso de la app; no conecta, apaga ni expulsa físicamente el USB.",
                style = MaterialTheme.typography.labelSmall, color = Color(0xFFC2D4E5))
        }

        PanelCard {
            Text("BATERÍA Y ALIMENTACIÓN · EN VIVO", color = Sky, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("TELÉFONO", modifier = Modifier.weight(1f), color = Color.White,
                    fontWeight = FontWeight.SemiBold)
                Text(if (power.percent >= 0) "${power.percent}%" else "—%",
                    fontSize = 26.sp, color = if (power.percent in 0..15) Orange else Green,
                    fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(
                progress = { (power.percent.coerceAtLeast(0) / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(10.dp),
                color = if (power.percent in 0..15) Orange else Sky,
                trackColor = Navy
            )
            Text("Estado: ${if (power.charging) "Cargando" else if (power.externalPower) "Alimentación conectada, sin carga confirmada" else "Sin carga externa"}",
                color = Color.White)
            Text("Fuente: ${power.source}", color = Color(0xFFC6DBEB))
            HorizontalDivider(color = Color(0xFF24577B))
            Text("POWER BANK · PORCENTAJE", color = Color.White,
                fontWeight = FontWeight.SemiBold)
            Text("—%  ·  No disponible", color = Orange,
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Este power bank no comunica su batería a Android por USB de carga.",
                color = Color(0xFFBDD5E4), style = MaterialTheme.typography.bodySmall)
            Text("Consulta la pantalla física del power bank. El porcentaje del teléfono NO corresponde al power bank.",
                color = Color(0xFFBDD5E4), style = MaterialTheme.typography.labelSmall)
        }

        PanelCard {
            Text("INFORMACIÓN REAL DEL USB Y VOLUMEN", color = Sky,
                fontWeight = FontWeight.Bold)
            Text("Carpeta: ${root?.name ?: "Sin seleccionar"}", color = Color.White)
            InfoRow("Origen", storageEvidence.description)
            InfoRow("Etiqueta del volumen", storageEvidence.volumeLabel ?: "No informada")
            InfoRow("Identificador del volumen", storageEvidence.volumeId ?: "No disponible")
            InfoRow("Capacidad total", TelemetryFormats.bytes(storageEvidence.totalBytes))
            InfoRow("Espacio libre", TelemetryFormats.bytes(storageEvidence.availableBytes))
            val totalVolumeBytes = storageEvidence.totalBytes
            val freeVolumeBytes = storageEvidence.availableBytes
            val used = if (totalVolumeBytes != null && freeVolumeBytes != null)
                (totalVolumeBytes - freeVolumeBytes).coerceAtLeast(0L) else null
            InfoRow("Espacio utilizado", TelemetryFormats.bytes(used))
            if (usb.devices.isEmpty()) {
                Text("Fabricante/modelo USB: no informados por Android. Un volumen montado puede no aparecer en la API USB.",
                    color = Color(0xFFBDD5E4), style = MaterialTheme.typography.bodySmall)
            } else {
                usb.devices.forEachIndexed { index, device ->
                    HorizontalDivider(color = Color(0xFF24577B))
                    Text("Dispositivo USB ${index + 1}", color = Orange,
                        fontWeight = FontWeight.SemiBold)
                    InfoRow("Fabricante", device.manufacturer ?: "No informado")
                    InfoRow("Producto", device.product ?: "No informado")
                    InfoRow("Identificación", "VID %04X · PID %04X".format(device.vendorId, device.productId))
                    InfoRow("Interfaz", if (device.massStorageDescriptor) "Almacenamiento masivo" else "Otra clase USB")
                }
            }
            Text("Android no informa con fiabilidad el tipo físico de conector (USB-A/C/micro-USB). Un descriptor USB no se asocia automáticamente a la carpeta elegida.",
                color = Color(0xFFBDD5E4), style = MaterialTheme.typography.labelSmall)
            OutlinedButton(onClick = {
                usb = UsbProbe.read(context)
                rootUri?.let { storageEvidence = RealStorage.inspect(context, it) }
                updateCounter++
            }) { Text("Actualizar información USB") }
        }

        PanelCard {
            Text("ESTADO REAL DEL DISPOSITIVO", color = Sky, fontWeight = FontWeight.Bold)
            SignalRow("Energía externa", if (power.externalPower) "Detectada" else "No detectada", power.externalPower)
            SignalRow("Batería del teléfono", "${if (power.percent >= 0) "${power.percent}%" else "Sin lectura"} · ${if (power.charging) "Cargando" else "No informa carga"}", power.charging)
            SignalRow("USB / OTG", "Host: ${if (usb.hostFeatureReported) "soportado" else "no declarado"}; API: ${usb.devices.size} dispositivo(s)", usb.devices.isNotEmpty())
            SignalRow("Acceso a carpeta", if (session) "Disponible" else "Sin autorización", session)
            SignalRow("Carga + extraíble", if (simultaneouslyObserved) "Ambos observados" else "No confirmado", simultaneouslyObserved)
            Text(storageEvidence.description, color = Color(0xFFD0E6F5),
                style = MaterialTheme.typography.bodySmall)
            Text("La API USB puede omitir memorias montadas por Android. La app no mide potencia PD ni identifica el power bank.",
                color = Color(0xFFB9D2E1), style = MaterialTheme.typography.labelSmall)
            usb.devices.take(6).forEach { device ->
                Text(
                    "USB observado: VID=${"%04X".format(device.vendorId)} " +
                        "PID=${"%04X".format(device.productId)} " +
                        (if (device.massStorageDescriptor) "(clase almacenamiento)" else "(otro dispositivo)"),
                    color = Color(0xFFC6DBEB), style = MaterialTheme.typography.labelSmall
                )
            }
            usb.error?.let { Text(it, color = Orange) }
            OutlinedButton(onClick = {
                usb = UsbProbe.read(context)
                updateCounter++
                log = "Estado Android actualizado sin modificar archivos."
            }) { Text("Actualizar diagnóstico") }
        }

        PanelCard {
            Text("PROGRESO DE TRANSFERENCIA REAL", color = Sky,
                fontWeight = FontWeight.Bold)
            val tx = transfer
            if (tx == null) {
                Text("Sin transferencias iniciadas en esta sesión.", color = Color.White)
            } else {
                Text("Archivo: ${tx.name}", color = Color.White, fontWeight = FontWeight.SemiBold)
                InfoRow("Tamaño total", TelemetryFormats.bytes(tx.totalBytes))
                InfoRow("Transferido", TelemetryFormats.bytes(tx.copiedBytes))
                val pct = TelemetryFormats.percent(tx.copiedBytes, tx.totalBytes)
                Text("${if (pct != null) "$pct%" else "Porcentaje no disponible"} · ${tx.stage}",
                    color = if (tx.verified) Green else Orange)
                if (pct == null) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    LinearProgressIndicator(
                        progress = { pct / 100f }, modifier = Modifier.fillMaxWidth().height(10.dp),
                        color = if (tx.verified) Green else Sky, trackColor = Navy
                    )
                }
                if (tx.verified) Text("Integridad SHA-256 comprobada. Original conservado.",
                    color = Green, style = MaterialTheme.typography.bodySmall)
                else Text("El 100% de copia no equivale a transferencia verificada hasta finalizar SHA-256.",
                    color = Color(0xFFBDD5E4), style = MaterialTheme.typography.labelSmall)
            }
        }

        PanelCard {
            Text("TRANSFERENCIA Y EXPLORADOR", color = Sky, fontWeight = FontWeight.Bold)
            Text("Carpeta: ${current?.name ?: "Sin seleccionar"}", color = Color.White)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Autorizar escritura", modifier = Modifier.weight(1f), color = Color.White)
                Switch(
                    checked = writeAllowed,
                    onCheckedChange = { writeAllowed = it },
                    enabled = session && !busy
                )
            }
            Text("Por defecto, solo lectura. Para copiar, activa escritura y utiliza archivos de prueba primero.",
                color = Color(0xFFBDD5E4), style = MaterialTheme.typography.bodySmall)
            Button(
                onClick = { filePicker.launch(arrayOf("*/*")) },
                enabled = session && writeAllowed && !busy,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Copiar archivo a carpeta · SHA-256") }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        if (folders.size > 1) {
                            folders.removeAt(folders.lastIndex)
                            selected = null
                            editorReady = false
                        }
                    }, enabled = folders.size > 1 && !busy
                ) { Text("Atrás") }
                OutlinedButton(onClick = { updateCounter++ }, enabled = session && !busy) {
                    Text("Actualizar archivos")
                }
            }
            if (session && items.isEmpty()) {
                Text("Carpeta vacía o sin elementos visibles.", color = Color.White)
            }
            items.forEach { file ->
                Text(
                    "${if (file.isDirectory) "📁" else "📄"} ${file.name ?: "Sin nombre"}",
                    modifier = Modifier.fillMaxWidth()
                        .clickable(enabled = !busy) {
                            if (file.isDirectory) {
                                folders.add(file)
                                selected = null
                                editorReady = false
                            } else {
                                selected = file
                                editorReady = false
                                editor = ""
                                log = "Archivo real seleccionado: ${file.name}"
                                if (SafeDocumentOps.isEditableText(file)) {
                                    busy = true
                                    scope.launch {
                                        val result = withContext(Dispatchers.IO) {
                                            runCatching {
                                                SafeDocumentOps.readText(context.contentResolver, file)
                                            }
                                        }
                                        result.onSuccess {
                                            editor = it
                                            editorReady = true
                                        }.onFailure {
                                            log = "No se pudo leer el texto: ${it.message}"
                                        }
                                        busy = false
                                    }
                                }
                            }
                        }.padding(vertical = 7.dp),
                    color = if (selected?.uri == file.uri) Orange else Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (items.size >= 150) {
                Text("Se muestran los primeros 150 elementos.", style = MaterialTheme.typography.labelSmall)
            }
        }

        if (session && selected != null) {
            PanelCard {
                val chosen = selected
                Text("ARCHIVO SELECCIONADO", color = Sky, fontWeight = FontWeight.Bold)
                Text(chosen?.name ?: "", color = Color.White)
                Text("Tamaño: ${TelemetryFormats.bytes(chosen?.length())}",
                    color = Color(0xFFD0E6F5))
                if (editorReady) {
                    OutlinedTextField(
                        value = editor,
                        onValueChange = { editor = it },
                        label = { Text("Editor de texto UTF-8") },
                        minLines = 3, maxLines = 8, modifier = Modifier.fillMaxWidth()
                    )
                    Button(onClick = {
                        val origin = selected ?: return@Button
                        val destination = current ?: return@Button
                        val text = editor
                        safely("Copia editada") {
                            val created = SafeDocumentOps.saveEditedVersion(
                                context.contentResolver, destination, origin, text
                            )
                            "${created.name}; original intacto."
                        }
                    }, enabled = !busy && writeAllowed) { Text("Guardar como nueva copia") }
                }
                OutlinedButton(onClick = {
                    val origin = selected ?: return@OutlinedButton
                    val destination = current ?: return@OutlinedButton
                    safely("Duplicación") {
                        val created = SafeDocumentOps.copyWithVerification(
                            context.contentResolver, destination, origin, "GENAI_COPIA"
                        )
                        "${created.name} con SHA-256 verificado."
                    }
                }, enabled = !busy && writeAllowed) { Text("Duplicar y verificar") }
                OutlinedButton(onClick = {
                    renameName = chosen?.name.orEmpty()
                    renameDialog = true
                }, enabled = !busy && writeAllowed) { Text("Renombrar") }
                OutlinedButton(onClick = { deleteDialog = true }, enabled = !busy && writeAllowed) {
                    Text("Eliminar con respaldo previo", color = Red)
                }
            }
        }

        PanelCard {
            Text("REGISTRO LOCAL", color = Sky, fontWeight = FontWeight.Bold)
            Text(if (busy) "Operación de archivos en curso…" else log, color = Color.White)
            Text("Sin root · Sin ADB · Sin permisos globales de almacenamiento",
                style = MaterialTheme.typography.labelSmall, color = Color(0xFFB9D2E1))
        }
        Text("GENAI INDUSTRIES · v0.5.0 · Modo real · Compatibilidad física según teléfono y hub",
            color = Color(0xFFBED2E3), style = MaterialTheme.typography.labelSmall)
    }

    if (renameDialog) {
        AlertDialog(
            onDismissRequest = { renameDialog = false },
            title = { Text("Renombrar archivo") },
            text = { OutlinedTextField(value = renameName, onValueChange = { renameName = it }) },
            confirmButton = {
                TextButton(onClick = {
                    renameDialog = false
                    val origin = selected ?: return@TextButton
                    val newName = renameName
                    safely("Renombrado") {
                        SafeDocumentOps.renameFile(origin, newName)
                        "Archivo renombrado a $newName"
                    }
                    selected = null
                    editorReady = false
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { renameDialog = false }) { Text("Cancelar") }
            }
        )
    }
    if (deleteDialog) {
        AlertDialog(
            onDismissRequest = { deleteDialog = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("Se crea una copia SHA-256 en la MISMA carpeta y después se borra el original. Esto NO protege frente a fallas físicas de la memoria. Haz antes una copia independiente.") },
            confirmButton = {
                TextButton(onClick = {
                    deleteDialog = false
                    val origin = selected ?: return@TextButton
                    val dest = current ?: return@TextButton
                    safely("Eliminación") {
                        val backup = SafeDocumentOps.deleteAfterBackup(
                            context.contentResolver, dest, origin
                        )
                        "Original eliminado. Respaldo conservado: ${backup.name}"
                    }
                    selected = null
                    editorReady = false
                }) { Text("Respaldar y eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun PanelCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF24577B), RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp), color = Panel
    ) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(9.dp), content = content)
    }
}

@Composable
private fun SignalRow(label: String, detail: String, positive: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("●", color = if (positive) Green else Orange, modifier = Modifier.padding(end = 8.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(detail, color = Color(0xFFC6DBEB), style = MaterialTheme.typography.bodySmall)
        }
    }
}


@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(label, modifier = Modifier.weight(0.44f), color = Color(0xFFAACDDF),
            style = MaterialTheme.typography.bodySmall)
        Text(value, modifier = Modifier.weight(0.56f), color = Color.White,
            style = MaterialTheme.typography.bodySmall)
    }
}

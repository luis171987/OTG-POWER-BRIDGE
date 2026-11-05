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

internal data class TransferDisplay(
    val name: String,
    val totalBytes: Long?,
    val copiedBytes: Long = 0L,
    val stage: String = "Copiando",
    val verified: Boolean = false,
    val startElapsedMs: Long = 0L
)

internal data class PowerInfo(
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
                val startMs = android.os.SystemClock.elapsedRealtime()
                transfer = TransferDisplay(fileName, totalBytes, startElapsedMs = startMs)
                safely("Transferencia") {
                    try {
                        val copy = SafeDocumentOps.copyWithVerification(
                            context.contentResolver, dest, origin, "COPIA_VERIFICADA"
                        ) { copied, total ->
                            mainHandler.post {
                                transfer = TransferDisplay(fileName, total, copied, "Copiando", startElapsedMs = startMs)
                            }
                        }
                        mainHandler.post {
                            transfer = TransferDisplay(
                                fileName, totalBytes,
                                if (totalBytes != null) totalBytes else origin.length(),
                                "Copia verificada (SHA-256)", true, startMs
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

    val phoneSpace = remember(updateCounter) { DeviceSpace.read(context) }
    BridgeInterface(
        data = BridgeUiData(
            power = power,
            usb = usb,
            evidence = storageEvidence,
            selectedRoot = root,
            currentFolder = current,
            session = session,
            removable = removable,
            simultaneous = simultaneouslyObserved,
            writeAllowed = writeAllowed,
            busy = busy,
            transfer = transfer,
            items = items,
            selected = selected,
            editor = editor,
            editorReady = editorReady,
            hasParent = folders.size > 1,
            log = log,
            phoneSpace = phoneSpace
        ),
        actions = BridgeActions(
            connect = { folderPicker.launch(null) },
            disconnect = { closeSession() },
            refresh = {
                usb = UsbProbe.read(context)
                rootUri?.let { storageEvidence = RealStorage.inspect(context, it) }
                updateCounter++
                log = "Diagnóstico Android actualizado sin modificar archivos."
            },
            writeChange = { writeAllowed = it },
            selectSourceFile = { filePicker.launch(arrayOf("*/*")) },
            upFolder = {
                if (folders.size > 1) {
                    folders.removeAt(folders.lastIndex)
                    selected = null
                    editorReady = false
                    updateCounter++
                }
            },
            openItem = { file ->
                if (!busy) {
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
                                    runCatching { SafeDocumentOps.readText(context.contentResolver, file) }
                                }
                                result.onSuccess { editor = it; editorReady = true }
                                    .onFailure { log = "Lectura de texto: ${it.message}" }
                                busy = false
                            }
                        }
                    }
                }
            },
            editText = { editor = it },
            saveEdited = {
                val origin = selected
                val destination = current
                if (origin != null && destination != null) {
                    val text = editor
                    safely("Copia editada") {
                        val created = SafeDocumentOps.saveEditedVersion(
                            context.contentResolver, destination, origin, text
                        )
                        "${created.name}; original conservado."
                    }
                }
            },
            duplicate = {
                val origin = selected
                val destination = current
                if (origin != null && destination != null) {
                    safely("Duplicación") {
                        val created = SafeDocumentOps.copyWithVerification(
                            context.contentResolver, destination, origin, "GENAI_COPIA"
                        )
                        "${created.name}; SHA-256 verificado."
                    }
                }
            },
            rename = {
                renameName = selected?.name.orEmpty()
                renameDialog = true
            },
            delete = { deleteDialog = true },
            checksum = {
                val doc = selected
                if (doc == null || !doc.isFile) {
                    log = "Selecciona un archivo para calcular su SHA-256."
                } else if (!busy) {
                    busy = true
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            runCatching { SafeDocumentOps.checksum(context.contentResolver, doc) }
                        }
                        log = result.fold(
                            { "SHA-256 (${doc.name}): $it" },
                            { "No fue posible verificar SHA-256: ${it.message}" }
                        )
                        busy = false
                    }
                }
            }
        )
    )

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

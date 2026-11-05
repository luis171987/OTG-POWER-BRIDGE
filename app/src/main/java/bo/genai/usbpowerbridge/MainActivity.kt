package bo.genai.usbpowerbridge

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import bo.genai.core.Connection
import bo.genai.core.ConnectionModel
import bo.genai.core.Validation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Color(0xFF65D6F9), secondary = Color(0xFFFF9A31),
                background = Color(0xFF0A1522), surface = Color(0xFF152638)
            )) { BridgeScreen() }
        }
    }
}

private data class PowerInfo(
    val percent: Int = -1,
    val plugged: Boolean = false,
    val charging: Boolean = false,
    val source: String = "Sin medición"
)

@Composable
private fun observePower(): PowerInfo {
    val context = LocalContext.current
    var info by remember { mutableStateOf(PowerInfo()) }
    DisposableEffect(context) {
        fun fromIntent(intent: Intent?): PowerInfo {
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
            val pct = if (scale > 0 && level >= 0) 100 * level / scale else -1
            val plug = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
            val source = when (plug) {
                BatteryManager.BATTERY_PLUGGED_AC -> "Adaptador / AC"
                BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Carga inalámbrica"
                else -> "Sin fuente externa detectada"
            }
            val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val charging = status == BatteryManager.BATTERY_STATUS_CHARGING
            return PowerInfo(pct, plug != 0, charging, source)
        }
        info = fromIntent(context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)))
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, i: Intent?) { info = fromIntent(i) }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        onDispose { context.unregisterReceiver(receiver) }
    }
    return info
}

@Composable
private fun BridgeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val power = observePower()
    var demo by remember { mutableStateOf(true) }
    var writeEnabled by remember { mutableStateOf(false) }
    var usbSnapshot by remember { mutableStateOf(UsbSnapshot(false, emptyList())) }
    var externalVolumeHint by remember { mutableStateOf("Sin carpeta autorizada") }
    var scenario by remember { mutableStateOf(Connection.HUB_UNVERIFIED) }
    val simulation = ConnectionModel.evaluate(scenario)
    val path = remember { mutableStateListOf<DocumentFile>() }
    val current = path.lastOrNull()
    var entries by remember { mutableStateOf<List<DocumentFile>>(emptyList()) }
    var selected by remember { mutableStateOf<DocumentFile?>(null) }
    var editor by remember { mutableStateOf("") }
    var editorReady by remember { mutableStateOf(false) }
    var renameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    var deleteDialog by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Sandbox iniciado: no se ha modificado ningún archivo.") }
    var busy by remember { mutableStateOf(false) }
    var refreshVersion by remember { mutableIntStateOf(0) }

    val folderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val read = Intent.FLAG_GRANT_READ_URI_PERMISSION
        val write = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        val permission = runCatching {
            context.contentResolver.takePersistableUriPermission(uri, read or write)
            "lectura/escritura solicitadas"
        }.getOrElse {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, read) }
            "lectura disponible; escritura dependerá del proveedor"
        }
        val treeId = runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrDefault("")
        externalVolumeHint = when {
            uri.authority == "com.android.externalstorage.documents" && treeId.startsWith("primary:", true) ->
                "Almacenamiento principal (no USB)"
            uri.authority == "com.android.externalstorage.documents" && treeId.contains(':') ->
                "Volumen no primario: puede ser USB o tarjeta SD (no certificado)"
            else -> "Proveedor de documentos: origen físico indeterminado"
        }
        val root = DocumentFile.fromTreeUri(context, uri)
        if (root == null || !root.isDirectory) {
            message = "No se pudo abrir la carpeta seleccionada."
        } else {
            path.clear(); path.add(root); selected = null; editorReady = false
            message = "Carpeta autorizada ($permission). El acceso no identifica el fabricante USB."
            refreshVersion++
        }
    }
    LaunchedEffect(current?.uri, refreshVersion) {
        entries = emptyList()
        if (current != null) {
            entries = withContext(Dispatchers.IO) {
                runCatching { current.listFiles().sortedWith(compareBy<DocumentFile> { !it.isDirectory }.thenBy { it.name?.lowercase() }).take(80) }
                    .getOrElse { emptyList() }
            }
        }
    }

    fun runSafAction(title: String, block: () -> String) {
        if (demo || busy || !writeEnabled) return
        busy = true
        scope.launch {
            val result = withContext(Dispatchers.IO) { runCatching(block) }
            message = result.fold(onSuccess = { "$title: $it" }, onFailure = { "$title: error: ${it.message}" })
            busy = false
            refreshVersion++
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val destination = path.lastOrNull()
        if (uri == null) {
            message = "Importación cancelada por el usuario."
        } else if (demo || !writeEnabled || destination == null) {
            message = "No se importó: autoriza la carpeta de destino y habilita cambios."
        } else {
            val origin = DocumentFile.fromSingleUri(context, uri)
            if (origin == null || !origin.isFile) {
                message = "No se pudo abrir el archivo seleccionado."
            } else {
                runSafAction("Importación SAF") {
                    val result = SafeDocumentOps.copyWithVerification(
                        context.contentResolver, destination, origin, "COPIA_VERIFICADA"
                    )
                    "Transferencia completada y comprobada con SHA-256: ${result.name}"
                }
            }
        }
    }

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.brand_logo),
            contentDescription = "Logotipo oficial OTG POWER BRIDGE de GENAI INDUSTRIES",
            modifier = Modifier.fillMaxWidth().height(175.dp),
            contentScale = ContentScale.Fit
        )
        Text("OTG POWER BRIDGE", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Text("GENAI INDUSTRIES • Compatible según capacidades de tu Android", style = MaterialTheme.typography.bodyMedium)
        InfoCard {
            Text("⚠️ Capacidad física NO verificada", fontWeight = FontWeight.Bold)
            Text("El software no convierte el power bank en un hub. Para usar carga + OTG, se necesita una ruta de datos real y confirmación física de compatibilidad del teléfono.")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Modo sandbox (sin acceso a archivos)", modifier = Modifier.weight(1f))
            Switch(checked = demo, onCheckedChange = {
                demo = it
                writeEnabled = false
                message = if (it) "SIMULACIÓN: no toca archivos." else "MODO REAL: consulta inicial de solo lectura."
            })
        }

        if (demo) {
            InfoCard {
                Text("SIMULADOR LÓGICO", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                val descriptions = mapOf(
                    Connection.MEMORY_DIRECT to "1. Memoria conectada al teléfono",
                    Connection.MEMORY_VIA_POWERBANK_POWER_ONLY to "2. Memoria en power bank (supuesto: solo energía)",
                    Connection.HUB_COMPATIBLE to "3. Hub PD + teléfono compatible (hipótesis)",
                    Connection.HUB_PHONE_INCOMPATIBLE to "4. Hub PD + teléfono incompatible (hipótesis)",
                    Connection.HUB_UNVERIFIED to "5. Hub PD + compatibilidad por comprobar",
                    Connection.DISCONNECTED_DRIVE to "6. Memoria desconectada",
                    Connection.READ_ONLY_DRIVE to "7. Memoria montada en solo lectura"
                )
                Connection.entries.forEach { type ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { scenario = type }) {
                        RadioButton(selected = scenario == type, onClick = { scenario = type })
                        Text(descriptions[type].orEmpty(), style = MaterialTheme.typography.bodySmall)
                    }
                }
                Text("Carga simulada: ${if (simulation.charging) "SÍ" else "NO"}")
                Text("Datos simulados: ${if (simulation.dataPath) "RUTA PRESENTE" else "SIN RUTA"}")
                Text("Ambas funciones: ${when(simulation.simultaneous) { Validation.YES -> "SÍ EN EL MODELO"; Validation.NO -> "NO"; Validation.UNKNOWN -> "PENDIENTE" }}",
                    fontWeight = FontWeight.Bold)
                Text(simulation.note, style = MaterialTheme.typography.bodySmall)
            }
        } else {
            InfoCard {
                Text("DIAGNÓSTICO REAL DE ANDROID", fontWeight = FontWeight.Bold)
                Text("Batería del teléfono: ${if (power.percent >= 0) "${power.percent}%" else "sin lectura"}")
                Text("Alimentación externa conectada: ${if (power.plugged) "SÍ" else "NO"}")
                Text("Batería cargando según Android: ${if (power.charging) "SÍ" else "NO / no confirmado"}")
                Text("Origen indicado por Android: ${power.source}")
                Text("Carpeta SAF autorizada: ${if (current != null) "SÍ" else "NO"}")
                Text("Tipo de almacenamiento seleccionado: $externalVolumeHint")
                Text("Android no informa necesariamente qué power bank se está usando ni verifica negociación USB-PD.", style = MaterialTheme.typography.bodySmall)
            }
            InfoCard {
                Text("DISPOSITIVOS USB OBSERVABLES", fontWeight = FontWeight.Bold)
                Text("Función host declarada: ${if (usbSnapshot.hostFeatureReported) "SÍ" else "NO / indeterminada"}")
                Text("Dispositivos enumerados por la API: ${usbSnapshot.devices.size}")
                usbSnapshot.devices.forEach { item ->
                    Text("VID=${"%04X".format(item.vendorId)} PID=${"%04X".format(item.productId)} " +
                        "interfaces=${item.interfaceCount} clase=${item.deviceClass} " +
                        "almacenamiento=${if (item.massStorageDescriptor) "posible" else "no indicado"}",
                        style = MaterialTheme.typography.bodySmall)
                }
                usbSnapshot.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Text("Una memoria montada por Android puede NO aparecer aquí. Una lista vacía NO demuestra ausencia de USB.",
                    style = MaterialTheme.typography.bodySmall)
                OutlinedButton(onClick = {
                    usbSnapshot = UsbProbe.read(context)
                    message = "Consulta USB actualizada: ${usbSnapshot.devices.size} dispositivos visibles en API."
                }) { Text("🔎 Actualizar diagnóstico USB") }
            }
            InfoCard {
                Text("PROTECCIÓN DE ARCHIVOS", fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Habilitar cambios de archivos", modifier = Modifier.weight(1f))
                    Switch(checked = writeEnabled, onCheckedChange = {
                        writeEnabled = it
                        message = if (it) "CAMBIOS HABILITADOS: usar solo archivos de prueba."
                        else "SOLO LECTURA: operaciones de modificación bloqueadas."
                    }, enabled = current != null && !busy)
                }
                Text("Inicialmente solo lectura. Los respaldos creados en la misma memoria NO protegen frente a una falla de esa memoria.",
                    style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = { folderLauncher.launch(null) }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                Text("📂 Autorizar carpeta de la memoria USB")
            }
            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("*/*")) },
                enabled = current != null && writeEnabled && !busy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📤 Copiar archivo del teléfono a la carpeta autorizada (SHA-256)")
            }
            Text(
                "Seleccione primero una carpeta del pendrive. El selector de Android permite elegir el archivo de origen; el original no se borra.",
                style = MaterialTheme.typography.bodySmall
            )
            InfoCard {
                Text("EXPLORADOR DE DOCUMENTOS", fontWeight = FontWeight.Bold)
                Text("Ruta: ${path.joinToString(" / ") { it.name ?: "USB" }}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        if (path.size > 1) { path.removeAt(path.lastIndex); selected = null; editorReady = false }
                    }, enabled = path.size > 1) { Text("Atrás") }
                    OutlinedButton(onClick = { refreshVersion++ }, enabled = current != null) { Text("Actualizar") }
                }
                if (entries.isEmpty()) Text("Sin archivos visibles o falta autorizar la carpeta USB.")
                entries.forEach { file ->
                    Text(
                        "${if (file.isDirectory) "📁" else "📄"} ${file.name ?: "(sin nombre)"}",
                        modifier = Modifier.fillMaxWidth().clickable {
                            if (file.isDirectory) {
                                path.add(file); selected = null; editorReady = false
                            } else {
                                selected = file; editorReady = false; editor = ""
                                message = "Seleccionado ${file.name}. Edición de texto guarda una copia nueva."
                                if (SafeDocumentOps.isEditableText(file)) {
                                    busy = true
                                    scope.launch {
                                        val output = withContext(Dispatchers.IO) {
                                            runCatching { SafeDocumentOps.readText(context.contentResolver, file) }
                                        }
                                        output.onSuccess { editor = it; editorReady = true }
                                            .onFailure { message = "Vista previa: ${it.message}" }
                                        busy = false
                                    }
                                }
                            }
                        }.padding(vertical = 6.dp),
                        color = if (selected?.uri == file.uri) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                if (entries.size >= 80) Text("Mostrando 80 elementos; abra subcarpetas para reducir el listado.", style = MaterialTheme.typography.labelSmall)
            }
            if (selected != null && current != null) {
                InfoCard {
                    Text("Archivo: ${selected?.name}", fontWeight = FontWeight.Bold)
                    Text("Tamaño informado: ${selected?.length()} bytes")
                    Text("Escritura informada: ${if (selected?.canWrite() == true) "posible" else "no disponible"}")
                    if (editorReady) {
                        OutlinedTextField(value = editor, onValueChange = { editor = it },
                            label = { Text("Editor UTF-8 (máximo 256 KiB)") },
                            modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 10)
                        Button(onClick = {
                            val origin = selected ?: return@Button
                            val dir = current
                            val content = editor
                            runSafAction("Guardar versión") {
                                val f = SafeDocumentOps.saveEditedVersion(context.contentResolver, dir, origin, content)
                                "Copia verificada SHA-256: ${f.name}; original intacto"
                            }
                        }, enabled = !busy && writeEnabled) { Text("Guardar copia editada (no sobrescribir)") }
                    } else {
                        Text("Vista previa disponible para archivos de texto de hasta 256 KiB.", style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedButton(onClick = {
                        val origin = selected ?: return@OutlinedButton
                        val dir = current
                        runSafAction("Duplicar") {
                            val f = SafeDocumentOps.copyWithVerification(context.contentResolver, dir, origin, "GENAI_COPIA")
                            "Nueva copia SHA-256 verificada: ${f.name}"
                        }
                    }, enabled = !busy && writeEnabled) { Text("Duplicar y verificar SHA-256") }
                    OutlinedButton(onClick = {
                        renameText = selected?.name.orEmpty(); renameDialog = true
                    }, enabled = !busy && writeEnabled) { Text("Renombrar archivo") }
                    OutlinedButton(onClick = { deleteDialog = true }, enabled = !busy && writeEnabled,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Text("Eliminar con respaldo verificado")
                    }
                }
            }
        }
        InfoCard {
            Text("REGISTRO LOCAL", fontWeight = FontWeight.Bold)
            Text(if (busy) "Operación en curso..." else message)
            Text("No se solicita root, ADB, Termux ni permiso global sobre el almacenamiento.", style = MaterialTheme.typography.labelSmall)
        }
        Text("GENAI INDUSTRIES • Prototipo 0.3.0 • Compatibilidad física pendiente",
            style = MaterialTheme.typography.labelSmall)
    }
    if (deleteDialog) {
        AlertDialog(onDismissRequest = { deleteDialog = false },
            title = { Text("Confirmación de eliminación") },
            text = { Text("Se creará una copia verificada SHA-256 EN LA MISMA CARPETA. Esto NO protege de fallas del USB. Después se intentará borrar «${selected?.name}». Asegúrate de tener un respaldo externo independiente antes de continuar. ¿Confirmas?") },
            confirmButton = {
                TextButton(onClick = {
                    deleteDialog = false
                    val origin = selected ?: return@TextButton
                    val dir = current ?: return@TextButton
                    runSafAction("Eliminación con respaldo") {
                        val backup = SafeDocumentOps.deleteAfterBackup(context.contentResolver, dir, origin)
                        "Original eliminado. Respaldo SHA-256 conservado: ${backup.name}"
                    }
                    selected = null; editorReady = false
                }) { Text("Respaldar y eliminar") }
            }, dismissButton = { TextButton(onClick = { deleteDialog = false }) { Text("Cancelar") } })
    }
    if (renameDialog) {
        AlertDialog(onDismissRequest = { renameDialog = false },
            title = { Text("Renombrar archivo") },
            text = { OutlinedTextField(value = renameText, onValueChange = { renameText = it }, singleLine = true) },
            confirmButton = {
                TextButton(onClick = {
                    renameDialog = false
                    val origin = selected ?: return@TextButton
                    val newName = renameText
                    runSafAction("Renombrar") {
                        SafeDocumentOps.renameFile(origin, newName)
                        "Nombre actualizado: $newName"
                    }
                    selected = null; editorReady = false
                }) { Text("Renombrar") }
            }, dismissButton = { TextButton(onClick = { renameDialog = false }) { Text("Cancelar") } })
    }
}

@Composable
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp), content = content)
    }
}

package bo.genai.usbpowerbridge

import android.content.Context
import android.os.SystemClock
import android.os.StatFs
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import java.util.Locale

/** Capa visual V0.6.0. No contiene valores simulados ni altera las operaciones SAF. */
internal data class DeviceSpace(val total: Long?, val available: Long?) {
    companion object {
        fun read(context: Context): DeviceSpace = runCatching {
            val stats = StatFs(context.filesDir.absolutePath)
            DeviceSpace(stats.totalBytes.takeIf { it > 0L }, stats.availableBytes.takeIf { it >= 0L })
        }.getOrDefault(DeviceSpace(null, null))
    }
}

internal data class BridgeUiData(
    val power: PowerInfo,
    val usb: UsbSnapshot,
    val evidence: StorageEvidence,
    val selectedRoot: DocumentFile?,
    val currentFolder: DocumentFile?,
    val session: Boolean,
    val removable: Boolean,
    val simultaneous: Boolean,
    val writeAllowed: Boolean,
    val busy: Boolean,
    val transfer: TransferDisplay?,
    val items: List<DocumentFile>,
    val selected: DocumentFile?,
    val editor: String,
    val editorReady: Boolean,
    val hasParent: Boolean,
    val log: String,
    val phoneSpace: DeviceSpace
)

internal data class BridgeActions(
    val connect: () -> Unit,
    val disconnect: () -> Unit,
    val refresh: () -> Unit,
    val writeChange: (Boolean) -> Unit,
    val selectSourceFile: () -> Unit,
    val upFolder: () -> Unit,
    val openItem: (DocumentFile) -> Unit,
    val editText: (String) -> Unit,
    val saveEdited: () -> Unit,
    val duplicate: () -> Unit,
    val rename: () -> Unit,
    val delete: () -> Unit,
    val checksum: () -> Unit
)

private val SpaceBg = Color(0xFF031020)
private val Glass = Color(0xFF0C2440)
private val Cyan = Color(0xFF36E0FF)
private val Blue = Color(0xFF177CFC)
private val Amber = Color(0xFFFFA62B)
private val Lime = Color(0xFF45F3B3)
private val TextHi = Color(0xFFF3F8FF)
private val TextSub = Color(0xFFC0DCF1)
private val Danger = Color(0xFFFF6573)

private enum class Page(val icon: String, val caption: String) {
    HOME("⌂", "Tablero"), TRANSFER("⇄", "Transferir"),
    STORAGE("▣", "Memoria"), TOOLS("⚒", "Herramientas"), SETTINGS("⚙", "Ajustes")
}

@Composable
internal fun BridgeInterface(data: BridgeUiData, actions: BridgeActions) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("otg_visual_preferences", Context.MODE_PRIVATE) }
    var currentPage by remember { mutableStateOf(Page.HOME) }
    var animations by remember { mutableStateOf(prefs.getBoolean("animations", true)) }
    var compact by remember { mutableStateOf(prefs.getBoolean("compact", false)) }
    var technical by remember { mutableStateOf(prefs.getBoolean("technical", false)) }

    Box(modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(Color(0xFF010B19), Color(0xFF09294B), SpaceBg))
    )) {
        Column(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(if (compact) 9.dp else 13.dp)
            ) {
                BrandHero()
                when (currentPage) {
                    Page.HOME -> HomePage(data, actions, animations) { currentPage = it }
                    Page.TRANSFER -> TransferPage(data, actions, animations)
                    Page.STORAGE -> StoragePage(data, actions, technical)
                    Page.TOOLS -> ToolsPage(data, actions)
                    Page.SETTINGS -> SettingsPage(
                        animations, compact, technical,
                        onAnimations = { animations = it; prefs.edit().putBoolean("animations", it).apply() },
                        onCompact = { compact = it; prefs.edit().putBoolean("compact", it).apply() },
                        onTechnical = { technical = it; prefs.edit().putBoolean("technical", it).apply() }
                    )
                }
                Text(
                    "GENAI INDUSTRIES · OTG POWER BRIDGE 0.6.0 · Datos reales de Android",
                    modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp),
                    color = TextSub, textAlign = TextAlign.Center, fontSize = 10.sp
                )
            }
            BottomNavigation(currentPage) { currentPage = it }
        }
    }
}

@Composable
private fun BrandHero() {
    Box(
        modifier = Modifier.fillMaxWidth().height(183.dp).clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF09243E), SpaceBg)))
            .border(1.dp, Brush.horizontalGradient(listOf(Blue, Amber, Cyan)), RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.brand_banner),
            contentDescription = "Logotipo oficial OTG POWER BRIDGE — GENAI INDUSTRIES",
            modifier = Modifier.fillMaxWidth().height(181.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun BottomNavigation(selected: Page, onSelect: (Page) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFF123453), Color(0xFF051326))))
            .border(1.dp, Cyan.copy(alpha = .45f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(horizontal = 5.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Page.values().forEach { page ->
            val active = selected == page
            Column(
                modifier = Modifier.weight(1f).height(58.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (active) Brush.verticalGradient(listOf(Color(0xFF21516F), Color(0xFF3E2814)))
                        else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent)))
                    .border(if (active) 1.dp else .4.dp,
                        if (active) Amber else Cyan.copy(alpha = .25f), RoundedCornerShape(12.dp))
                    .clickable { onSelect(page) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(page.icon, color = if (active) Amber else Cyan, fontSize = 22.sp)
                Text(page.caption, color = TextHi, fontSize = 9.sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun NeonPanel(
    title: String,
    accent: Color = Cyan,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = Modifier.fillMaxWidth()
            .shadow(7.dp, shape, ambientColor = accent.copy(alpha = .15f), spotColor = accent.copy(alpha = .22f))
            .background(Brush.verticalGradient(listOf(Color(0xFF14344F), Glass, Color(0xFF081B32))), shape)
            .border(1.dp, Brush.horizontalGradient(listOf(accent.copy(alpha = .8f), Blue.copy(alpha = .3f), accent.copy(alpha = .6f))), shape)
            .padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(title.uppercase(Locale.ROOT), color = accent, fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold, letterSpacing = 1.1.sp)
        HorizontalDivider(color = accent.copy(alpha = .2f))
        content()
    }
}

@Composable
private fun NeonButton(
    label: String, enabled: Boolean, primary: Boolean = true,
    modifier: Modifier = Modifier, onClick: () -> Unit
) {
    val tone = if (primary) Cyan else Amber
    Box(
        modifier = modifier.height(48.dp).clip(RoundedCornerShape(13.dp))
            .background(
                Brush.verticalGradient(if (enabled) listOf(tone.copy(alpha = .35f), Color(0xFF0D2841))
                else listOf(Color(0xFF152E42), Color(0xFF102137)))
            )
            .border(1.3.dp, if (enabled) tone else Color(0xFF526679), RoundedCornerShape(13.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp,
            color = if (enabled) TextHi else Color(0xFF8DA4BA), maxLines = 1)
    }
}

@Composable
private fun LivePill(label: String, good: Boolean?, detail: String = "") {
    val color = when (good) { true -> Lime; false -> Danger; null -> Amber }
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = .07f))
            .border(1.dp, color.copy(alpha = .55f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 11.dp)
    ) {
        Text("●  $label", color = color, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        if (detail.isNotBlank()) Text(detail, color = TextSub, fontSize = 11.sp)
    }
}

@Composable
private fun Metric(label: String, value: String, color: Color = TextHi) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(label, modifier = Modifier.weight(1f), color = TextSub, fontSize = 11.sp)
        Text(value, modifier = Modifier.weight(1.1f), color = color,
            textAlign = TextAlign.End, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CircleGauge(percent: Int, title: String, accent: Color = Lime, modifier: Modifier = Modifier) {
    Box(modifier.size(110.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 15f, cap = StrokeCap.Round)
            drawArc(Cyan.copy(alpha = .17f), 135f, 270f, false, style = stroke)
            if (percent >= 0) drawArc(accent, 135f, 270f * percent.coerceIn(0,100) / 100f,
                false, style = stroke)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = TextSub, fontSize = 10.sp, textAlign = TextAlign.Center)
            Text(if (percent < 0) "—" else "$percent%", fontSize = 24.sp,
                color = accent, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun LiveDiagram(data: BridgeUiData, animated: Boolean) {
    val transition = rememberInfiniteTransition(label = "flujo_usb")
    val motion by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1850), RepeatMode.Restart), label = "movimiento_cables"
    )
    val showData = data.removable
    val showPower = data.power.externalPower
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                val y = size.height * .53f
                val left = size.width * .16f
                val center = size.width * .50f
                val right = size.width * .84f
                val path1 = Path().apply {
                    moveTo(left, y)
                    cubicTo(size.width * .29f, y - 25f, size.width * .38f, y + 22f, center, y)
                }
                val path2 = Path().apply {
                    moveTo(center, y)
                    cubicTo(size.width * .61f, y - 23f, size.width * .71f, y + 22f, right, y)
                }
                drawPath(path1, if (showData) Cyan else Color(0xFF37546B),
                    style = Stroke(width = 7f, cap = StrokeCap.Round))
                drawPath(path2, if (showPower) Amber else Color(0xFF37546B),
                    style = Stroke(width = 7f, cap = StrokeCap.Round))
                val phase = if (animated) motion else .45f
                if (showData) drawCircle(Color.White, 8f,
                    center = Offset(left + (center-left)*phase, y))
                if (showPower) drawCircle(Amber, 8f,
                    center = Offset(center + (right-center)*(1f-phase), y))
                drawCircle(Cyan, 15f, Offset(center, y))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("TELÉFONO", color = TextSub, fontSize = 9.sp)
            Text("HUB¹", color = Cyan, fontSize = 9.sp)
            Text("FUENTE EXT.", color = TextSub, fontSize = 9.sp)
        }
        Text("¹ Representación gráfica. Android no identifica universalmente la marca del hub ni el power bank.",
            color = TextSub, fontSize = 10.sp)
    }
}

@Composable
private fun HomePage(data: BridgeUiData, actions: BridgeActions, animations: Boolean, go: (Page) -> Unit) {
    NeonPanel("Centro de control · estado en vivo") {
        LivePill(
            if (data.session) "CARPETA AUTORIZADA" else "SIN SESIÓN DE ARCHIVOS",
            data.session,
            if (data.session) "Acceso concedido: ${data.selectedRoot?.name ?: "Carpeta sin etiqueta"}"
            else "Pulsa Conectar para seleccionar una carpeta real de Android."
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NeonButton("⚡ CONECTAR", !data.busy, modifier = Modifier.weight(1f), onClick = actions.connect)
            NeonButton("⨯ DESCONECTAR", data.session && !data.busy, primary = false,
                modifier = Modifier.weight(1f), onClick = actions.disconnect)
        }
        LiveDiagram(data, animations)
    }
    NeonPanel("Batería y alimentación", Amber) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CircleGauge(data.power.percent, "TELÉFONO", if (data.power.percent in 0..15) Amber else Lime)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Metric("Estado", when {
                    data.power.charging -> "Cargando"
                    data.power.externalPower -> "Fuente detectada"
                    else -> "Sin alimentación externa"
                }, if (data.power.externalPower) Lime else TextHi)
                Metric("Origen", data.power.source)
                Metric("Power bank (%)", "No disponible", Amber)
                Text("No se infieren porcentajes ni vatios del power bank.", color = TextSub, fontSize = 10.sp)
            }
        }
    }
    NeonPanel("USB y memoria") {
        Metric("USB host declarado", if (data.usb.hostFeatureReported) "Sí" else "No")
        Metric("Equipos enumerados", "${data.usb.devices.size}")
        Metric("Destino autorizado", data.selectedRoot?.name ?: "Sin seleccionar")
        Metric("Volumen extraíble", when(data.evidence.removable) {
            true -> "Sí (USB o SD)"; false -> "No"; null -> "No confirmado"
        }, if (data.removable) Lime else Amber)
        Metric("Capacidad externa", TelemetryFormats.bytes(data.evidence.totalBytes))
        Metric("Carga + extraíble", if (data.simultaneous) "Observadas" else "No confirmado",
            if (data.simultaneous) Lime else Amber)
        NeonButton("ABRIR ALMACENAMIENTO  ›", true, onClick = { go(Page.STORAGE) })
    }
    NeonPanel("Transferencia actual", Cyan) {
        TransferSummary(data.transfer)
        NeonButton("GESTIONAR TRANSFERENCIAS  ›", true, onClick = { go(Page.TRANSFER) })
    }
    NeonPanel("Accesos rápidos", Amber) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NeonButton("HERRAMIENTAS", true, modifier = Modifier.weight(1f), onClick = { go(Page.TOOLS) })
            NeonButton("ACTUALIZAR", !data.busy, primary = false,
                modifier = Modifier.weight(1f), onClick = actions.refresh)
        }
    }
}

@Composable
private fun TransferSummary(tx: TransferDisplay?) {
    if (tx == null) {
        Text("No hay transferencias activas en esta sesión.", color = TextSub)
        return
    }
    Metric("Archivo", tx.name, Cyan)
    Metric("Copiado", TelemetryFormats.bytes(tx.copiedBytes))
    Metric("Tamaño", TelemetryFormats.bytes(tx.totalBytes))
    val progress = TelemetryFormats.percent(tx.copiedBytes, tx.totalBytes)
    Text("${if (progress == null) "Progreso indeterminado" else "$progress%"} · ${tx.stage}",
        color = if (tx.verified) Lime else Amber, fontWeight = FontWeight.Bold)
    if (progress == null) LinearProgressIndicator(Modifier.fillMaxWidth())
    else LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth().height(11.dp),
        color = if (tx.verified) Lime else Cyan, trackColor = SpaceBg)
    val elapsed = (SystemClock.elapsedRealtime() - tx.startElapsedMs).coerceAtLeast(0L)
    if (tx.startElapsedMs > 0L && tx.copiedBytes > 0L && elapsed > 1000 && !tx.verified) {
        val bytesPerSec = tx.copiedBytes * 1000.0 / elapsed
        Metric("Velocidad media", "${TelemetryFormats.bytes(bytesPerSec.toLong())}/s")
        val left = tx.totalBytes?.minus(tx.copiedBytes)?.coerceAtLeast(0L)
        if (left != null && bytesPerSec > 0.0) {
            val seconds = (left / bytesPerSec).toLong().coerceAtLeast(0L)
            Metric("Tiempo estimado", "~${seconds}s")
        }
    }
    if (tx.verified) Text("✓ SHA-256 verificado. Origen conservado.", color = Lime, fontSize = 12.sp)
}

@Composable
private fun TransferPage(data: BridgeUiData, actions: BridgeActions, animations: Boolean) {
    NeonPanel("⇄ Gestor de transferencias", Amber) {
        LivePill(if (data.session) "SESIÓN DE DESTINO ABIERTA" else "DESTINO SIN AUTORIZACIÓN", data.session,
            data.currentFolder?.name ?: "Selecciona una carpeta real del teléfono o USB")
        LiveDiagram(data, animations)
        Metric("Origen", "Archivo seleccionado por Android")
        Metric("Destino", data.currentFolder?.name ?: "No seleccionado")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NeonButton("SELECCIONAR CARPETA", !data.busy,
                modifier = Modifier.weight(1f), onClick = actions.connect)
            NeonButton("REFRESCAR", !data.busy, primary = false,
                modifier = Modifier.weight(1f), onClick = actions.refresh)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Habilitar escritura", Modifier.weight(1f), color = TextHi, fontSize = 13.sp)
            Switch(checked = data.writeAllowed, onCheckedChange = actions.writeChange,
                enabled = data.session && !data.busy)
        }
        NeonButton("+ SELECCIONAR Y COPIAR ARCHIVO", data.session && data.writeAllowed && !data.busy,
            onClick = actions.selectSourceFile)
        Text("La app hace una copia nueva y comprueba SHA-256. Por ahora se procesa un archivo a la vez; no existe pausa/reanudación.",
            fontSize = 11.sp, color = TextSub)
    }
    NeonPanel("Transferencia real en curso") { TransferSummary(data.transfer) }
    NeonPanel("Registro de operaciones", Amber) {
        Text(data.log, color = TextHi, fontSize = 12.sp)
        Text("Historial de la sesión actual; no se inventan transferencias anteriores.",
            color = TextSub, fontSize = 11.sp)
    }
}

@Composable
private fun CapacityCard(title: String, total: Long?, free: Long?, subtitle: String) {
    val used = if (total != null && free != null) (total-free).coerceAtLeast(0L) else null
    val fraction = if (total != null && total > 0 && used != null) used.toFloat()/total.toFloat() else null
    NeonPanel(title, Cyan) {
        Text(subtitle, color = TextSub, fontSize = 11.sp)
        Metric("Total", TelemetryFormats.bytes(total))
        Metric("Usado", TelemetryFormats.bytes(used))
        Metric("Disponible", TelemetryFormats.bytes(free), Lime)
        if (fraction != null) LinearProgressIndicator(progress = { fraction.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(10.dp), color = Cyan, trackColor = SpaceBg)
    }
}

@Composable
private fun StoragePage(data: BridgeUiData, actions: BridgeActions, technical: Boolean) {
    NeonPanel("▣ Almacenamiento conectado", Amber) {
        Metric("Carpeta", data.selectedRoot?.name ?: "No seleccionada")
        Metric("Tipo", data.evidence.description)
        Metric("Etiqueta", data.evidence.volumeLabel ?: "No informada")
        Metric("USB detectados por API", data.usb.devices.size.toString())
        if (technical) Metric("Identificador", data.evidence.volumeId ?: "No disponible")
        NeonButton("SELECCIONAR CARPETA REAL", !data.busy, onClick = actions.connect)
    }
    CapacityCard("Capacidad del teléfono", data.phoneSpace.total, data.phoneSpace.available,
        "Partición de datos consultada con StatFs. No equivale a capacidad física total de RAM/ROM.")
    CapacityCard("Volumen autorizado", data.evidence.totalBytes, data.evidence.availableBytes,
        "Puede ser USB, microSD o memoria interna. Android limita algunas mediciones SAF.")
    NeonPanel("Identidad del dispositivo USB") {
        if (data.usb.devices.isEmpty()) Text("Sin descriptores USB accesibles; una unidad montada puede no aparecer aquí.",
            color = TextSub, fontSize = 12.sp)
        data.usb.devices.forEach { dev ->
            Metric("Fabricante", dev.manufacturer ?: "No informado")
            Metric("Producto", dev.product ?: "No informado")
            if (technical) Metric("VID / PID", "%04X / %04X".format(dev.vendorId, dev.productId))
            Metric("Clase", if (dev.massStorageDescriptor) "Almacenamiento masivo" else "USB (otra clase)")
            HorizontalDivider(color = Cyan.copy(alpha = .18f))
        }
        Text("No se vincula automáticamente un descriptor USB a una carpeta SAF.", color = TextSub, fontSize = 10.sp)
    }
    NeonPanel("Explorador de archivos", Amber) {
        Metric("Ruta actual", data.currentFolder?.name ?: "Ninguna")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NeonButton("‹ ATRÁS", data.hasParent && !data.busy,
                modifier = Modifier.weight(1f), onClick = actions.upFolder)
            NeonButton("ACTUALIZAR", data.session && !data.busy, primary = false,
                modifier = Modifier.weight(1f), onClick = actions.refresh)
        }
        if (!data.session) Text("Selecciona una carpeta desde Android para explorar su contenido.", color = TextSub)
        else if (data.items.isEmpty()) Text("Sin archivos visibles en esta carpeta.", color = TextSub)
        data.items.forEach { file ->
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(if (data.selected?.uri == file.uri) Cyan.copy(alpha = .18f)
                        else Color.White.copy(alpha = .035f))
                    .clickable(enabled = !data.busy) { actions.openItem(file) }
                    .padding(11.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (file.isDirectory) "▣" else "▤", color = Amber, fontSize = 18.sp)
                Spacer(Modifier.width(10.dp))
                Text(file.name ?: "Sin nombre", modifier = Modifier.weight(1f), color = TextHi,
                    fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(if (file.isDirectory) "›" else TelemetryFormats.bytes(file.length()),
                    fontSize = 10.sp, color = TextSub)
            }
        }
        if (data.items.size >= 150) Text("Se muestran los primeros 150 elementos.", color = TextSub, fontSize = 10.sp)
    }
    if (data.selected != null && data.session) {
        NeonPanel("Archivo seleccionado", Amber) {
            Metric("Nombre", data.selected.name ?: "Sin nombre")
            Metric("Tamaño", TelemetryFormats.bytes(data.selected.length()))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Autorizar cambios", Modifier.weight(1f), color = TextHi)
                Switch(data.writeAllowed, actions.writeChange, enabled = !data.busy)
            }
            if (data.editorReady) {
                Text("Editor de texto UTF-8 (crea copia)", color = TextSub, fontSize = 11.sp)
                BasicTextField(value = data.editor, onValueChange = actions.editText,
                    modifier = Modifier.fillMaxWidth().height(130.dp)
                        .background(Color(0xFF011626), RoundedCornerShape(9.dp))
                        .padding(10.dp), textStyle = androidx.compose.ui.text.TextStyle(color = TextHi, fontSize = 13.sp))
                NeonButton("GUARDAR COMO NUEVO ARCHIVO", !data.busy && data.writeAllowed,
                    onClick = actions.saveEdited)
            }
            NeonButton("DUPLICAR + SHA-256", !data.busy && data.writeAllowed,
                onClick = actions.duplicate)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NeonButton("RENOMBRAR", !data.busy && data.writeAllowed,
                    modifier = Modifier.weight(1f), onClick = actions.rename)
                NeonButton("RESPALDAR Y ELIMINAR", !data.busy && data.writeAllowed, primary = false,
                    modifier = Modifier.weight(1f), onClick = actions.delete)
            }
        }
    }
}

@Composable
private fun ToolsPage(data: BridgeUiData, actions: BridgeActions) {
    NeonPanel("⚒ Herramientas y diagnóstico", Amber) {
        LivePill("USB host: ${if (data.usb.hostFeatureReported) "DECLARADO" else "NO DECLARADO"}",
            data.usb.hostFeatureReported, "Dispositivos enumerados: ${data.usb.devices.size}")
        Metric("Fuente de energía", data.power.source)
        Metric("Batería", if (data.power.percent >= 0) "${data.power.percent}%" else "No disponible")
        Metric("Carga externa", if (data.power.externalPower) "Detectada" else "No detectada")
        Metric("Carpeta accesible", if (data.session) "Sí" else "No")
        Metric("Extraíble identificado", when(data.evidence.removable) {
            true -> "Sí (USB/SD)"; false -> "No"; null -> "Desconocido" })
        Metric("Carga y extraíble", if (data.simultaneous) "Ambos observados" else "No confirmado")
        NeonButton("ACTUALIZAR DIAGNÓSTICO REAL", !data.busy, onClick = actions.refresh)
        data.usb.error?.let { Text(it, color = Amber, fontSize = 11.sp) }
    }
    NeonPanel("Verificación de archivos · SHA-256") {
        Metric("Archivo seleccionado", data.selected?.name ?: "Ninguno")
        NeonButton("CALCULAR SHA-256", data.selected?.isFile == true && !data.busy,
            onClick = actions.checksum)
        Text("El hash se obtiene leyendo el archivo real, sin escribir ni alterar su contenido.",
            color = TextSub, fontSize = 11.sp)
    }
    NeonPanel("Expulsión y seguridad", Amber) {
        Text("La app no controla físicamente la expulsión del dispositivo. Espera a que termine la copia, cierra el acceso de la app y utiliza la opción de expulsar del sistema Android si existe.",
            color = TextHi, fontSize = 12.sp)
        NeonButton("CERRAR SESIÓN DE ARCHIVOS", data.session && !data.busy,
            primary = false, onClick = actions.disconnect)
    }
    NeonPanel("Registro local de actividad") {
        Text(data.log, color = TextHi, fontSize = 12.sp)
        Text("No se realizan pruebas eléctricas ni mediciones de potencia USB-PD.",
            color = TextSub, fontSize = 11.sp)
    }
}

@Composable
private fun SettingsPage(
    animations: Boolean, compact: Boolean, technical: Boolean,
    onAnimations: (Boolean) -> Unit,
    onCompact: (Boolean) -> Unit,
    onTechnical: (Boolean) -> Unit
) {
    NeonPanel("⚙ Ajustes de la interfaz", Amber) {
        SettingSwitch("Efectos de flujo animados", "Se animan solamente rutas con actividad real detectada.",
            animations, onAnimations)
        SettingSwitch("Vista compacta", "Reduce espacios entre paneles.", compact, onCompact)
        SettingSwitch("Ver identificadores técnicos", "Muestra VID/PID e identificadores del volumen.",
            technical, onTechnical)
        Text("Paleta oficial: azul cian + naranja metalizado, logotipo 3D aprobado.",
            color = TextSub, fontSize = 11.sp)
    }
    NeonPanel("Permisos y privacidad") {
        Text("Android solicita acceso a cada carpeta mediante el selector del sistema. No hay root, ADB ni permiso global de almacenamiento.",
            color = TextHi, fontSize = 12.sp)
        Text("Los ajustes visuales se guardan únicamente en las preferencias privadas de esta aplicación.",
            color = TextSub, fontSize = 11.sp)
    }
    NeonPanel("Seguridad de los archivos", Amber) {
        Metric("Copia de archivo", "SHA-256 obligatorio")
        Metric("Archivo origen", "Se conserva")
        Metric("Escritura inicial", "Desactivada")
        Metric("Borrado", "Solo con confirmación y respaldo")
        Text("No hay restauración automática tras una falla física de la unidad. Haz siempre copias independientes.",
            color = TextSub, fontSize = 11.sp)
    }
    NeonPanel("Acerca del proyecto") {
        Metric("Aplicación", "OTG POWER BRIDGE")
        Metric("Versión candidata", "0.6.0 visual real")
        Metric("Plataforma", "Android 8+")
        Metric("Creador", "GENAI INDUSTRIES")
        Text("Dirección: Luis Carlos Moreno Jiménez · Santa Cruz de la Sierra, Bolivia.",
            fontSize = 11.sp, color = TextSub)
        Text("Carga + OTG simultáneos dependen del teléfono y del hub. No se muestran valores de potencia o del power bank que Android no informe.",
            fontSize = 11.sp, color = Amber)
    }
}

@Composable
private fun SettingSwitch(label: String, detail: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, color = TextHi, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Text(detail, color = TextSub, fontSize = 10.sp)
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

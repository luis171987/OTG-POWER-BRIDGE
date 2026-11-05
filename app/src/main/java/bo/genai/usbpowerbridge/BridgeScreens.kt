package bo.genai.usbpowerbridge

import android.content.Context
import android.os.SystemClock
import android.os.StatFs
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Capa visual V0.6.1. Instrumentos vinculados a Android real; SAF permanece separado. */
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

private val SpaceBg = Color(0xFF020914)
private val Glass = Color(0xFF091B30)
private val Cyan = Color(0xFF45E5FF)
private val Blue = Color(0xFF1579FF)
private val Amber = Color(0xFFFF9B22)
private val Lime = Color(0xFF43FBB8)
private val TextHi = Color(0xFFF4FAFF)
private val TextSub = Color(0xFFB8D8EE)
private val Danger = Color(0xFFFF5D78)
private val Titanium = Color(0xFFAFC5DE)
private val Purple = Color(0xFFAE80FF)

private enum class Page(val icon: String, val caption: String) {
    HOME("⌂", "Tablero"), TRANSFER("⇄", "Transferir"),
    STORAGE("▣", "Memoria"), TOOLS("⚒", "Herramientas"), SETTINGS("⚙", "Ajustes")
}

@Composable
internal fun BridgeInterface(data: BridgeUiData, actions: BridgeActions) {
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences("otg_visual_preferences", Context.MODE_PRIVATE)
    }
    var currentPage by remember { mutableStateOf(Page.HOME) }
    var animations by remember { mutableStateOf(prefs.getBoolean("animations", true)) }
    var compact by remember { mutableStateOf(prefs.getBoolean("compact", false)) }
    var technical by remember { mutableStateOf(prefs.getBoolean("technical", false)) }
    fun go(page: Page) { currentPage = page }

    Box(Modifier.fillMaxSize().background(SpaceBg)) {
        Image(
            painter = painterResource(R.drawable.cyber_cockpit_backdrop),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().alpha(.80f),
            contentScale = ContentScale.FillBounds
        )
        Box(Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0x7800081A), Color(0x69041629), SpaceBg.copy(alpha = .91f)))
        ))
        Column(Modifier.fillMaxSize()) {
            BrandHero(data)
            Box(Modifier.weight(1f)) {
                val content: @Composable (Page) -> Unit = { page ->
                    key(page) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp, vertical = if (compact) 6.dp else 10.dp),
                            verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp)
                        ) {
                            when (page) {
                                Page.HOME -> HomePage(data, actions, animations) { go(it) }
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
                                "GENAI INDUSTRIES  •  CYBER COCKPIT V0.6.1  •  TELEMETRÍA REAL",
                                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                color = TextSub, textAlign = TextAlign.Center, fontSize = 9.sp,
                                letterSpacing = .6.sp
                            )
                        }
                    }
                }
                if (animations) {
                    AnimatedContent(
                        targetState = currentPage,
                        transitionSpec = {
                            val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                            (slideInHorizontally(tween(340)) { full -> direction * full } + fadeIn(tween(260)))
                                .togetherWith(slideOutHorizontally(tween(280)) { full -> -direction * full } + fadeOut(tween(220)))
                        },
                        label = "cambio_de_monitores_cyber_cockpit"
                    ) { page -> content(page) }
                } else {
                    content(currentPage)
                }
            }
            BottomNavigation(currentPage, ::go)
        }
    }
}

@Composable
private fun BrandHero(data: BridgeUiData) {
    Box(
        modifier = Modifier.fillMaxWidth().height(117.dp)
            .background(Brush.verticalGradient(listOf(Color(0xFF06172B), Color(0xFF01101E))))
    ) {
        Image(
            painter = painterResource(R.drawable.brand_banner),
            contentDescription = "Logotipo OTG POWER BRIDGE — GENAI INDUSTRIES",
            modifier = Modifier.fillMaxWidth().height(91.dp),
            contentScale = ContentScale.Fit
        )
        Row(
            Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                .background(Color(0xB6001025))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("◈  CYBER COCKPIT", color = Cyan, fontWeight = FontWeight.Black,
                letterSpacing = 1.3.sp, fontSize = 11.sp)
            Text("V0.6.1  •  ANDROID", color = Amber, fontWeight = FontWeight.Bold,
                fontSize = 10.sp)
            Text(if (data.power.charging) "● CARGANDO" else "● EN VIVO",
                color = if (data.power.charging) Lime else Cyan,
                fontWeight = FontWeight.Bold, fontSize = 9.sp)
        }
        Canvas(Modifier.fillMaxWidth().height(2.dp).align(Alignment.BottomCenter)) {
            drawLine(brush = Brush.horizontalGradient(listOf(Blue, Cyan, Amber, Blue)),
                start = Offset.Zero, end = Offset(size.width, 0f), strokeWidth = 2.dp.toPx())
        }
    }
}

@Composable
private fun BottomNavigation(selected: Page, onSelect: (Page) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xF60A2746), Color(0xFF020918))))
            .border(1.dp, Brush.horizontalGradient(listOf(Blue, Titanium, Cyan)),
                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(horizontal = 5.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Page.values().forEach { page ->
            val active = selected == page
            val tone = if (active) Amber else Cyan
            Column(
                modifier = Modifier.weight(1f).height(58.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Brush.verticalGradient(
                        if (active) listOf(Color(0xFF574121), Color(0xFF112C49), Color(0xFF0A1425))
                        else listOf(Color(0xFF132D4B), Color(0xFF041023))))
                    .border(if (active) 1.3.dp else .6.dp, tone.copy(alpha = if (active) 1f else .30f), RoundedCornerShape(7.dp))
                    .clickable { onSelect(page) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(page.icon, color = tone, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(page.caption, color = if (active) TextHi else TextSub,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 9.sp, maxLines = 1, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun NeonPanel(
    title: String, accent: Color = Cyan,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Box(Modifier.fillMaxWidth()
        .shadow(12.dp, shape, ambientColor = accent.copy(alpha = .21f), spotColor = accent.copy(alpha = .29f))
        .clip(shape)
        .background(Brush.verticalGradient(listOf(Color(0xF4224262), Color(0xF20A1A2E), Color(0xEF061325))))
    ) {
        Canvas(Modifier.matchParentSize()) {
            val line = 1.dp.toPx()
            drawRoundRect(
                brush = Brush.linearGradient(listOf(accent.copy(alpha = .85f), Titanium.copy(alpha = .62f), Blue.copy(alpha = .35f), accent.copy(alpha = .7f))),
                cornerRadius = CornerRadius(12.dp.toPx()),
                style = Stroke(width = line)
            )
            drawLine(Titanium.copy(alpha = .30f), Offset(14.dp.toPx(), 4.dp.toPx()),
                Offset(size.width * .62f, 4.dp.toPx()), strokeWidth = 1.dp.toPx())
            drawLine(accent.copy(alpha = .65f), Offset(size.width * .72f, size.height - 4.dp.toPx()),
                Offset(size.width - 16.dp.toPx(), size.height - 4.dp.toPx()), strokeWidth = 2.dp.toPx())
            val x = size.width - 25.dp.toPx()
            drawCircle(accent.copy(alpha = .42f), 3.dp.toPx(), Offset(x, 15.dp.toPx()))
            drawCircle(Color.White.copy(alpha = .6f), 1.dp.toPx(), Offset(x, 15.dp.toPx()))
        }
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(4.dp).height(18.dp).clip(RoundedCornerShape(2.dp))
                    .background(Brush.verticalGradient(listOf(Color.White, accent, Blue))))
                Spacer(Modifier.width(8.dp))
                Text(title.uppercase(Locale.ROOT), Modifier.weight(1f), color = TextHi,
                    fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = .8.sp)
                Text("◈", color = accent, fontSize = 13.sp)
            }
            Canvas(Modifier.fillMaxWidth().height(2.dp)) {
                drawLine(brush = Brush.horizontalGradient(listOf(accent, accent.copy(alpha = .4f), Color.Transparent)),
                    start = Offset.Zero, end = Offset(size.width, 0f), strokeWidth = 1.7.dp.toPx())
            }
            content()
        }
    }
}

@Composable
private fun NeonButton(
    label: String, enabled: Boolean, primary: Boolean = true,
    modifier: Modifier = Modifier, onClick: () -> Unit
) {
    val tone = if (primary) Cyan else Amber
    Box(
        modifier = modifier.height(49.dp).clip(RoundedCornerShape(8.dp))
            .background(Brush.verticalGradient(if (enabled)
                listOf(tone.copy(alpha = .34f), Color(0xFF143452), Color(0xFF051222))
                else listOf(Color(0xFF1C2D40), Color(0xFF0A1A2D))))
            .border(1.dp, if (enabled) tone else Color(0xFF4A5A6B), RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick), contentAlignment = Alignment.Center
    ) {
        Text(label, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp,
            color = if (enabled) TextHi else Color(0xFF8DA0B1),
            maxLines = 1, textAlign = TextAlign.Center, letterSpacing = .1.sp)
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
    val safe = percent.coerceIn(0, 100)
    val fill by animateFloatAsState(targetValue = if (percent < 0) 0f else safe / 100f,
        animationSpec = tween(620), label = "indicador_bateria_real")
    Box(modifier.size(132.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val rad = size.minDimension * .435f
            drawCircle(brush = Brush.radialGradient(listOf(Color(0xFF15385B), Color(0xFF020F20))),
                radius = rad, center = Offset(cx, cy))
            drawCircle(Titanium.copy(alpha = .45f), radius = rad, style = Stroke(width = 2.dp.toPx()))
            for (i in 0..59) {
                val angle = ((i * 6f) - 90f) * PI / 180.0
                val inner = rad * if (i % 5 == 0) .81f else .88f
                val outer = rad * .94f
                drawLine(
                    if (i / 60f <= fill) accent else Color(0xFF2A516A),
                    Offset(cx + cos(angle).toFloat() * inner, cy + sin(angle).toFloat() * inner),
                    Offset(cx + cos(angle).toFloat() * outer, cy + sin(angle).toFloat() * outer),
                    strokeWidth = if (i % 5 == 0) 2.dp.toPx() else 1.dp.toPx()
                )
            }
            drawArc(Color(0xFF1A405B), -225f, 270f, false, style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round))
            if (percent >= 0) {
                drawArc(brush = Brush.sweepGradient(listOf(Blue, Cyan, accent, Amber)),
                    startAngle = -225f, sweepAngle = 270f * fill,
                    useCenter = false, style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round))
            }
            drawCircle(Cyan.copy(alpha = .10f), radius = rad * .69f,
                style = Stroke(1.dp.toPx()))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = TextSub, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center, letterSpacing = .7.sp)
            Text(if (percent < 0) "—" else "$percent%", color = accent,
                fontSize = 29.sp, fontWeight = FontWeight.Black)
            Text("ANDROID", color = Titanium, fontSize = 8.sp, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun LiveDiagram(data: BridgeUiData, animated: Boolean) {
    val transition = rememberInfiniteTransition(label = "cyber_energia_datos")
    val phase by transition.animateFloat(
        0f, 1f,
        animationSpec = infiniteRepeatable(tween(1700), RepeatMode.Restart),
        label = "flujo_visual_estado_real"
    )
    val dataActive = data.usb.devices.isNotEmpty() || data.removable
    val powerActive = data.power.externalPower
    Box(Modifier.fillMaxWidth().height(152.dp)) {
        Canvas(Modifier.matchParentSize()) {
            val center = Offset(size.width / 2f, size.height * .47f)
            val r = size.height * .33f
            for (i in 0..3) {
                drawCircle(if (i % 2 == 0) Cyan.copy(alpha = .30f) else Blue.copy(alpha = .35f),
                    radius = r + i * 7.dp.toPx(), center = center, style = Stroke(1.dp.toPx()))
            }
            val y = size.height * .50f
            val start = size.width * .19f
            val middle = size.width * .50f
            val end = size.width * .81f
            drawLine(if (dataActive) Cyan else Color(0xFF375069), Offset(start, y), Offset(middle, y),
                strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
            drawLine(if (powerActive) Amber else Color(0xFF375069), Offset(middle, y), Offset(end, y),
                strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
            if (animated && dataActive) {
                drawCircle(Color.White, 3.5.dp.toPx(), Offset(start + (middle-start)*phase, y))
            }
            if (animated && powerActive) {
                drawCircle(Amber, 3.5.dp.toPx(), Offset(end + (middle-end)*phase, y))
            }
            val yy = size.height*.92f
            drawLine(brush = Brush.horizontalGradient(listOf(Color.Transparent, Cyan, Amber, Color.Transparent)),
                start = Offset(0f, yy), end = Offset(size.width, yy), strokeWidth = 1.5.dp.toPx())
        }
        Row(
            Modifier.fillMaxWidth().align(Alignment.Center).padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CyberNode("▣", "ANDROID", if (data.session) "SAF ACTIVO" else "SIN SESIÓN", data.session, Cyan)
            CyberNode("⌁", "OTG / HUB", "${data.usb.devices.size} USB", dataActive, Amber)
            CyberNode("ϟ", "ENERGÍA", if (powerActive) "DETECTADA" else "SIN FUENTE", powerActive, Lime)
        }
        Text("Diagrama de conexión ilustrativo; NO mide tráfico USB. Hub y fuente pueden no identificarse.",
            Modifier.align(Alignment.BottomCenter).padding(bottom = 0.dp),
            color = TextSub, fontSize = 9.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun CyberNode(glyph: String, title: String, subtitle: String, active: Boolean, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(98.dp)) {
        Box(
            modifier = Modifier.size(62.dp).clip(RoundedCornerShape(12.dp))
                .background(Brush.radialGradient(listOf(accent.copy(alpha = if (active) .5f else .10f), Color(0xFF041123))))
                .border(1.3.dp, if (active) accent else Titanium.copy(alpha = .42f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(glyph, color = if (active) accent else TextSub, fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.height(4.dp))
        Text(title, color = TextHi, fontSize = 9.sp, fontWeight = FontWeight.Black, maxLines = 1)
        Text(subtitle, color = if (active) accent else TextSub,
            fontSize = 8.sp, textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Composable
private fun HomePage(data: BridgeUiData, actions: BridgeActions, animations: Boolean, go: (Page) -> Unit) {
    NeonPanel("Centro de mando • Cyber Cockpit", Cyan) {
        val state = when {
            data.session && data.simultaneous -> "ARCHIVOS + ALIMENTACIÓN"
            data.session -> "SESIÓN DE ARCHIVOS ACTIVA"
            else -> "SIN SESIÓN DE ARCHIVOS"
        }
        LivePill(state, if (data.session) true else null,
            if (data.session) "Carpeta autorizada: ${data.selectedRoot?.name ?: "Sin etiqueta"}"
            else "Conectá una carpeta real con el selector de Android")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NeonButton("⛓  CONECTAR", !data.busy, modifier = Modifier.weight(1f), onClick = actions.connect)
            NeonButton("✕  DESCONECTAR", data.session && !data.busy,
                primary = false, modifier = Modifier.weight(1f), onClick = actions.disconnect)
        }
        LiveDiagram(data, animations)
        Text("CONEXIÓN • ENERGÍA • ARCHIVOS", Modifier.fillMaxWidth(),
            fontSize = 9.sp, color = Cyan, letterSpacing = 1.6.sp, textAlign = TextAlign.Center)
    }
    NeonPanel("Instrumentación de energía", Amber) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CircleGauge(data.power.percent, "BATERÍA", if (data.power.percent in 0..15) Amber else Lime)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Metric("Estado", if (data.power.charging) "CARGANDO" else "SIN CARGA", if (data.power.charging) Lime else Amber)
                Metric("Fuente", data.power.source)
                Metric("Power bank", "Sin telemetría", Amber)
                Text("La carga real depende del teléfono y del hub USB-C PD.", color = TextSub, fontSize = 10.sp)
            }
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CyberMiniMonitor("USB HOST", if (data.usb.hostFeatureReported) "SÍ" else "NO", Cyan, Modifier.weight(1f))
        CyberMiniMonitor("EQUIPOS", "${data.usb.devices.size}", Amber, Modifier.weight(1f))
        CyberMiniMonitor("EXTRAÍBLE", if (data.removable) "SÍ" else "—", Lime, Modifier.weight(1f))
    }
    NeonPanel("Monitor de transferencia", Cyan) {
        val pct = data.transfer?.let { TelemetryFormats.percent(it.copiedBytes, it.totalBytes) }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CircleGauge(pct ?: -1, "COPIA", if (data.transfer?.verified == true) Lime else Cyan,
                modifier = Modifier.size(120.dp))
            Column(Modifier.weight(1f)) {
                TransferSummary(data.transfer)
            }
        }
        NeonButton("ABRIR CENTRO DE TRANSFERENCIAS  ›", true, onClick = { go(Page.TRANSFER) })
    }
    NeonPanel("Almacenamiento real", Amber) {
        Metric("Carpeta autorizada", data.selectedRoot?.name ?: "Sin seleccionar")
        Metric("Capacidad disponible", TelemetryFormats.bytes(data.evidence.availableBytes))
        Metric("Carga + extraíble", if (data.simultaneous) "Ambos observados" else "No confirmado",
            if (data.simultaneous) Lime else Amber)
        NeonButton("EXPLORADOR DE ARCHIVOS  ›", true, onClick = { go(Page.STORAGE) })
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NeonButton("HERRAMIENTAS", true, modifier = Modifier.weight(1f), onClick = { go(Page.TOOLS) })
        NeonButton("ACTUALIZAR", !data.busy, primary = false, modifier = Modifier.weight(1f),
            onClick = actions.refresh)
    }
}

@Composable
private fun CyberMiniMonitor(title: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Box(modifier.height(89.dp).clip(RoundedCornerShape(9.dp))
        .background(Brush.verticalGradient(listOf(Color(0xFF254561), Color(0xFF061426))))
        .border(1.dp, accent.copy(alpha = .72f), RoundedCornerShape(9.dp))) {
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = TextSub, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = .2.sp)
            Spacer(Modifier.height(6.dp))
            Text(value, color = accent, fontSize = 23.sp, fontWeight = FontWeight.Black)
        }
        Box(Modifier.fillMaxWidth().height(2.dp).align(Alignment.BottomCenter).background(accent.copy(alpha = .58f)))
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
    NeonPanel("↔ Estación de transferencia", Cyan) {
        val percent = data.transfer?.let { TelemetryFormats.percent(it.copiedBytes, it.totalBytes) }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            CircleGauge(percent ?: -1, "TRANSFER", Cyan, modifier = Modifier.size(118.dp))
            Column(Modifier.weight(1f)) {
                Text(if (data.transfer == null) "SIN COPIA ACTIVA" else "OPERACIÓN REAL", color = Cyan,
                    fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                Text(if (data.transfer == null) "Seleccioná archivos para iniciar" else data.transfer.stage,
                    color = TextSub, fontSize = 11.sp)
            }
        }
    }
    NeonPanel("Gestor de transferencias", Amber) {
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
    NeonPanel("◈ Radar de almacenamiento", Cyan) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CyberMiniMonitor("DISPOSITIVOS", "${data.usb.devices.size}", Cyan, Modifier.weight(1f))
            CyberMiniMonitor("SESION", if (data.session) "ON" else "OFF", Amber, Modifier.weight(1f))
            CyberMiniMonitor("EXTRAÍBLE", if (data.removable) "SÍ" else "—", Lime, Modifier.weight(1f))
        }
    }
    NeonPanel("Almacenamiento conectado", Amber) {
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
    NeonPanel("◈ Diagnóstico en vivo", Cyan) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CyberMiniMonitor("BATERÍA", if (data.power.percent >= 0) "${data.power.percent}%" else "—", Lime, Modifier.weight(1f))
            CyberMiniMonitor("USB HOST", if (data.usb.hostFeatureReported) "ON" else "OFF", Cyan, Modifier.weight(1f))
            CyberMiniMonitor("EQUIPOS", "${data.usb.devices.size}", Amber, Modifier.weight(1f))
        }
    }
    NeonPanel("Herramientas y diagnóstico", Amber) {
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
        SettingSwitch("Efectos de flujo animados", "Transiciones de pantallas y flujo animado solo si existe una conexión observada.",
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
        Metric("Versión candidata", "0.6.1 Cyber Cockpit")
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

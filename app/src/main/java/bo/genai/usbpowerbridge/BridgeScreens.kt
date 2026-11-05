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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
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
import kotlin.math.max

/** Capa nativa V0.6.3. Visuales optimizados; métricas solo de API Android y SAF. */
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
            contentScale = ContentScale.Crop
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
                                .padding(horizontal = 9.dp, vertical = if (compact) 5.dp else 8.dp),
                            verticalArrangement = Arrangement.spacedBy(if (compact) 7.dp else 9.dp)
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
                                "GENAI INDUSTRIES  •  CYBER COCKPIT V0.6.3  •  DATOS REALES",
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
    Column(Modifier.fillMaxWidth().background(Color(0xFF020A16))) {
        Image(
            painter = painterResource(R.drawable.cyber_hero_v062),
            contentDescription = "OTG POWER BRIDGE — GENAI INDUSTRIES",
            modifier = Modifier.fillMaxWidth().height(104.dp),
            contentScale = ContentScale.FillBounds
        )
        Row(
            Modifier.fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Color(0xFF102F4A), Color(0xFF031328), Color(0xFF172E45))))
                .padding(horizontal = 12.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("◈ CYBER COCKPIT V0.6.3", color = Cyan, fontSize = 10.sp,
                letterSpacing = .5.sp, fontWeight = FontWeight.Black)
            Text("● ${if (data.power.charging) "CARGANDO" else "ENERGÍA: ${data.power.source}"}",
                color = if (data.power.charging) Lime else Amber, fontSize = 9.sp,
                maxLines = 1, fontWeight = FontWeight.Bold)
        }
        Canvas(Modifier.fillMaxWidth().height(3.dp)) {
            drawLine(Brush.horizontalGradient(listOf(Color.Transparent, Blue, Cyan, Amber, Color.Transparent)),
                Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 3.dp.toPx())
        }
    }
}

@Composable
private fun BottomNavigation(selected: Page, onSelect: (Page) -> Unit) {
    val art = when (selected) {
        Page.HOME -> R.drawable.cyber_nav_home_v062
        Page.TRANSFER -> R.drawable.cyber_nav_transfer_v062
        Page.STORAGE -> R.drawable.cyber_nav_storage_v062
        Page.TOOLS -> R.drawable.cyber_nav_tools_v062
        Page.SETTINGS -> R.drawable.cyber_nav_settings_v062
    }
    Box(Modifier.fillMaxWidth().height(88.dp).background(SpaceBg)) {
        Image(
            painter = painterResource(art),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Row(Modifier.fillMaxSize()) {
            Page.values().forEach { page ->
                Box(
                    Modifier.weight(1f).fillMaxSize()
                        .semantics { contentDescription = "Ir a ${page.caption}" }
                        .clickable { onSelect(page) }
                )
            }
        }
    }
}

@Composable
private fun NeonPanel(
    title: String, accent: Color = Cyan,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(Modifier.fillMaxWidth().padding(horizontal = 1.dp, vertical = 2.dp)
        .shadow(12.dp, RoundedCornerShape(10.dp),
            ambientColor = accent.copy(alpha = .34f), spotColor = accent.copy(alpha = .4f))
    ) {
        Canvas(Modifier.matchParentSize()) {
            val bevel = 11.dp.toPx()
            val w = size.width
            val h = size.height
            val body = Path().apply {
                moveTo(bevel, 0f); lineTo(w-bevel, 0f); lineTo(w, bevel)
                lineTo(w, h-bevel); lineTo(w-bevel,h); lineTo(bevel,h)
                lineTo(0f,h-bevel); lineTo(0f,bevel); close()
            }
            drawPath(body, Brush.verticalGradient(listOf(Color(0xFF1B4164), Color(0xFF061A30), Color(0xFF020C1A))))
            drawPath(body, Brush.linearGradient(listOf(Blue, accent, Titanium, Color(0xFF173755), accent)),
                style = Stroke(width = 1.8.dp.toPx(), join = StrokeJoin.Round))
            drawPath(body, accent.copy(alpha = .15f),
                style = Stroke(width = 5.5.dp.toPx(), join = StrokeJoin.Round))
            val stripeY = 28.dp.toPx().coerceAtMost(h*.2f)
            drawLine(Brush.horizontalGradient(listOf(accent.copy(alpha = .86f), Blue.copy(alpha = .45f), Color.Transparent)),
                Offset(bevel, stripeY), Offset(w-bevel, stripeY), strokeWidth = 2.dp.toPx())
            for (index in 1..9) {
                val xx = index * w/10f
                drawLine(Color(0x141FB9ED), Offset(xx, stripeY + 8.dp.toPx()),
                    Offset(xx, h - bevel), strokeWidth = .4.dp.toPx())
            }
            drawLine(Brush.horizontalGradient(listOf(Color.Transparent, Cyan, Amber, Color.Transparent)),
                Offset(bevel,h-3.dp.toPx()), Offset(w-bevel,h-3.dp.toPx()), strokeWidth = 2.dp.toPx())
            val k = 17.dp.toPx()
            drawLine(Amber.copy(alpha = .85f),Offset(0f,h-k), Offset(0f,h-bevel), strokeWidth = 3.dp.toPx())
            drawLine(Cyan.copy(alpha = .88f),Offset(bevel,0f), Offset(k+29.dp.toPx(),0f),strokeWidth=3.dp.toPx())
            drawCircle(Brush.radialGradient(listOf(accent.copy(alpha = .26f), Color.Transparent)),
                radius = 62.dp.toPx(), center = Offset(w*.82f, h*.18f))
        }
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(5.dp).height(20.dp).background(
                    Brush.verticalGradient(listOf(Color.White, accent, Blue))))
                Spacer(Modifier.width(8.dp))
                Text(title.uppercase(Locale.ROOT), Modifier.weight(1f), color = TextHi,
                    fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = .35.sp,
                    lineHeight = 17.sp, maxLines = 3)
                Text("◀  ◈  ▶", color = accent.copy(alpha = .8f), fontSize = 9.sp)
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
    val tone = if (primary) Cyan else Color(0xFFFF7767)
    val shape = RoundedCornerShape(topStart = 16.dp, bottomEnd = 16.dp, topEnd = 4.dp, bottomStart = 4.dp)
    Box(modifier.height(52.dp).shadow(7.dp, shape,
        ambientColor = tone.copy(alpha = if (enabled) .35f else 0f))
        .clip(shape)
        .background(Brush.verticalGradient(if (enabled)
            listOf(tone.copy(alpha = .52f), Color(0xFF165078), Color(0xFF031124))
            else listOf(Color(0xFF2A374A), Color(0xFF0A182C))))
        .border(1.3.dp, if (enabled) tone else Color(0xFF536474), shape)
        .clickable(enabled = enabled, onClick = onClick), contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawLine(Color.White.copy(alpha = if (enabled) .47f else .14f),
                Offset(17.dp.toPx(), 3.dp.toPx()), Offset(size.width-8.dp.toPx(),3.dp.toPx()),
                strokeWidth = 1.3.dp.toPx())
            drawLine(tone.copy(alpha = if (enabled) .92f else .2f),
                Offset(10.dp.toPx(), size.height-3.dp.toPx()),
                Offset(size.width-15.dp.toPx(),size.height-3.dp.toPx()),strokeWidth=2.dp.toPx())
        }
        Text(label, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp,
            color = if (enabled) TextHi else Color(0xFF8DA0B1),
            maxLines = 2, lineHeight = 13.sp, textAlign = TextAlign.Center, letterSpacing = .2.sp,
            modifier = Modifier.padding(horizontal = 6.dp))
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
        Text(label, modifier = Modifier.weight(1f), color = TextSub,
            fontSize = 12.sp, lineHeight = 16.sp)
        Text(value, modifier = Modifier.weight(1.05f), color = color,
            textAlign = TextAlign.End, fontSize = 12.sp,
            lineHeight = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CircleGauge(
    percent: Int, title: String, accent: Color = Lime,
    modifier: Modifier = Modifier.size(132.dp), compact: Boolean = false
) {
    val safe = percent.coerceIn(0, 100)
    val fill by animateFloatAsState(targetValue = if (percent < 0) 0f else safe / 100f,
        animationSpec = tween(620), label = "indicador_bateria_real")
    // Respect the size specified by the parent. Fixed 132 dp overflowed narrow instrument cards.
    Box(modifier, contentAlignment = Alignment.Center) {
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
            Text(title, color = TextSub, fontSize = if (compact) 7.sp else 9.sp,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                letterSpacing = if (compact) 0.sp else .6.sp,
                maxLines = 1)
            Text(if (percent < 0) "—" else "$percent%", color = accent,
                fontSize = if (compact) 21.sp else 29.sp,
                fontWeight = FontWeight.Black, maxLines = 1)
            if (!compact) Text("ANDROID", color = Titanium,
                fontSize = 8.sp, letterSpacing = 1.sp)
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
    val usbObserved = data.usb.devices.isNotEmpty() || data.removable
    // Android reports external power to the PHONE; it does NOT prove the supply crosses the hub.
    val phonePoweredExternally = data.power.externalPower
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.fillMaxWidth().height(132.dp)) {
            Canvas(Modifier.matchParentSize()) {
                val center = Offset(size.width / 2f, size.height * .46f)
                val radius = size.height * .34f
                for (i in 0..3) {
                    drawCircle(
                        if (i % 2 == 0) Cyan.copy(alpha = .24f) else Blue.copy(alpha = .25f),
                        radius = radius + i * 7.dp.toPx(), center = center,
                        style = Stroke(1.dp.toPx())
                    )
                }
                val y = size.height * .43f
                val first = size.width * .17f
                val middle = size.width * .5f
                val last = size.width * .83f
                drawLine(if (usbObserved) Cyan else Color(0xFF375069),
                    Offset(first, y), Offset(middle, y),
                    strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                drawLine(if (data.simultaneous) Amber else Color(0xFF375069),
                    Offset(middle, y), Offset(last, y),
                    strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                if (animated && usbObserved) {
                    drawCircle(Color.White, 3.dp.toPx(),
                        Offset(first + (middle - first) * phase, y))
                }
                if (animated && data.simultaneous) {
                    drawCircle(Amber, 3.dp.toPx(),
                        Offset(last + (middle - last) * phase, y))
                }
                val bottom = size.height * .92f
                drawLine(Brush.horizontalGradient(
                    listOf(Color.Transparent, Cyan, Amber, Color.Transparent)),
                    Offset(0f, bottom), Offset(size.width, bottom),
                    strokeWidth = 1.5.dp.toPx())
            }
            Row(
                Modifier.fillMaxWidth().align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CyberNode("▣", "ANDROID",
                    if (data.session) "CARPETA SAF" else "SIN SESIÓN",
                    data.session, Cyan, Modifier.weight(1f))
                CyberNode("⌁", "USB / OTG",
                    "${data.usb.devices.size} EQUIPOS",
                    usbObserved, Amber, Modifier.weight(1f))
                CyberNode("ϟ", "ALIMENTACIÓN",
                    if (phonePoweredExternally) "EXTERNA" else "NO DETECTADA",
                    phonePoweredExternally, Lime, Modifier.weight(1f))
            }
        }
        Text(
            "ESQUEMA ILUSTRATIVO · Estado leído de Android; no confirma el recorrido " +
                "físico por el hub ni mide la potencia USB-PD.",
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xE0041526))
                .padding(horizontal = 9.dp, vertical = 8.dp),
            color = TextSub, fontSize = 11.sp,
            lineHeight = 14.sp, textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CyberNode(
    glyph: String, title: String, subtitle: String,
    active: Boolean, accent: Color, modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(
            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp))
                .background(Brush.radialGradient(
                    listOf(accent.copy(alpha = if (active) .5f else .10f), Color(0xFF041123))))
                .border(1.3.dp,
                    if (active) accent else Titanium.copy(alpha = .42f),
                    RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(glyph, color = if (active) accent else TextSub,
                fontSize = 29.sp, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.height(6.dp))
        Text(title, color = TextHi, fontSize = 10.sp,
            fontWeight = FontWeight.Black, maxLines = 2,
            lineHeight = 12.sp, textAlign = TextAlign.Center)
        Text(subtitle, color = if (active) accent else TextSub,
            fontSize = 9.sp, lineHeight = 12.sp,
            textAlign = TextAlign.Center, maxLines = 2)
    }
}

@Composable
private fun InstrumentCell(
    label: String, percent: Int?, primaryValue: String, secondary: String,
    accent: Color, modifier: Modifier = Modifier,
    gaugeTitle: String = label,
    body: @Composable (() -> Unit)? = null
) {
    Box(modifier.background(Brush.verticalGradient(
        listOf(Color(0xFF21496F), Color(0xFF071D32), Color(0xFF020D1A))))
        .border(1.dp, accent.copy(alpha = .87f), RoundedCornerShape(7.dp))) {
        Column(Modifier.fillMaxWidth().padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(label, color = TextHi, fontSize = 9.sp, maxLines = 2,
                lineHeight = 11.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            if (body != null) body()
            else CircleGauge(percent ?: -1, gaugeTitle, accent,
                modifier = Modifier.size(81.dp), compact = true)
            Text(primaryValue, color = accent, fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center,
                lineHeight = 14.sp, maxLines = 3)
            Text(secondary, color = TextSub, fontSize = 10.sp,
                textAlign = TextAlign.Center, lineHeight = 13.sp, maxLines = 4)
        }
    }
}

@Composable
private fun HomePage(data: BridgeUiData, actions: BridgeActions, animations: Boolean, go: (Page) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        NeonButton("⛓  CONECTAR", !data.busy,
            modifier = Modifier.weight(1f), onClick = actions.connect)
        NeonButton("⛓̸  DESCONECTAR", data.session && !data.busy, primary = false,
            modifier = Modifier.weight(1f), onClick = actions.disconnect)
    }
    NeonPanel("Centro de control · Estado en vivo", Lime) {
        val state = when {
            data.session && data.simultaneous -> "ARCHIVOS + ALIMENTACIÓN DETECTADOS"
            data.session -> "SESIÓN DE ARCHIVOS ACTIVA"
            else -> "SIN SESIÓN DE ARCHIVOS"
        }
        LivePill(state, if (data.session) true else null,
            if (data.session) "Carpeta autorizada: ${data.selectedRoot?.name ?: "Sin etiqueta"}"
            else "Conecta una carpeta con el selector real de Android")
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            CyberMiniMonitor("HOST USB", if (data.usb.hostFeatureReported) "SÍ" else "NO", Cyan, Modifier.weight(1f))
            CyberMiniMonitor("DISPOSITIVOS", "${data.usb.devices.size}", Amber, Modifier.weight(1f))
            CyberMiniMonitor("CARPETA", if (data.session) "OK" else "—", Lime, Modifier.weight(1f))
        }
    }
    val phone = data.phoneSpace
    val storagePercent = if (phone.total != null && phone.available != null && phone.total > 0L)
        ((phone.total - phone.available).coerceAtLeast(0L) * 100L / phone.total).toInt().coerceIn(0,100) else null
    NeonPanel("Instrumentación del dispositivo", Amber) {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            InstrumentCell("BATERÍA", data.power.percent,
                if (data.power.charging) "CARGANDO" else "SIN CARGA",
                data.power.source, if (data.power.percent in 0..15) Amber else Lime,
                Modifier.weight(1f))
            InstrumentCell("POWER BANK", null, "SIN TELEMETRÍA",
                "Android no informa su carga", Amber, Modifier.weight(1f)) {
                Box(Modifier.size(95.dp), contentAlignment = Alignment.Center) {
                    Image(painterResource(R.drawable.cyber_otg_core_v062), null,
                        Modifier.size(77.dp), contentScale = ContentScale.Fit)
                    Text("⚡", color = Amber, fontSize = 33.sp, fontWeight = FontWeight.Bold)
                }
            }
            InstrumentCell("ALMACENAMIENTO", storagePercent,
                if (storagePercent == null) "SIN DATOS" else "$storagePercent% OCUPADO",
                "${TelemetryFormats.bytes(phone.available)} libres en datos",
                Cyan, Modifier.weight(1f), gaugeTitle = "OCUPADO")
        }
        Text("El porcentaje de almacenamiento indica ESPACIO OCUPADO en la partición de datos; " +
            "no es la capacidad libre. Sin telemetría inventada del power bank.",
            color = TextSub, fontSize = 11.sp, lineHeight = 14.sp)
    }
    NeonPanel("Diagrama de conexión · Datos y energía", Cyan) {
        LiveDiagram(data, animations)
    }
    NeonPanel("Transferencia en tiempo real", Cyan) {
        val pct = data.transfer?.let { TelemetryFormats.percent(it.copiedBytes, it.totalBytes) }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            CircleGauge(pct ?: -1, "COPIA", if (data.transfer?.verified == true) Lime else Cyan,
                modifier = Modifier.size(119.dp))
            Column(Modifier.weight(1f)) {
                TransferSummary(data.transfer)
            }
        }
        TransferSpeedPlot(data.transfer)
        NeonButton("ABRIR CENTRO DE TRANSFERENCIAS  ›", true, onClick = { go(Page.TRANSFER) })
    }
    NeonPanel("Monitores de almacenamiento", Amber) {
        Metric("Carpeta autorizada", data.selectedRoot?.name ?: "Sin seleccionar")
        Metric("Espacio libre autorizado", TelemetryFormats.bytes(data.evidence.availableBytes))
        Metric("Carga + extraíble", if (data.simultaneous) "Ambos observados" else "No confirmado",
            if (data.simultaneous) Lime else Amber)
        NeonButton("EXPLORAR MEMORIA USB / TELÉFONO  ›", true, onClick = { go(Page.STORAGE) })
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NeonButton("HERRAMIENTAS", true, modifier = Modifier.weight(1f), onClick = { go(Page.TOOLS) })
        NeonButton("ACTUALIZAR", !data.busy, primary = false,
            modifier = Modifier.weight(1f), onClick = actions.refresh)
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
private fun TransferSpeedPlot(tx: TransferDisplay?) {
    val samples = remember(tx?.name, tx?.startElapsedMs) { mutableStateListOf<Float>() }
    var prior by remember(tx?.name, tx?.startElapsedMs) { mutableStateOf<Pair<Long, Long>?>(null) }
    LaunchedEffect(tx?.copiedBytes, tx?.startElapsedMs) {
        if (tx != null && tx.startElapsedMs > 0L) {
            val now = SystemClock.elapsedRealtime()
            val last = prior
            if (last != null && now > last.first && tx.copiedBytes >= last.second) {
                val bytesPerSec = (tx.copiedBytes - last.second).toFloat() * 1000f / (now - last.first)
                samples.add(bytesPerSec)
                if (samples.size > 34) samples.removeAt(0)
            }
            prior = now to tx.copiedBytes
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text("VELOCIDAD OBSERVADA · ${if (samples.isEmpty()) "SIN MUESTRAS" else "${samples.size} MUESTRAS"}",
            color = Cyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Canvas(Modifier.fillMaxWidth().height(44.dp)) {
            drawRect(Brush.verticalGradient(listOf(Color(0xA90D2742), Color(0xA0021120))))
            val horizontal = size.height / 4f
            for (i in 1..3) drawLine(Cyan.copy(alpha=.13f), Offset(0f,i*horizontal),
                Offset(size.width,i*horizontal),strokeWidth=.6.dp.toPx())
            if (samples.size > 1) {
                val maxValue = max(1f, samples.maxOrNull() ?: 1f)
                val points = samples.toList()
                val path = Path()
                points.forEachIndexed { index, v ->
                    val x = index.toFloat() / max(1,points.size-1) * size.width
                    val y = size.height-4.dp.toPx() - (v/maxValue)*(size.height-8.dp.toPx())
                    if (index == 0) path.moveTo(x,y) else path.lineTo(x,y)
                }
                drawPath(path, Brush.horizontalGradient(listOf(Cyan,Lime,Amber)),
                    style=Stroke(width=2.dp.toPx(), cap=StrokeCap.Round, join=StrokeJoin.Round))
            }
        }
    }
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
    NeonPanel("Transferencia real en curso") {
        TransferSummary(data.transfer)
        TransferSpeedPlot(data.transfer)
    }
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
        if (fraction != null) Text(
            "ESPACIO OCUPADO: ${(fraction * 100f).toInt().coerceIn(0, 100)}%",
            color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
    NeonPanel("◈ Diagnóstico y telemetría en vivo", Cyan) {
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
        Metric("Potencia real (W)", "No disponible: Android no mide USB-PD", Amber)
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
            color = TextSub, fontSize = 12.sp, lineHeight = 16.sp)
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
        Text("Recursos gráficos aprobados: encabezado y navegación 3D. Métricas dinámicas en Compose.",
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
        Metric("Versión candidata", "0.6.3 Cyber Cockpit · precisión visual")
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
            Text(label, color = TextHi, fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp, lineHeight = 17.sp)
            Text(detail, color = TextSub, fontSize = 11.sp, lineHeight = 15.sp)
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

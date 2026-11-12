package io.github.nikalbazu24.gahvoria.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.nikalbazu24.gahvoria.core.Palettes
import kotlinx.coroutines.delay

@Composable
fun GahvoriaApp(s: AppState) {
    val dark = isSystemInDarkTheme()
    val scheme = Palettes.scheme(s.palette, s.mode, dark)
    val colors = Colors.from(scheme, scheme === Palettes.byId(s.palette).dark)
    GahvoriaTheme(colors) {
        val c = LocalColors.current
        Box(
            Modifier.fillMaxSize().background(c.bg)
                .background(Brush.radialGradient(listOf(c.accent.copy(alpha = if (c.dark) .24f else .16f), Color.Transparent),
                    center = Offset(900f, -60f), radius = 1300f))
                .background(Brush.verticalGradient(listOf(Color.Transparent, c.bg2.copy(alpha = .9f))))
                .safeDrawingPadding()
        ) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val rail = maxWidth > maxHeight && maxWidth > 600.dp
                val blurred = if (s.errorReport != null || s.intro != null) Modifier.blur(16.dp) else Modifier
                if (rail) Row(Modifier.fillMaxSize().then(blurred)) {
                    Bar(s, vertical = true, Modifier.fillMaxHeight().padding(12.dp))
                    Box(Modifier.weight(1f).fillMaxHeight()) { Content(s) }
                } else Column(Modifier.fillMaxSize().then(blurred)) {
                    Box(Modifier.weight(1f).fillMaxWidth()) { Content(s) }
                    Bar(s, vertical = false, Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp))
                }
                s.errorReport?.let { ErrorDialog(s, it) }
                s.intro?.let { IntroScreen(it, s.introIsUpdate) { s.finishIntro() } }
                s.notice?.let { n ->
                    NoticeCard(n, Modifier.align(Alignment.BottomCenter).padding(bottom = if (rail) 16.dp else 88.dp)) { s.notice = null }
                }
            }
        }
    }
}

@Composable
private fun Content(s: AppState) {
    val g = s.optionsFor
    if (g != null) { GameScreen(s, g); return }
    if (s.tab != Tab.LIBRARY) BackHandler { s.tab = Tab.LIBRARY }
    when (s.tab) {
        Tab.LIBRARY -> LibraryScreen(s)
        Tab.ADD -> AddScreen(s)
        Tab.UPDATES -> UpdatesScreen(s)
        Tab.SETTINGS -> SettingsScreen(s)
    }
}

/** A short message in the app's style: a colored icon by kind, the text and, if there is one, an action. */
@Composable
private fun NoticeCard(n: Notice, modifier: Modifier, onDone: () -> Unit) {
    val c = LocalColors.current
    LaunchedEffect(n) { delay(if (n.action != null) 8000L else 4000L); onDone() }
    val tint = when (n.kind) {
        NoticeKind.INFO -> c.accent3
        NoticeKind.OK -> c.ok
        NoticeKind.WARN -> c.warn
        NoticeKind.ERROR -> if (c.dark) Color(0xFFFF8E8E) else Color(0xFFC62F3A)
    }
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier.padding(horizontal = 16.dp).widthIn(max = 520.dp)
            .press(shape, onClick = onDone).background(c.bg2.copy(alpha = .95f), shape).border(1.dp, c.line, shape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NoticeIcon(n.kind, tint)
        Spacer(Modifier.width(12.dp))
        T(n.text, 13.sp, FontWeight.SemiBold, modifier = Modifier.weight(1f, fill = false))
        if (n.action != null) {
            Spacer(Modifier.width(12.dp))
            val chip = RoundedCornerShape(10.dp)
            Box(Modifier.press(chip) { n.onAction?.invoke(); onDone() }.padding(horizontal = 6.dp, vertical = 6.dp)) {
                T(n.action, 12.sp, FontWeight.ExtraBold, c.accent)
            }
        }
    }
}

/** The small symbol of each kind of notice, drawn in code so it takes the palette's colors. */
@Composable
private fun NoticeIcon(kind: NoticeKind, tint: Color) {
    Canvas(Modifier.size(30.dp)) {
        drawCircle(tint.copy(alpha = .2f))
        val k = 17.dp.toPx() / 24f
        val ox = (size.width - 24f * k) / 2f
        val oy = (size.height - 24f * k) / 2f
        fun o(x: Float, y: Float) = Offset(ox + x * k, oy + y * k)
        val w = 2f * k
        fun line(x1: Float, y1: Float, x2: Float, y2: Float) = drawLine(tint, o(x1, y1), o(x2, y2), w, StrokeCap.Round)
        when (kind) {
            NoticeKind.INFO -> {
                drawCircle(tint, 9f * k, o(12f, 12f), style = Stroke(w))
                line(12f, 11f, 12f, 16f); drawCircle(tint, 1.1f * k, o(12f, 8f))
            }
            NoticeKind.OK -> { line(5f, 12.5f, 9.5f, 17f); line(9.5f, 17f, 19f, 7.5f) }
            NoticeKind.WARN -> {
                val tri = androidx.compose.ui.graphics.Path().apply {
                    moveTo(o(12f, 4f).x, o(12f, 4f).y); lineTo(o(21f, 20f).x, o(21f, 20f).y); lineTo(o(3f, 20f).x, o(3f, 20f).y); close()
                }
                drawPath(tri, tint, style = Stroke(w, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                line(12f, 10f, 12f, 14f); drawCircle(tint, 1.1f * k, o(12f, 17f))
            }
            NoticeKind.ERROR -> {
                drawCircle(tint, 9f * k, o(12f, 12f), style = Stroke(w))
                line(9f, 9f, 15f, 15f); line(15f, 9f, 9f, 15f)
            }
        }
    }
}

/** The glass bar: Biblioteca, Añadir, Actualizar, Ajustes. Vertical on wide screens (Thor). */
@Composable
private fun Bar(s: AppState, vertical: Boolean, modifier: Modifier) {
    val c = LocalColors.current
    val shape = RoundedCornerShape(24.dp)
    val items = listOf(Tab.LIBRARY to "Biblioteca", Tab.ADD to "Añadir", Tab.UPDATES to "Actualizar", Tab.SETTINGS to "Ajustes")
    val pending = s.update.let { it is UpdateState.Available || it is UpdateState.Ready }
    val inner = @Composable { mod: Modifier, t: Tab, label: String ->
        val on = s.tab == t && s.optionsFor == null
        val itemShape = RoundedCornerShape(18.dp)
        Box(mod.press(itemShape, role = Role.Tab) { s.optionsFor = null; s.tab = t }
            .background(if (on) c.glass else Color.Transparent, itemShape).padding(vertical = 9.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TabIcon(t, if (on) c.accent else c.faint)
                Spacer(Modifier.height(4.dp))
                T(label, 10.sp, FontWeight.Bold, if (on) c.ink else c.faint, maxLines = 1)
            }
            if (t == Tab.UPDATES && pending) Box(Modifier.align(Alignment.TopEnd).padding(top = 2.dp, end = 10.dp).size(7.dp).clip(CircleShape).background(c.accent))
        }
    }
    if (vertical) Column(modifier.width(96.dp).clip(shape).background(c.glassStrong).border(1.dp, c.line, shape).padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.padding(vertical = 10.dp)) { Cartridge(40.dp) }
        items.forEach { (t, l) -> inner(Modifier.fillMaxWidth(), t, l) }
    } else Row(modifier.clip(shape).background(c.glassStrong).border(1.dp, c.line, shape).padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        items.forEach { (t, l) -> inner(Modifier.weight(1f), t, l) }
    }
}

/** Line icons drawn in code, so they take the palette's colors. */
@Composable
private fun TabIcon(t: Tab, color: Color) {
    Canvas(Modifier.size(22.dp)) {
        val k = size.minDimension / 24f
        val st = Stroke(width = 1.8f * k, cap = StrokeCap.Round)
        fun o(x: Float, y: Float) = Offset(x * k, y * k)
        when (t) {
            Tab.LIBRARY -> for ((x, y) in listOf(3.5f to 3.5f, 13.5f to 3.5f, 3.5f to 13.5f, 13.5f to 13.5f))
                drawRoundRect(color, o(x, y), Size(7f * k, 7f * k), androidx.compose.ui.geometry.CornerRadius(2f * k), style = st)
            Tab.ADD -> { drawLine(color, o(12f, 5f), o(12f, 19f), st.width, StrokeCap.Round); drawLine(color, o(5f, 12f), o(19f, 12f), st.width, StrokeCap.Round) }
            Tab.UPDATES -> {
                drawLine(color, o(12f, 4f), o(12f, 15f), st.width, StrokeCap.Round)
                drawLine(color, o(12f, 15f), o(8f, 11f), st.width, StrokeCap.Round)
                drawLine(color, o(12f, 15f), o(16f, 11f), st.width, StrokeCap.Round)
                drawLine(color, o(5f, 20f), o(19f, 20f), st.width, StrokeCap.Round)
            }
            Tab.SETTINGS -> {
                drawLine(color, o(4f, 7f), o(13f, 7f), st.width, StrokeCap.Round); drawLine(color, o(17f, 7f), o(20f, 7f), st.width, StrokeCap.Round)
                drawLine(color, o(4f, 17f), o(7f, 17f), st.width, StrokeCap.Round); drawLine(color, o(11f, 17f), o(20f, 17f), st.width, StrokeCap.Round)
                drawCircle(color, 2f * k, o(15f, 7f), style = st); drawCircle(color, 2f * k, o(9f, 17f), style = st)
            }
        }
    }
}

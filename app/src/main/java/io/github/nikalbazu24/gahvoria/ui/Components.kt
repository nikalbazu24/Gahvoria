package io.github.nikalbazu24.gahvoria.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

/** Text in Montserrat with the theme's ink by default. */
@Composable
fun T(
    text: String, size: TextUnit = 14.sp, weight: FontWeight = FontWeight.Medium, color: Color = LocalColors.current.ink,
    modifier: Modifier = Modifier, align: TextAlign? = null, maxLines: Int = Int.MAX_VALUE, spacing: TextUnit = TextUnit.Unspecified,
) = Text(text, modifier, color = color, fontSize = size, fontWeight = weight, fontFamily = Montserrat, textAlign = align,
    maxLines = maxLines, letterSpacing = spacing, lineHeight = (size.value * 1.35f).sp)

/** Small uppercase label above a group. */
@Composable
fun Label(text: String, color: Color = LocalColors.current.muted) =
    T(text.uppercase(), 11.sp, FontWeight.Bold, color, Modifier.padding(start = 2.dp, top = 6.dp), spacing = 1.8.sp)

/**
 * Clickable with a visible focus ring: on the Thor the D-pad and A button move and press like a finger.
 */
fun Modifier.press(shape: Shape, enabled: Boolean = true, role: Role = Role.Button, onClick: () -> Unit): Modifier =
    this.then(Modifier.focusRingClickable(shape, enabled, role, onClick))

@Composable
private fun Modifier.focusRingClickableImpl(shape: Shape, enabled: Boolean, role: Role, onClick: () -> Unit): Modifier {
    val source = remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    val c = LocalColors.current
    return this
        .border(if (focused) BorderStroke(2.5.dp, c.accent) else BorderStroke(0.dp, Color.Transparent), shape)
        .clip(shape)
        .clickable(interactionSource = source, indication = androidx.compose.foundation.LocalIndication.current,
            enabled = enabled, role = role, onClick = onClick)
}

private fun Modifier.focusRingClickable(shape: Shape, enabled: Boolean, role: Role, onClick: () -> Unit): Modifier =
    androidx.compose.ui.composed { focusRingClickableImpl(shape, enabled, role, onClick) }

/** Translucent card. */
@Composable
fun Glass(modifier: Modifier = Modifier, radius: Dp = 22.dp, content: @Composable ColumnScope.() -> Unit) {
    val c = LocalColors.current
    val shape = RoundedCornerShape(radius)
    Column(modifier.clip(shape).background(c.glass).border(1.dp, c.line, shape), content = content)
}

/** The main action: coral gradient. */
@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, icon: (@Composable () -> Unit)? = null, onClick: () -> Unit) {
    val c = LocalColors.current
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier
            .alpha(if (enabled) 1f else .45f)
            .press(shape, enabled, onClick = onClick)
            .background(Brush.horizontalGradient(listOf(c.accent, c.accent2, c.accent3)), shape)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) { icon(); Spacer(Modifier.width(10.dp)) }
        T(text, 15.sp, FontWeight.ExtraBold, c.onAccent, spacing = .3.sp)
    }
}

@Composable
fun SecondaryButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    val c = LocalColors.current
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier.alpha(if (enabled) 1f else .45f).press(shape, enabled, onClick = onClick)
            .background(c.glass, shape).border(1.dp, c.line, shape).padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) { T(text, 14.sp, FontWeight.Bold) }
}

/** A row inside a card: title, small description and something at the end. */
@Composable
fun RowItem(
    title: String, detail: String? = null, titleColor: Color = LocalColors.current.ink, onClick: (() -> Unit)? = null,
    role: Role = Role.Button, end: @Composable RowScope.() -> Unit = {},
) {
    val c = LocalColors.current
    val base = Modifier.fillMaxWidth()
    Row(
        (if (onClick != null) base.press(RoundedCornerShape(0.dp), role = role, onClick = onClick) else base)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            T(title, 15.sp, FontWeight.SemiBold, titleColor)
            if (detail != null) T(detail, 12.sp, FontWeight.Medium, c.muted)
        }
        Spacer(Modifier.width(12.dp))
        end()
    }
}

@Composable
fun Divider() = Box(Modifier.fillMaxWidth().height(1.dp).background(LocalColors.current.line))

/** Two or three options side by side (mode, tabs). */
@Composable
fun Segmented(options: List<String>, selected: Int, modifier: Modifier = Modifier, onSelect: (Int) -> Unit) {
    val c = LocalColors.current
    val outer = RoundedCornerShape(16.dp)
    Row(modifier.fillMaxWidth().background(c.glass, outer).border(1.dp, c.line, outer).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        options.forEachIndexed { i, o ->
            val on = i == selected
            val shape = RoundedCornerShape(12.dp)
            Box(
                Modifier.weight(1f).press(shape, role = Role.Tab) { onSelect(i) }
                    .background(if (on) c.glassStrong else Color.Transparent, shape).padding(vertical = 11.dp),
                contentAlignment = Alignment.Center,
            ) { T(o, 13.sp, FontWeight.Bold, if (on) c.ink else c.muted) }
        }
    }
}

@Composable
fun Toggle(on: Boolean) {
    val c = LocalColors.current
    Box(
        Modifier.size(46.dp, 28.dp).clip(CircleShape)
            .background(if (on) Brush.horizontalGradient(listOf(c.accent, c.accent2)) else Brush.linearGradient(listOf(c.glassStrong, c.glassStrong)))
            .border(1.dp, if (on) Color.Transparent else c.line, CircleShape).padding(3.dp),
        contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart,
    ) { Box(Modifier.size(22.dp).clip(CircleShape).background(if (on) Color.White else c.faint)) }
}

@Composable
fun Radio(on: Boolean) {
    val c = LocalColors.current
    Box(Modifier.size(20.dp).clip(CircleShape).border(2.dp, if (on) c.accent else c.faint, CircleShape).padding(5.dp)) {
        if (on) Box(Modifier.size(10.dp).clip(CircleShape).background(c.accent))
    }
}

@Composable
fun ProgressBar(value: Float, modifier: Modifier = Modifier) {
    val c = LocalColors.current
    Box(modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(c.glassStrong)) {
        Box(Modifier.fillMaxWidth(value.coerceIn(0f, 1f)).height(8.dp).clip(CircleShape)
            .background(Brush.horizontalGradient(listOf(c.accent, c.accent2, c.accent3))))
    }
}

/** Gahvoria's mark: a cartridge with a bright rhombus, in the palette's colors. */
@Composable
fun Cartridge(size: Dp, modifier: Modifier = Modifier) {
    val c = LocalColors.current
    Canvas(modifier.size(size)) { drawCartridge(c.accent, c.accent2, c.accent3, c.bg) }
}

fun DrawScope.drawCartridge(a1: Color, a2: Color, a3: Color, hole: Color) {
    val k = size.minDimension / 100f
    fun p(x: Float, y: Float) = Offset(x * k, y * k)
    val body = Path().apply {
        moveTo(30f * k, 6f * k); lineTo(70f * k, 6f * k); lineTo(84f * k, 20f * k); lineTo(84f * k, 90f * k)
        lineTo(78f * k, 96f * k); lineTo(22f * k, 96f * k); lineTo(16f * k, 90f * k); lineTo(16f * k, 20f * k); close()
    }
    drawPath(body, Brush.verticalGradient(listOf(a1, a2, a3), startY = 6f * k, endY = 96f * k))
    drawRoundRect(hole, p(25f, 20f), Size(50f * k, 38f * k), androidx.compose.ui.geometry.CornerRadius(5f * k))
    val gem = Path().apply { moveTo(50f * k, 25f * k); lineTo(62f * k, 39f * k); lineTo(50f * k, 53f * k); lineTo(38f * k, 39f * k); close() }
    drawPath(gem, a1)
    val shine = Path().apply { moveTo(50f * k, 25f * k); lineTo(62f * k, 39f * k); lineTo(50f * k, 41f * k); close() }
    drawPath(shine, Color.White.copy(alpha = .55f))
    for (i in 0 until 4) drawRoundRect(hole, p(31f + i * 11f, 74f), Size(4f * k, 16f * k), androidx.compose.ui.geometry.CornerRadius(2f * k))
}

/**
 * An original cover for every game: facets in the palette's colors, different for each title.
 * Never official artwork.
 */
fun DrawScope.drawFacets(seed: Int, base: Color, a: Color, b: Color) {
    val r = Random(seed)
    val cols = 5; val rows = 6
    val w = size.width; val h = size.height
    val pts = Array(rows + 1) { y ->
        Array(cols + 1) { x ->
            val jx = if (x == 0 || x == cols) 0f else (r.nextFloat() - .5f) * w / cols * .55f
            val jy = if (y == 0 || y == rows) 0f else (r.nextFloat() - .5f) * h / rows * .55f
            Offset(x * w / cols + jx, y * h / rows + jy)
        }
    }
    drawRect(base)
    for (y in 0 until rows) for (x in 0 until cols) {
        val p00 = pts[y][x]; val p10 = pts[y][x + 1]; val p01 = pts[y + 1][x]; val p11 = pts[y + 1][x + 1]
        val glow = 1f - y.toFloat() / rows
        for (tri in listOf(listOf(p00, p10, p01), listOf(p10, p11, p01))) {
            val pick = r.nextFloat()
            val col = if (pick < .35f) b else if (pick < .62f) a else base
            val op = ((.18f + r.nextFloat() * .55f) * (.4f + glow * .8f)).coerceAtMost(.95f)
            val path = Path().apply { moveTo(tri[0].x, tri[0].y); lineTo(tri[1].x, tri[1].y); lineTo(tri[2].x, tri[2].y); close() }
            drawPath(path, col.copy(alpha = op))
        }
    }
    drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color(0xC80A0A10)), startY = h * .45f, endY = h))
}

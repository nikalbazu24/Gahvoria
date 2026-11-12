package io.github.nikalbazu24.gahvoria.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.nikalbazu24.gahvoria.core.Game
import io.github.nikalbazu24.gahvoria.core.PadPiece
import io.github.nikalbazu24.gahvoria.core.PadSpot
import io.github.nikalbazu24.gahvoria.core.TouchLayout
import kotlin.math.roundToInt

/** Where each piece sits before the player moves it: top-left corner and size, as fractions of the screen. */
private fun home(p: PadPiece): List<Float> = when (p) {
    PadPiece.DPAD -> listOf(.03f, .52f, .22f, .40f)
    PadPiece.L -> listOf(.03f, .06f, .12f, .12f)
    PadPiece.R -> listOf(.85f, .06f, .12f, .12f)
    PadPiece.ACTIONS -> listOf(.70f, .50f, .27f, .42f)
    PadPiece.BAR -> listOf(.36f, .84f, .28f, .12f)
}

/**
 * A small landscape screen with the five pieces of the touch controls. Drag a piece to move it, pick it to change its
 * size, hide it or read what it does. Every change is saved for this game and used the next time it opens.
 */
@Composable
fun PadEditor(s: AppState, g: Game) {
    val c = LocalColors.current
    var layout by remember(g.path) { mutableStateOf(s.padFor(g)) }
    var picked by remember(g.path) { mutableStateOf(PadPiece.DPAD) }
    fun change(next: TouchLayout) { layout = next; s.savePad(g, next) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        T("Arrastra cada botón donde te acomode. Toca uno para ver para qué sirve y cambiar su tamaño.", 12.sp, FontWeight.Medium, c.muted)
        val shape = RoundedCornerShape(18.dp)
        BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(c.bg, shape).border(1.dp, c.line, shape)) {
            val w = constraints.maxWidth.toFloat()
            val h = constraints.maxHeight.toFloat()
            val density = LocalDensity.current
            for (p in PadPiece.entries) {
                val (hx, hy, hw, hh) = home(p)
                val spot = layout.spot(p)
                val on = p == picked
                val pieceShape = RoundedCornerShape(12.dp)
                Box(
                    Modifier
                        .offset { IntOffset(((hx + spot.x) * w).roundToInt(), ((hy + spot.y) * h).roundToInt()) }
                        .size(with(density) { (hw * w).toDp() }, with(density) { (hh * h).toDp() })
                        .scale(spot.scale)
                        .alpha(if (spot.hidden) .3f else 1f)
                        .background(if (on) c.accent.copy(alpha = .35f) else c.glass, pieceShape)
                        .border(if (on) 2.dp else 1.dp, if (on) c.accent else c.line, pieceShape)
                        .pointerInput(p) {
                            detectDragGestures(onDragStart = { picked = p }, onDragEnd = { s.savePad(g, layout) }, onDragCancel = { s.savePad(g, layout) }) { ev, drag ->
                                ev.consume()
                                val cur = layout.spot(p)
                                layout = layout.with(p, cur.copy(x = cur.x + drag.x / w, y = cur.y + drag.y / h))
                            }
                        }
                        .pointerInput(p) { androidx.compose.foundation.gestures.detectTapGestures { picked = p } },
                    contentAlignment = Alignment.Center,
                ) { T(p.title, 10.sp, FontWeight.Bold, if (on) c.ink else c.muted, maxLines = 2) }
            }
        }
        val spot = layout.spot(picked)
        Glass(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                T(picked.title, 15.sp, FontWeight.ExtraBold)
                T(picked.guide, 13.sp, FontWeight.Medium, c.muted)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    T("Tamaño", 12.sp, FontWeight.Bold, c.muted)
                    Slider(
                        spot.scale, { change(layout.with(picked, spot.copy(scale = it))) }, valueRange = .5f..1.8f,
                        modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
                        colors = SliderDefaults.colors(thumbColor = c.accent, activeTrackColor = c.accent, inactiveTrackColor = c.line),
                    )
                    T("${(spot.scale * 100).roundToInt()}%", 12.sp, FontWeight.Bold)
                }
            }
            Divider()
            RowItem(if (spot.hidden) "Mostrar este botón" else "Ocultar este botón",
                if (spot.hidden) "Vuelve a aparecer en el juego." else "No se verá en el juego. Puedes volver a mostrarlo aquí.",
                onClick = { change(layout.with(picked, spot.copy(hidden = !spot.hidden))) }) {
                T(if (spot.hidden) "Mostrar" else "Ocultar", 12.sp, FontWeight.Bold, c.accent)
            }
            Divider()
            RowItem("Volver a la posición original", "Solo de este botón.",
                onClick = { change(layout.with(picked, PadSpot())) }) { T("Restaurar", 12.sp, FontWeight.Bold, c.accent) }
            Divider()
            RowItem("Restaurar todos los botones", "Deja la cruz, L, R, A B C y la barra como vienen.",
                onClick = { change(TouchLayout()) }) { T("Restaurar", 12.sp, FontWeight.Bold, c.accent) }
        }
    }
}

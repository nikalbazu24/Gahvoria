package io.github.nikalbazu24.gahvoria.ui

import android.text.format.DateUtils
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.nikalbazu24.gahvoria.core.Game
import io.github.nikalbazu24.gahvoria.core.Saves
import io.github.nikalbazu24.gahvoria.core.Support
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.absoluteValue

@Composable
fun LibraryScreen(s: AppState) {
    val c = LocalColors.current
    Column(Modifier.fillMaxSize()) {
        Header(s)
        if (s.games.isEmpty()) { EmptyLibrary(s); return }
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val wide = maxWidth > maxHeight && maxWidth > 600.dp
            val pager = rememberPagerState(initialPage = s.selected.coerceIn(0, s.games.lastIndex)) { s.games.size }
            val scope = rememberCoroutineScope()
            LaunchedEffect(pager) { snapshotFlow { pager.currentPage }.collect { s.selected = it } }
            LaunchedEffect(s.games.size, s.selected) { if (pager.currentPage != s.selected && s.selected < s.games.size) pager.scrollToPage(s.selected) }
            val game = s.games[pager.currentPage.coerceIn(0, s.games.lastIndex)]
            val coverH = if (wide) (maxHeight - 40.dp).coerceAtMost(320.dp) else 300.dp
            val coverW = coverH * .75f
            val carousel = @Composable { mod: Modifier ->
                Column(mod, horizontalAlignment = Alignment.CenterHorizontally) {
                    HorizontalPager(
                        state = pager,
                        contentPadding = PaddingValues(horizontal = ((if (wide) maxWidth / 2 else maxWidth) - coverW) / 2),
                        pageSpacing = 14.dp,
                        modifier = Modifier.fillMaxWidth().height(coverH + 24.dp),
                    ) { page ->
                        val offset = ((pager.currentPage - page) + pager.currentPageOffsetFraction).absoluteValue.coerceAtMost(1f)
                        Cover(s.games[page], page, Modifier.size(coverW, coverH).graphicsLayer {
                            val k = 1f - offset * .12f
                            scaleX = k; scaleY = k; alpha = 1f - offset * .45f
                        }) { scope.launch { if (page == pager.currentPage) s.play(s.games[page]) else pager.animateScrollToPage(page) } }
                    }
                    Spacer(Modifier.height(8.dp))
                    Dots(s.games.size, pager.currentPage)
                }
            }
            if (wide) Row(Modifier.fillMaxSize().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                carousel(Modifier.weight(1f))
                Info(s, game, Modifier.weight(1f).padding(end = 24.dp).verticalScroll(rememberScrollState()))
            } else Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                carousel(Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                Info(s, game, Modifier.padding(horizontal = 18.dp))
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun Header(s: AppState) {
    val c = LocalColors.current
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Cartridge(30.dp)
        Spacer(Modifier.width(10.dp))
        T("Gahvoria", 18.sp, FontWeight.Bold, modifier = Modifier.weight(1f))
        val u = s.update
        if (u is UpdateState.Available || u is UpdateState.Ready || u is UpdateState.Downloading) {
            val name = when (u) { is UpdateState.Available -> u.release.versionName; is UpdateState.Ready -> u.release.versionName
                is UpdateState.Downloading -> u.release.versionName; else -> "" }
            val shape = CircleShape
            Row(Modifier.press(shape) { s.tab = Tab.UPDATES }.background(c.glass, shape).border(1.dp, c.line, shape)
                .padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(c.ok))
                Spacer(Modifier.width(8.dp))
                T("Versión $name lista", 12.sp, FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun Cover(g: Game, index: Int, modifier: Modifier, onClick: () -> Unit) {
    val c = LocalColors.current
    val shape = RoundedCornerShape(26.dp)
    val colors = listOf(c.accent, c.accent2, c.accent3)
    val seed = g.path.hashCode()
    Box(modifier.shadow(18.dp, shape, ambientColor = c.accent, spotColor = c.accent).press(shape, onClick = onClick)
        .border(1.dp, c.line, shape)) {
        val base = if (c.dark) c.bg2 else Color(0xFF22232C)
        Canvas(Modifier.fillMaxSize()) { drawFacets(seed, base, colors[(seed and 0xff) % 3], colors[((seed shr 8) and 0xff) % 3]) }
        T(g.kind.label.uppercase(), 10.sp, FontWeight.Bold, Color(0xFFF2F2F6),
            Modifier.padding(14.dp).clip(CircleShape).background(Color(0x80080810)).padding(horizontal = 10.dp, vertical = 5.dp), spacing = 1.sp)
        T(g.title, 20.sp, FontWeight.ExtraBold, Color(0xFFF2F2F6), Modifier.align(Alignment.BottomStart).padding(16.dp), maxLines = 3)
    }
}

@Composable
private fun Dots(count: Int, current: Int) {
    val c = LocalColors.current
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(count.coerceAtMost(12)) { i ->
            Box(Modifier.size(if (i == current) 20.dp else 6.dp, 6.dp).clip(CircleShape).background(if (i == current) c.accent else c.faint))
        }
    }
}

@Composable
private fun Info(s: AppState, g: Game, modifier: Modifier) {
    val c = LocalColors.current
    val support = g.inspection.support
    val saves = remember(g.path, g.lastPlayed) { if (g.kind.isRpgMaker) Saves.list(s.saveDir(g)) else emptyList<File>() }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        T(g.title, 28.sp, FontWeight.ExtraBold, maxLines = 2)
        Row(verticalAlignment = Alignment.CenterVertically) {
            T(g.inspection.engineLabel, 12.sp, FontWeight.SemiBold, c.muted)
            Sep()
            val (text, color) = when (support) {
                Support.PLAYABLE -> "Compatible" to c.ok
                Support.NEXT_PHASE -> "Próxima fase" to c.warn
                Support.RESEARCH -> "En investigación" to c.warn
                Support.NOT_A_GAME -> "No reconocido" to c.warn
            }
            T(text, 12.sp, FontWeight.SemiBold, color)
            if (g.playMs > 60_000) { Sep(); T(playTime(g.playMs) + " jugadas", 12.sp, FontWeight.SemiBold, c.muted) }
        }
        Glass(Modifier.fillMaxWidth(), radius = 18.dp) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                T("TU PARTIDA", 10.sp, FontWeight.Bold, c.accent, spacing = 1.6.sp)
                Spacer(Modifier.height(3.dp))
                T(progressLine(g, saves.size, support), 14.sp, FontWeight.Medium)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PrimaryButton(if (support == Support.PLAYABLE) "Jugar" else "Aún no disponible", Modifier.weight(1f),
                enabled = support == Support.PLAYABLE,
                icon = { Canvas(Modifier.size(14.dp)) {
                    val p = androidx.compose.ui.graphics.Path().apply { moveTo(0f, 0f); lineTo(size.width, size.height / 2); lineTo(0f, size.height); close() }
                    drawPath(p, c.onAccent)
                } }) { s.play(g) }
            val shape = RoundedCornerShape(18.dp)
            Box(Modifier.size(56.dp).press(shape) { s.optionsFor = g }.background(c.glass, shape).border(1.dp, c.line, shape),
                contentAlignment = Alignment.Center) { T("•••", 14.sp, FontWeight.Bold) }
        }
    }
}

@Composable
private fun Sep() {
    Box(Modifier.padding(horizontal = 8.dp).size(3.dp).clip(CircleShape).background(LocalColors.current.faint))
}

private fun progressLine(g: Game, saves: Int, support: Support): String = when {
    support == Support.NEXT_PHASE -> "Godot llega en la fase 2b del plan."
    support == Support.RESEARCH -> "Unity se evalúa en la fase 4; aún no se puede jugar."
    support == Support.NOT_A_GAME -> "No reconocí el motor de esta carpeta."
    g.lastPlayed == 0L && saves == 0 -> "Empieza una nueva aventura."
    else -> buildString {
        if (g.lastPlayed > 0) append("Última vez " + DateUtils.getRelativeTimeSpanString(g.lastPlayed, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS))
        if (saves > 0) { if (isNotEmpty()) append(" · "); append(if (saves == 1) "1 partida guardada" else "$saves partidas guardadas") }
    }
}

fun playTime(ms: Long): String {
    val min = ms / 60_000
    return if (min < 60) "$min min" else "${min / 60} h ${min % 60} min"
}

@Composable
private fun EmptyLibrary(s: AppState) {
    val c = LocalColors.current
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Cartridge(72.dp)
        Spacer(Modifier.height(20.dp))
        T("Tu biblioteca está vacía", 22.sp, FontWeight.ExtraBold)
        Spacer(Modifier.height(8.dp))
        T("Añade la carpeta de un juego de RPG Maker o Pokémon Essentials que tengas en el equipo.", 14.sp, FontWeight.Medium, c.muted,
            align = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        PrimaryButton("Añadir juego", Modifier.fillMaxWidth(.8f)) { s.tab = Tab.ADD }
    }
}

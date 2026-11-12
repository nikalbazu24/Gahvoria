package io.github.nikalbazu24.gahvoria.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.nikalbazu24.gahvoria.core.Slide

/** The introduction (first time) or the changes of a big update: a few screens, with "Omitir" always visible. */
@Composable
fun IntroScreen(slides: List<Slide>, update: Boolean, onDone: () -> Unit) {
    val c = LocalColors.current
    var i by remember(slides) { mutableIntStateOf(0) }
    BackHandler { if (i > 0) i-- else onDone() }
    val shape = RoundedCornerShape(28.dp)
    Box(
        Modifier.fillMaxSize().background(c.bg.copy(alpha = .94f)).pointerInput(Unit) { detectTapGestures { } }.padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.widthIn(max = 520.dp).fillMaxWidth().background(c.bg2, shape).border(1.dp, c.line, shape)
                .verticalScroll(rememberScrollState()).padding(26.dp),
        ) {
            Box(Modifier.fillMaxWidth().height(96.dp).clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(c.accent, c.accent2, c.accent3)))) {
                Cartridge(44.dp, Modifier.align(Alignment.CenterStart).padding(start = 20.dp))
            }
            Spacer(Modifier.height(20.dp))
            if (update) T("NOVEDADES", 11.sp, FontWeight.Bold, c.accent, spacing = 1.5.sp)
            AnimatedContent(i, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "intro") { k ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    T(slides[k].title, 22.sp, FontWeight.ExtraBold)
                    T(slides[k].text, 14.sp, FontWeight.Medium, c.muted)
                }
            }
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SecondaryButton("Omitir", Modifier.widthIn(min = 96.dp)) { onDone() }
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)) {
                    slides.indices.forEach { k ->
                        Box(Modifier.size(if (k == i) 20.dp else 6.dp, 6.dp).clip(CircleShape).background(if (k == i) c.accent else c.faint))
                    }
                }
                PrimaryButton(if (i == slides.lastIndex) "Empezar" else "Siguiente", Modifier.widthIn(min = 120.dp)) {
                    if (i == slides.lastIndex) onDone() else i++
                }
            }
        }
    }
}

package io.github.nikalbazu24.gahvoria.ui

import android.provider.Settings as AndroidSettings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * The welcome animation when Gahvoria opens after being closed (approved sketch v11): the cartridge rises with a
 * coral glow and the name appears. 1.2 s, a tap skips it, and with Android's animations off only the logo shows.
 */
@Composable
fun Splash(onDone: () -> Unit) {
    val context = LocalContext.current
    val reduced = remember {
        runCatching { AndroidSettings.Global.getFloat(context.contentResolver, AndroidSettings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }
            .getOrDefault(false)
    }
    // Full screen: the status and navigation bars hide while it plays and come back after.
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (context as? android.app.Activity)?.window
        val bars = window?.let { WindowCompat.getInsetsController(it, view) }
        bars?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        bars?.hide(WindowInsetsCompat.Type.systemBars())
        onDispose { bars?.show(WindowInsetsCompat.Type.systemBars()) }
    }
    val rise = remember { Animatable(if (reduced) 1f else 0f) }
    val word = remember { Animatable(if (reduced) 1f else 0f) }
    val glow = remember { Animatable(if (reduced) 1f else 0f) }
    val out = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        if (!reduced) {
            launch { glow.animateTo(1f, tween(600)) }
            launch { rise.animateTo(1f, tween(550, easing = CubicBezierEasing(.2f, 1.4f, .4f, 1f))) }
            launch { kotlinx.coroutines.delay(600); word.animateTo(1f, tween(350)) }
            kotlinx.coroutines.delay(1200)
        } else kotlinx.coroutines.delay(300)
        out.animateTo(0f, tween(if (reduced) 0 else 300))
        onDone()
    }
    Box(
        Modifier.fillMaxSize().graphicsLayer { alpha = out.value }.background(Color(0xFF0D0E12))
            .pointerInput(Unit) { detectTapGestures { onDone() } },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.fillMaxSize().graphicsLayer { alpha = glow.value }
            .background(Brush.radialGradient(listOf(Color(0x59E8664F), Color.Transparent), radius = 900f)))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Cartridge(72.dp, Modifier.graphicsLayer {
                val k = .6f + .4f * rise.value
                scaleX = k; scaleY = k; alpha = rise.value.coerceIn(0f, 1f)
                translationY = (1f - rise.value) * 24.dp.toPx()
            })
            Spacer(Modifier.height(18.dp))
            T("Gahvoria", 26.sp, FontWeight.ExtraBold, Color(0xFFF2F2F6),
                Modifier.graphicsLayer { alpha = word.value; translationY = (1f - word.value) * 8.dp.toPx() },
                spacing = (0.2f - 0.16f * word.value).let { (it * 26).sp })
        }
    }
}

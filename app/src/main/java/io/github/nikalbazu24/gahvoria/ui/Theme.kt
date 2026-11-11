package io.github.nikalbazu24.gahvoria.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import io.github.nikalbazu24.gahvoria.R
import io.github.nikalbazu24.gahvoria.core.Scheme

/** The colors in use, from the chosen palette and mode. */
@Immutable
data class Colors(
    val bg: Color, val bg2: Color, val ink: Color, val muted: Color, val faint: Color,
    val accent: Color, val accent2: Color, val accent3: Color, val onAccent: Color,
    val ok: Color, val warn: Color,
    /** Glass surfaces: translucent cards and bars over the background. */
    val glass: Color, val glassStrong: Color, val line: Color,
    val dark: Boolean,
) {
    companion object {
        fun from(s: Scheme, dark: Boolean): Colors {
            fun c(v: Long) = Color(v.toInt())
            val ink = c(s.ink); val muted = c(s.muted)
            return Colors(
                bg = c(s.bg), bg2 = c(s.bg2), ink = ink, muted = muted, faint = muted.copy(alpha = .72f),
                accent = c(s.accent), accent2 = c(s.accent2), accent3 = c(s.accent3), onAccent = c(s.onAccent),
                ok = c(s.ok), warn = c(s.warn),
                glass = if (dark) Color.White.copy(alpha = .06f) else Color.White.copy(alpha = .74f),
                glassStrong = if (dark) Color.White.copy(alpha = .11f) else Color.White.copy(alpha = .96f),
                line = if (dark) Color.White.copy(alpha = .10f) else Color(0x1A1E1E3C),
                dark = dark,
            )
        }
    }
}

val LocalColors = staticCompositionLocalOf { Colors.from(io.github.nikalbazu24.gahvoria.core.Palettes.all.first().dark, true) }

val Montserrat = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold),
    Font(R.font.montserrat_extrabold, FontWeight.ExtraBold),
)

@Composable
fun GahvoriaTheme(colors: Colors, content: @Composable () -> Unit) {
    val scheme = if (colors.dark) darkColorScheme(
        primary = colors.accent, onPrimary = colors.onAccent, background = colors.bg, onBackground = colors.ink,
        surface = colors.bg2, onSurface = colors.ink,
    ) else lightColorScheme(
        primary = colors.accent, onPrimary = colors.onAccent, background = colors.bg, onBackground = colors.ink,
        surface = colors.bg2, onSurface = colors.ink,
    )
    val base = TextStyle(fontFamily = Montserrat, color = colors.ink)
    val type = MaterialTheme.typography.let { t ->
        t.copy(
            bodyLarge = t.bodyLarge.merge(base), bodyMedium = t.bodyMedium.merge(base), bodySmall = t.bodySmall.merge(base),
            titleLarge = t.titleLarge.merge(base), titleMedium = t.titleMedium.merge(base), labelLarge = t.labelLarge.merge(base),
        )
    }
    CompositionLocalProvider(LocalColors provides colors) {
        MaterialTheme(colorScheme = scheme, typography = type, content = content)
    }
}

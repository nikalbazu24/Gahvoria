package io.github.nikalbazu24.gahvoria.core

import java.util.Locale

/** The five pieces of the on-screen controls that can be moved, resized or hidden. */
enum class PadPiece(val id: String, val title: String, val guide: String) {
    DPAD("dpad", "Cruz", "Mueve al personaje y el cursor en los menús."),
    L("l", "L", "Página anterior en menús y cajas del juego."),
    R("r", "R", "Página siguiente en menús y cajas del juego."),
    ACTIONS("act", "A B C · X Y Z", "A acepta y habla. B cancela y abre el menú. X, Y y Z son extras que algunos juegos usan (X corre en muchos)."),
    BAR("bar", "F1 · Menú · Shift", "F1 abre la pantalla de controles del juego. Menú abre el menú. Shift corre en muchos juegos."),
}

/** Where one piece sits: offsets as a fraction of the screen's width and height, a scale, and whether it is hidden. */
data class PadSpot(val x: Float = 0f, val y: Float = 0f, val scale: Float = 1f, val hidden: Boolean = false) {
    val isDefault get() = x == 0f && y == 0f && scale == 1f && !hidden
}

/**
 * The player's arrangement of the touch controls for one game. Stored as a short text ("dpad,0.05,-0.02,1.2,0;...")
 * that goes to the game screen in the intent; unknown or broken parts are ignored, so a bad value is just the default.
 */
data class TouchLayout(val spots: Map<PadPiece, PadSpot> = emptyMap()) {

    fun spot(p: PadPiece) = spots[p] ?: PadSpot()

    fun with(p: PadPiece, s: PadSpot): TouchLayout {
        val c = PadSpot(s.x.coerceIn(-0.9f, 0.9f), s.y.coerceIn(-0.9f, 0.9f), s.scale.coerceIn(0.5f, 1.8f), s.hidden)
        return TouchLayout(if (c.isDefault) spots - p else spots + (p to c))
    }

    val isDefault get() = spots.isEmpty()

    fun encode(): String = spots.entries.sortedBy { it.key.ordinal }.joinToString(";") { (p, s) ->
        String.format(Locale.ROOT, "%s,%.4f,%.4f,%.3f,%d", p.id, s.x, s.y, s.scale, if (s.hidden) 1 else 0)
    }

    companion object {
        fun decode(text: String?): TouchLayout {
            if (text.isNullOrBlank()) return TouchLayout()
            var l = TouchLayout()
            for (part in text.split(';')) {
                val f = part.split(',')
                if (f.size < 5) continue
                val p = PadPiece.entries.firstOrNull { it.id == f[0] } ?: continue
                val x = f[1].toFloatOrNull() ?: continue
                val y = f[2].toFloatOrNull() ?: continue
                val s = f[3].toFloatOrNull() ?: continue
                if (x.isNaN() || y.isNaN() || s.isNaN()) continue
                l = l.with(p, PadSpot(x, y, s, f[4] == "1"))
            }
            return l
        }
    }
}

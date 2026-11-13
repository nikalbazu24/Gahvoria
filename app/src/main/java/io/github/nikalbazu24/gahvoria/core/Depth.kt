package io.github.nikalbazu24.gahvoria.core

/**
 * "2D / 2.5D / 3D" (experimental): the final picture leans back with perspective. Only the angle is kept
 * (0 to [MAX] degrees); the mode names are just ways to pick it.
 */
object Depth {
    const val MAX = 60
    const val DEFAULT_25D = 28
    const val DEFAULT_3D = 45
    /** Above this angle the effect is called 3D. */
    private const val LIMIT_25D = 38

    val names = listOf("2D", "2.5D", "3D")

    fun clamp(angle: Int) = angle.coerceIn(0, MAX)

    /** 0 = 2D, 1 = 2.5D, 2 = 3D. */
    fun modeOf(angle: Int) = when { angle <= 0 -> 0; angle <= LIMIT_25D -> 1; else -> 2 }

    /** The angle a mode starts with. */
    fun angleOf(mode: Int) = when (mode) { 1 -> DEFAULT_25D; 2 -> DEFAULT_3D; else -> 0 }
}

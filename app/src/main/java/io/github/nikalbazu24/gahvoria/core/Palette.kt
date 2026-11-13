package io.github.nikalbazu24.gahvoria.core

/** Colors as 0xAARRGGBB. Grafito y coral is the main palette; the rest are chosen in Ajustes. */
data class Scheme(
    val bg: Long, val bg2: Long, val ink: Long, val muted: Long,
    val accent: Long, val accent2: Long, val accent3: Long, val onAccent: Long,
    val ok: Long, val warn: Long,
)

data class Palette(val id: String, val name: String, val dark: Scheme, val light: Scheme)

enum class ThemeMode { SYSTEM, DARK, LIGHT }

object Palettes {
    val all = listOf(
        Palette("grafito", "Grafito y coral",
            Scheme(0xFF0D0E12, 0xFF1C1D26, 0xFFF2F2F6, 0xFF9C9DB0, 0xFFFF7A6B, 0xFFFFB38A, 0xFFA9B4FF, 0xFF1A0B09, 0xFF7ED6A8, 0xFFFFB38A),
            Scheme(0xFFF5F5F8, 0xFFE6E6EE, 0xFF1A1B22, 0xFF62637A, 0xFFC23A26, 0xFFE8754F, 0xFF5A65D8, 0xFFFFFFFF, 0xFF1F7A54, 0xFFB04E1C)),
        Palette("medianoche", "Medianoche y ámbar",
            Scheme(0xFF080B17, 0xFF131A33, 0xFFF5F0E6, 0xFF9FA4BC, 0xFFFFC15E, 0xFFFF9B5E, 0xFFF4D9A8, 0xFF1A1206, 0xFF8FD9A0, 0xFFFF9B5E),
            Scheme(0xFFFBF6EC, 0xFFF1E6D0, 0xFF1C1A24, 0xFF67627A, 0xFF8F5600, 0xFFD8631E, 0xFF9B6B1E, 0xFFFFFFFF, 0xFF2A7A44, 0xFFA84A17)),
        Palette("esmeralda", "Esmeralda",
            Scheme(0xFF050F0C, 0xFF0D2119, 0xFFEAF5EE, 0xFF8FB0A2, 0xFF3FE0A0, 0xFFA7F3D0, 0xFFE6D6A0, 0xFF04140E, 0xFF3FE0A0, 0xFFE6D6A0),
            Scheme(0xFFF0F8F3, 0xFFDDEEE4, 0xFF11251C, 0xFF55705F, 0xFF0B7550, 0xFF1FB4A0, 0xFF1F8FB5, 0xFFFFFFFF, 0xFF0B7550, 0xFF85680F)),
        Palette("vino", "Vino y marfil",
            Scheme(0xFF12070B, 0xFF2A0F19, 0xFFF8EFE6, 0xFFBA9CA2, 0xFFF5C2A8, 0xFFEE8B7F, 0xFFF8E6CC, 0xFF1E0A10, 0xFF9ED3A8, 0xFFEE8B7F),
            Scheme(0xFFFBF1EC, 0xFFF2DDD6, 0xFF2A0F19, 0xFF765760, 0xFFA8402F, 0xFFC96A5C, 0xFF8A5A3A, 0xFFFFFFFF, 0xFF33774A, 0xFFA8402F)),
        Palette("violeta", "Violeta",
            Scheme(0xFF0A0717, 0xFF160E33, 0xFFF1EDFC, 0xFF9A91BC, 0xFF6FE6F5, 0xFFB79CFF, 0xFFFF6FBE, 0xFF0A0717, 0xFF6FE6F5, 0xFFFF6FBE),
            Scheme(0xFFF4F1FB, 0xFFE9E2FA, 0xFF1D1633, 0xFF645C8A, 0xFF0A6F81, 0xFF6A48E0, 0xFFD2438E, 0xFFFFFFFF, 0xFF0A6F81, 0xFFB8336F)),
        Palette("oceano", "Océano",
            Scheme(0xFF06111A, 0xFF0E2536, 0xFFE8F4FB, 0xFF8FB2C6, 0xFF2FB7FF, 0xFF6EE7E0, 0xFFB4C8FF, 0xFF041018, 0xFF6EE7B0, 0xFFFFC27A),
            Scheme(0xFFF0F7FB, 0xFFDCEBF4, 0xFF0E2536, 0xFF4C6A7D, 0xFF0A6FA8, 0xFF0E8C86, 0xFF4A5FC0, 0xFFFFFFFF, 0xFF0F7A52, 0xFF9A5A10)),
        Palette("atardecer", "Atardecer",
            Scheme(0xFF170A14, 0xFF321527, 0xFFFDEEE8, 0xFFC2A0AA, 0xFFFF8A5C, 0xFFFF5F8F, 0xFFFFD08A, 0xFF240A10, 0xFF9ED8A8, 0xFFFFD08A),
            Scheme(0xFFFFF3EE, 0xFFFAE0D6, 0xFF2E1020, 0xFF7A5566, 0xFFB8431A, 0xFFC4285F, 0xFF8A5A10, 0xFFFFFFFF, 0xFF2A7A44, 0xFFA84A17)),
        Palette("menta", "Menta y crema",
            Scheme(0xFF0A1411, 0xFF162A23, 0xFFF2F8F1, 0xFF9AB8AA, 0xFF9BE7C4, 0xFFD7F5A8, 0xFFF3E2B0, 0xFF08140F, 0xFF9BE7C4, 0xFFF3E2B0),
            Scheme(0xFFF4F9F2, 0xFFE3EFDF, 0xFF14261D, 0xFF52695C, 0xFF1F7A58, 0xFF6A8F1F, 0xFF8A6A1E, 0xFFFFFFFF, 0xFF1F7A54, 0xFF85680F)),
        Palette("sakura", "Sakura",
            Scheme(0xFF160C14, 0xFF2C1727, 0xFFFCECF3, 0xFFC3A0B4, 0xFFFF9FC7, 0xFFFFC6A8, 0xFFD6B3FF, 0xFF240A1A, 0xFFA8E0B8, 0xFFFFC6A8),
            Scheme(0xFFFFF1F6, 0xFFFADCE8, 0xFF2C1727, 0xFF7A5568, 0xFFB02A6A, 0xFFC4573A, 0xFF7A48C0, 0xFFFFFFFF, 0xFF2A7A4C, 0xFFA84A17)),
        Palette("carbon", "Carbón y oro",
            Scheme(0xFF0B0B0B, 0xFF1B1A17, 0xFFF4F1E8, 0xFFA8A396, 0xFFE5B94C, 0xFFF0D98A, 0xFFC8C8C8, 0xFF1A1405, 0xFFA8D88A, 0xFFF0D98A),
            Scheme(0xFFF7F4EA, 0xFFE9E4D2, 0xFF1B1A17, 0xFF66624F, 0xFF8A6200, 0xFFA8791A, 0xFF5A5A5A, 0xFFFFFFFF, 0xFF3A7A2A, 0xFF8A5A10)),
        Palette("hielo", "Hielo",
            Scheme(0xFF0A1118, 0xFF14212C, 0xFFEEF6FB, 0xFF9BB0BF, 0xFF9FD8FF, 0xFFC9E7FF, 0xFFFFD1E8, 0xFF08121A, 0xFF9FE0C0, 0xFFFFD1A8),
            Scheme(0xFFF2F8FC, 0xFFE0EDF5, 0xFF12212C, 0xFF526878, 0xFF1A6A9E, 0xFF3A7FA8, 0xFFA83A7A, 0xFFFFFFFF, 0xFF1F7A5A, 0xFF9A5A10)),
    )

    const val DEFAULT = "grafito"

    fun byId(id: String?) = all.firstOrNull { it.id == id } ?: all.first()

    fun scheme(id: String?, mode: ThemeMode, systemDark: Boolean): Scheme {
        val p = byId(id)
        val dark = mode == ThemeMode.DARK || (mode == ThemeMode.SYSTEM && systemDark)
        return if (dark) p.dark else p.light
    }

    private fun channel(c: Long, shift: Int): Double {
        val v = ((c shr shift) and 0xFF) / 255.0
        return if (v <= 0.04045) v / 12.92 else Math.pow((v + 0.055) / 1.055, 2.4)
    }

    fun luminance(c: Long) = 0.2126 * channel(c, 16) + 0.7152 * channel(c, 8) + 0.0722 * channel(c, 0)

    /** WCAG contrast ratio. */
    fun contrast(a: Long, b: Long): Double {
        val la = luminance(a); val lb = luminance(b)
        return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
    }
}

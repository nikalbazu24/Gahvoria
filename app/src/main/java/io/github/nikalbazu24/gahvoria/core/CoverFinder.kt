package io.github.nikalbazu24.gahvoria.core

import java.io.File

/**
 * Looks for a cover inside a game's own folder (read only, nothing is written there). Games made with RPG Maker keep
 * their title screens in Graphics/Titles; some creators add a "cover" or "icon" file next to the game.
 */
object CoverFinder {

    private val EXTS = listOf("png", "jpg", "jpeg", "webp", "bmp", "gif")

    /** Best image for the game, or null. Order: a cover made for launchers, title screens, system title, pictures, icon. */
    fun find(game: File): File? {
        val spots = listOf(
            Spot(game, "essentia-cover", false), Spot(game, "gahvoria-cover", false), Spot(game, "cover", false),
            Spot(File(game, "Graphics/Titles"), "title", true), Spot(File(game, "Graphics/Titles"), null, true),
            Spot(File(game, "Graphics/System"), "title", false), Spot(File(game, "Graphics/Pictures"), "title", false),
            Spot(game, "folder", false), Spot(game, "icon", false), Spot(game, "game", false),
        )
        for (s in spots) s.pick()?.let { return it }
        return null
    }

    /** [name] null = any image in the folder; [prefix] true = the name only has to start with it. */
    private class Spot(val dir: File, val name: String?, val prefix: Boolean) {
        fun pick(): File? {
            val files = dir.listFiles()?.filter { it.isFile && it.extension.lowercase() in EXTS && it.length() in 2_000..30_000_000 } ?: return null
            val sorted = files.sortedWith(compareBy({ EXTS.indexOf(it.extension.lowercase()) }, { it.name.lowercase() }))
            if (name == null) return sorted.firstOrNull()
            return sorted.firstOrNull {
                val n = it.nameWithoutExtension.lowercase()
                n == name || (prefix && n.startsWith(name))
            }
        }
    }
}

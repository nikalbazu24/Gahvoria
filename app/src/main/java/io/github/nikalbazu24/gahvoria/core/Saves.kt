package io.github.nikalbazu24.gahvoria.core

import java.io.File
import java.util.Locale

/**
 * Where a game's saves live. Same rule as Essentia, so both apps (and JoiPlay) see the same saves:
 * the game folder itself, or Essentia/Partidas/<title> when the game folder can't be written.
 */
object Saves {

    fun dirFor(gameDir: File, title: String, storageRoot: File, gameFolderWritable: Boolean): File =
        if (gameFolderWritable) gameDir else File(File(storageRoot, "Essentia/Partidas"), safeName(title))

    fun safeName(s: String): String = s.replace(Regex("[\\\\/:*?\"<>|\\p{Cntrl}]"), "").trim().ifEmpty { "Juego" }

    /** RPG Maker save files (Save1.rxdata, Game.rvdata2...), not the game's own data. */
    fun looksLikeSave(name: String): Boolean {
        val n = name.lowercase(Locale.ROOT)
        if (!(n.endsWith(".rxdata") || n.endsWith(".rvdata") || n.endsWith(".rvdata2") || n.endsWith(".sav"))) return false
        val data = listOf("scripts", "map", "tilesets", "system.", "actors", "classes", "skills", "items", "weapons",
            "armors", "enemies", "troops", "states", "animations", "commonevents", "pluginscripts", "pkmnanimations")
        return data.none { n.startsWith(it) }
    }

    fun list(dir: File): List<File> =
        dir.listFiles()?.filter { it.isFile && looksLikeSave(it.name) }?.sortedByDescending { it.lastModified() }.orEmpty()
}

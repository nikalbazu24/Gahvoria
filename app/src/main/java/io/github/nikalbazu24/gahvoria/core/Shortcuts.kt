package io.github.nikalbazu24.gahvoria.core

import java.io.File

/**
 * Shortcut files for other launchers (Díptico, Daijishō, ES-DE, Cocoon, iiSU...). Each one is a tiny text file named
 * after the game, whose first line is the game's folder. Launchers list them like ROMs and open them with Gahvoria.
 * Writing them never touches the games themselves.
 */
object Shortcuts {
    const val EXT = ".gahvoria"

    /** Under the shared storage: the folder is named after the app so launchers can pick it as "the emulator's". */
    const val FOLDER = "ROMs/Gahvoria"

    fun fileName(title: String) = Saves.safeName(title) + EXT

    fun render(game: Game): String =
        game.path + "\n# Gahvoria: acceso a un juego. La primera línea es la carpeta del juego.\n"

    /** The game folder a shortcut points to: its first line that is not empty or a comment. */
    fun pathIn(text: String): String? =
        text.lineSequence().map { it.trim().removePrefix("﻿").trim() }.firstOrNull { it.isNotEmpty() && !it.startsWith("#") }

    /** One shortcut per game that Gahvoria can play. Returns how many were written. The same title twice gets " (2)". */
    fun export(games: List<Game>, folder: File): Int {
        if (!folder.isDirectory && !folder.mkdirs()) return 0
        val used = HashSet<String>()
        var written = 0
        for (g in games) {
            if (!g.kind.isRpgMaker) continue
            var name = fileName(g.title)
            var k = 2
            while (!used.add(name.lowercase())) name = Saves.safeName(g.title) + " (" + k++ + ")" + EXT
            if (runCatching { File(folder, name).writeText(render(g), Charsets.UTF_8) }.isSuccess) written++
        }
        return written
    }

    /** The game folder for what a launcher sent: the folder itself, a shortcut file, or any file inside the game. */
    fun resolve(target: File): File? {
        val dir = when {
            target.isDirectory -> target
            target.name.endsWith(EXT, ignoreCase = true) ->
                runCatching { pathIn(target.readText(Charsets.UTF_8)) }.getOrNull()?.let(::File)
            else -> target.parentFile
        }
        return dir?.takeIf { it.isDirectory }
    }
}

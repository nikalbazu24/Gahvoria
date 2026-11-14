package io.github.nikalbazu24.gahvoria.data

import android.content.Context
import io.github.nikalbazu24.gahvoria.core.Game
import io.github.nikalbazu24.gahvoria.core.Saves
import java.io.File

/** Where each game's translations are kept (inside Gahvoria's own files, so they survive restarts and never touch the game). */
object Translations {
    private const val CATALOG_FORMAT = "opening-v7"

    private fun root(c: Context) = File(c.filesDir, "translations")

    fun dirFor(c: Context, game: Game): File =
        File(root(c), Saves.safeName(game.title).take(40) + "-" + Integer.toHexString(game.path.hashCode()))

    fun catalog(c: Context, game: Game) = File(dirFor(c, game), "catalog")

    /** Cheap enough to run off the UI thread before choosing whether a rescan is needed. */
    fun catalogRevision(game: Game): String {
        val root = File(game.path)
        val data = root.listFiles()?.firstOrNull { it.isDirectory && it.name.equals("Data", true) }
        val scripts = listOfNotNull(root, data).flatMap { folder ->
            folder.listFiles().orEmpty().filter { it.isDirectory && it.name.equals("Scripts", true) }
        }
        val files = data?.listFiles().orEmpty().asSequence() +
            scripts.asSequence().flatMap { it.walkTopDown() } +
            root.listFiles().orEmpty().asSequence().filter { it.name.startsWith("Game.", true) }
        val revision = java.security.MessageDigest.getInstance("SHA-256")
        revision.update("$CATALOG_FORMAT\n".toByteArray(Charsets.UTF_8))
        for (file in files.filter { it.isFile }.sortedBy { it.path }) {
            revision.update("${file.path}\t${file.length()}\t${file.lastModified()}\n".toByteArray(Charsets.UTF_8))
        }
        return "$CATALOG_FORMAT:" + android.util.Base64.encodeToString(revision.digest(), android.util.Base64.NO_WRAP)
    }

    /** Phrases and bytes saved for one game (all languages). */
    fun sizeOf(c: Context, game: Game): Pair<Int, Long> = measure(dirFor(c, game))

    fun total(c: Context): Pair<Int, Long> = measure(root(c))

    private fun measure(dir: File): Pair<Int, Long> {
        var lines = 0
        var bytes = 0L
        dir.walkTopDown().filter { it.isFile && it.name.endsWith(".tsv") }.forEach { f ->
            bytes += f.length()
            lines += runCatching { f.useLines { it.count() } }.getOrDefault(0)
        }
        return lines to bytes
    }

    /** Forgets every saved translation (all games). Games and saves are not touched. */
    fun clearAll(c: Context) { root(c).deleteRecursively() }

    fun clear(c: Context, game: Game) { dirFor(c, game).deleteRecursively() }

    /** The translator's last message ("Traducidas 120 frases", an error...) and when, or null. */
    fun status(c: Context): Triple<Int, String, Long>? = runCatching {
        val l = File(c.filesDir, "translate_status.txt").readLines()
        Triple(l[0].toInt(), l[1], l[2].toLong())
    }.getOrNull()

    fun humanSize(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> String.format(java.util.Locale.ROOT, "%.1f MB", bytes / 1048576.0)
    }
}

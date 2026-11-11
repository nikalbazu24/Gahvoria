package io.github.nikalbazu24.gahvoria.core

import java.io.File

/** One game in the library. Only Gahvoria's own list: the game's files are never touched. */
data class Game(
    val path: String,
    val title: String,
    val kind: EngineKind,
    val essentials: Boolean,
    /** "auto", "modern" or "classic" (Ruby 1.8). */
    val engine: String = ENGINE_AUTO,
    val touch: Boolean = true,
    val addedAt: Long = 0,
    val lastPlayed: Long = 0,
    val playMs: Long = 0,
) {
    val inspection get() = Inspection(kind, essentials, title)

    companion object {
        const val ENGINE_AUTO = "auto"
        const val ENGINE_MODERN = "modern"
        const val ENGINE_CLASSIC = "classic"
    }
}

/**
 * The library file: one line per game, tab-separated, written atomically (temporary file + rename)
 * so a crash never leaves a half-written list.
 */
class Library(private val file: File) {

    fun load(): List<Game> = runCatching {
        if (!file.isFile) return emptyList()
        file.readLines(Charsets.UTF_8).mapNotNull(::parse)
    }.getOrDefault(emptyList())

    fun save(games: List<Game>) {
        file.parentFile?.mkdirs()
        val temp = File(file.parentFile, file.name + ".tmp")
        temp.writeText(games.joinToString("") { format(it) + "\n" }, Charsets.UTF_8)
        if (!temp.renameTo(file)) {
            file.delete()
            check(temp.renameTo(file)) { "No se pudo guardar la biblioteca" }
        }
    }

    /** Adds the game, or refreshes it if its folder was already in the library. */
    fun add(game: Game): List<Game> {
        val games = load().filterNot { it.path == game.path } + game
        save(games)
        return games
    }

    fun update(path: String, change: (Game) -> Game): List<Game> {
        val games = load().map { if (it.path == path) change(it) else it }
        save(games)
        return games
    }

    /** Removes the game from the list only. Its folder and saves stay where they are. */
    fun remove(path: String): List<Game> {
        val games = load().filterNot { it.path == path }
        save(games)
        return games
    }

    companion object {
        private const val VERSION = "g1"

        /** Most recently played first, then most recently added. */
        fun ordered(games: List<Game>) = games.sortedWith(
            compareByDescending<Game> { it.lastPlayed }.thenByDescending { it.addedAt }.thenBy { it.title.lowercase() })

        internal fun format(g: Game) = listOf(
            VERSION, g.path, g.title, g.kind.name, if (g.essentials) "1" else "0", g.engine,
            if (g.touch) "1" else "0", g.addedAt.toString(), g.lastPlayed.toString(), g.playMs.toString(),
        ).joinToString("\t") { escape(it) }

        internal fun parse(line: String): Game? {
            val f = line.split('\t').map(::unescape)
            if (f.size < 10 || f[0] != VERSION) return null
            return runCatching {
                Game(
                    path = f[1], title = f[2],
                    kind = runCatching { EngineKind.valueOf(f[3]) }.getOrDefault(EngineKind.UNKNOWN),
                    essentials = f[4] == "1", engine = f[5], touch = f[6] == "1",
                    addedAt = f[7].toLong(), lastPlayed = f[8].toLong(), playMs = f[9].toLong(),
                )
            }.getOrNull()
        }

        private fun escape(s: String) = s.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n")

        private fun unescape(s: String): String {
            val out = StringBuilder()
            var i = 0
            while (i < s.length) {
                val c = s[i]
                if (c == '\\' && i + 1 < s.length) {
                    when (s[i + 1]) {
                        't' -> out.append('\t'); 'n' -> out.append('\n'); else -> out.append(s[i + 1])
                    }
                    i += 2
                } else { out.append(c); i++ }
            }
            return out.toString()
        }
    }
}

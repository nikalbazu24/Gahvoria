package io.github.nikalbazu24.gahvoria.core

import java.io.File
import java.io.InputStream
import java.util.Locale
import java.util.zip.ZipInputStream

/** The kinds of compressed file Gahvoria can install a game from, told apart by the first bytes (not by the name). */
enum class ArchiveKind(val label: String) {
    ZIP(".zip"), SEVEN_Z(".7z"), RAR(".rar"), UNKNOWN("");

    companion object {
        fun detect(head: ByteArray): ArchiveKind = when {
            head.size >= 4 && head[0] == 'P'.code.toByte() && head[1] == 'K'.code.toByte() && (head[2].toInt() == 3 || head[2].toInt() == 5) -> ZIP
            head.size >= 6 && head[0] == '7'.code.toByte() && head[1] == 'z'.code.toByte() && head[2] == 0xBC.toByte() && head[3] == 0xAF.toByte() -> SEVEN_Z
            head.size >= 6 && head[0] == 'R'.code.toByte() && head[1] == 'a'.code.toByte() && head[2] == 'r'.code.toByte() && head[3] == '!'.code.toByte() -> RAR
            else -> UNKNOWN
        }
    }
}

/** A compressed file read entry by entry. [visit] gets each entry's name, whether it is a folder, and its content. */
fun interface ArchiveSource {
    fun walk(visit: (name: String, isDir: Boolean, content: InputStream) -> Unit)
}

/** .zip files, read as a stream. */
class ZipSource(private val input: () -> InputStream) : ArchiveSource {
    override fun walk(visit: (String, Boolean, InputStream) -> Unit) {
        ZipInputStream(input()).use { z ->
            while (true) {
                val e = z.nextEntry ?: break
                visit(e.name, e.isDirectory, z)
            }
        }
    }
}

/**
 * Installing a game from a compressed file (.zip, .7z or .rar) the player downloaded from its official page. It finds the game's folder inside the
 * zip, extracts it next to nothing else (to a temporary folder first, then renames it), and never replaces a folder
 * that already exists. Entries that try to leave the destination are skipped.
 */
object ZipInstaller {

    /** Where the game's files start inside the zip ("" or "Folder/") and how many files it holds. */
    data class Plan(val root: String, val files: Int)

    private const val MAX_ENTRIES = 200_000
    private const val MAX_BYTES = 12L * 1024 * 1024 * 1024

    private fun clean(name: String) = name.replace('\\', '/').trimStart('/')

    private fun unsafe(name: String) = name.split('/').any { it == ".." }

    /** The first folder of the zip that looks like an RPG Maker game, or null if none does. */
    fun plan(names: List<String>): Plan? {
        val files = names.map(::clean).filter { it.isNotEmpty() && !it.endsWith("/") && !it.startsWith("__MACOSX/") && !unsafe(it) }
        var best: String? = null
        for (n in files) {
            val lower = n.lowercase(Locale.ROOT)
            val root = when {
                lower.endsWith("/game.ini") || lower == "game.ini" -> n.dropLast("game.ini".length)
                Regex("(^|.*/)data/scripts\\.(rxdata|rvdata2?)$").matches(lower) -> n.substring(0, lower.lastIndexOf("data/scripts."))
                Regex("(^|.*/)data/system\\.(rxdata|rvdata2?)$").matches(lower) -> n.substring(0, lower.lastIndexOf("data/system."))
                lower.endsWith(".rgssad") || lower.endsWith(".rgss2a") || lower.endsWith(".rgss3a") -> n.substring(0, n.lastIndexOf('/') + 1)
                else -> continue
            }
            if (best == null || root.count { it == '/' } < best.count { it == '/' }) best = root
        }
        val r = best ?: return null
        return Plan(r, files.count { it.startsWith(r) })
    }

    /**
     * Why no game was found, in plain words, from what the archive holds: a Windows installer, another compressed file
     * inside, or just a list of what is at the top so the player (or a report) can see what it is.
     */
    fun whyNot(names: List<String>): String {
        val files = names.map(::clean).filter { it.isNotEmpty() && !it.startsWith("__MACOSX/") }
        if (files.isEmpty()) return "El archivo está vacío."
        val inner = files.firstOrNull { it.lowercase(Locale.ROOT).let { l -> l.endsWith(".zip") || l.endsWith(".7z") || l.endsWith(".rar") } }
        if (inner != null) return "Dentro hay otro archivo comprimido (${inner.substringAfterLast('/')}). Descomprime ese en lugar de este."
        val top = files.map { it.substringBefore('/') }.distinct()
        val exe = files.any { it.lowercase(Locale.ROOT).endsWith(".exe") }
        val shown = top.take(6).joinToString(", ") + if (top.size > 6) " y ${top.size - 6} más" else ""
        return "No encontré un juego de RPG Maker (busco Game.ini y la carpeta Data). Contiene: $shown." +
            if (exe && files.none { it.lowercase(Locale.ROOT).contains("data/") }) " Parece un instalador de Windows (.exe): no se puede instalar aquí." else ""
    }

    /** A folder name for the game that doesn't exist yet: "Name", then "Name (2)"... */
    fun freeFolder(base: File, name: String): File {
        val safe = Saves.safeName(name)
        var f = File(base, safe)
        var k = 2
        while (f.exists()) { f = File(base, "$safe ($k)"); k++ }
        return f
    }

    fun extract(input: InputStream, plan: Plan, dest: File, onProgress: (Int) -> Unit = {}): Int =
        extract(ZipSource { input }, plan, dest, onProgress)

    /**
     * Extracts the game into [dest] (which must not exist). Works on a ".part" folder and renames it at the end, so a
     * failure leaves nothing half-installed. Returns the number of files written.
     */
    fun extract(source: ArchiveSource, plan: Plan, dest: File, onProgress: (Int) -> Unit = {}): Int {
        require(!dest.exists()) { "La carpeta ya existe" }
        val part = File(dest.parentFile, dest.name + ".part")
        part.deleteRecursively()
        check(part.mkdirs()) { "No pude crear la carpeta del juego" }
        val canon = part.canonicalPath + File.separator
        var count = 0
        var bytes = 0L
        try {
            source.walk { rawName, isDir, content ->
                val name = clean(rawName)
                if (isDir || name.isEmpty() || unsafe(name) || name.startsWith("__MACOSX/") || !name.startsWith(plan.root)) return@walk
                val target = File(part, name.removePrefix(plan.root))
                if (!target.canonicalPath.startsWith(canon)) return@walk
                check(++count <= MAX_ENTRIES) { "El archivo tiene demasiados archivos" }
                target.parentFile?.mkdirs()
                target.outputStream().use { out ->
                    val buf = ByteArray(64 * 1024)
                    while (true) {
                        val n = content.read(buf)
                        if (n < 0) break
                        bytes += n
                        check(bytes <= MAX_BYTES) { "El archivo pesa demasiado" }
                        out.write(buf, 0, n)
                    }
                }
                if (count % 50 == 0) onProgress(count)
            }
            check(part.renameTo(dest)) { "No pude terminar la instalación" }
            return count
        } catch (e: Exception) {
            part.deleteRecursively()
            throw e
        }
    }

    /** Names of the entries, read without extracting anything. */
    fun listNames(input: InputStream): List<String> = listNames(ZipSource { input })

    fun listNames(source: ArchiveSource): List<String> {
        val out = ArrayList<String>()
        source.walk { name, _, _ ->
            out += name
            check(out.size <= MAX_ENTRIES) { "El archivo tiene demasiados archivos" }
        }
        return out
    }
}

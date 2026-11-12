package io.github.nikalbazu24.gahvoria.core

import java.io.File
import java.io.InputStream
import java.util.Locale
import java.util.zip.ZipInputStream

/**
 * Installing a game from a .zip the player downloaded from its official page. It finds the game's folder inside the
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
                lower.endsWith(".rgssad") || lower.endsWith(".rgss2a") || lower.endsWith(".rgss3a") -> n.substring(0, n.lastIndexOf('/') + 1)
                else -> continue
            }
            if (best == null || root.count { it == '/' } < best.count { it == '/' }) best = root
        }
        val r = best ?: return null
        return Plan(r, files.count { it.startsWith(r) })
    }

    /** A folder name for the game that doesn't exist yet: "Name", then "Name (2)"... */
    fun freeFolder(base: File, name: String): File {
        val safe = Saves.safeName(name)
        var f = File(base, safe)
        var k = 2
        while (f.exists()) { f = File(base, "$safe ($k)"); k++ }
        return f
    }

    /**
     * Extracts the game into [dest] (which must not exist). Works on a ".part" folder and renames it at the end, so a
     * failure leaves nothing half-installed. Returns the number of files written.
     */
    fun extract(input: InputStream, plan: Plan, dest: File, onProgress: (Int) -> Unit = {}): Int {
        require(!dest.exists()) { "La carpeta ya existe" }
        val part = File(dest.parentFile, dest.name + ".part")
        part.deleteRecursively()
        check(part.mkdirs()) { "No pude crear la carpeta del juego" }
        val canon = part.canonicalPath + File.separator
        var count = 0
        var bytes = 0L
        try {
            ZipInputStream(input).use { z ->
                while (true) {
                    val e = z.nextEntry ?: break
                    val name = clean(e.name)
                    if (e.isDirectory || name.isEmpty() || unsafe(name) || name.startsWith("__MACOSX/") || !name.startsWith(plan.root)) continue
                    val target = File(part, name.removePrefix(plan.root))
                    if (!target.canonicalPath.startsWith(canon)) continue
                    check(++count <= MAX_ENTRIES) { "El .zip tiene demasiados archivos" }
                    target.parentFile?.mkdirs()
                    target.outputStream().use { out ->
                        val buf = ByteArray(64 * 1024)
                        while (true) {
                            val n = z.read(buf)
                            if (n < 0) break
                            bytes += n
                            check(bytes <= MAX_BYTES) { "El .zip pesa demasiado" }
                            out.write(buf, 0, n)
                        }
                    }
                    if (count % 50 == 0) onProgress(count)
                }
            }
            check(part.renameTo(dest)) { "No pude terminar la instalación" }
            return count
        } catch (e: Exception) {
            part.deleteRecursively()
            throw e
        }
    }

    /** Names of the entries, read without extracting anything. */
    fun listNames(input: InputStream): List<String> {
        val out = ArrayList<String>()
        ZipInputStream(input).use { z ->
            while (true) {
                val e = z.nextEntry ?: break
                out += e.name
                if (out.size > MAX_ENTRIES) break
            }
        }
        return out
    }
}

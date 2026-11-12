package io.github.nikalbazu24.gahvoria.core

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Backing up and restoring a game's saves. A backup is a plain .zip of the save files (nothing else), so it can also
 * be opened on a computer. Restoring never replaces anything silently: the caller asks first, and a replaced save is
 * kept next to the new one with the ".antes" ending.
 */
object SaveBackup {
    const val MARKER = "gahvoria-respaldo.txt"
    private const val MAX_TOTAL = 256L * 1024 * 1024

    class Item(val name: String, val bytes: ByteArray)

    /** Writes the game's saves into a zip. Returns how many saves went in. */
    fun export(saveDir: File, out: OutputStream, gameTitle: String): Int {
        val saves = Saves.list(saveDir)
        ZipOutputStream(out).use { z ->
            z.putNextEntry(ZipEntry(MARKER))
            z.write(("Respaldo de partidas de Gahvoria\nJuego: $gameTitle\n").toByteArray(Charsets.UTF_8))
            z.closeEntry()
            for (f in saves) {
                z.putNextEntry(ZipEntry(f.name).also { it.time = f.lastModified() })
                f.inputStream().use { it.copyTo(z) }
                z.closeEntry()
            }
        }
        return saves.size
    }

    /** Saves found in a backup zip. Entries with folders or odd names are ignored, so a zip can't write elsewhere. */
    fun readZip(input: InputStream): List<Item> {
        val items = ArrayList<Item>()
        var total = 0L
        ZipInputStream(input).use { z ->
            while (true) {
                val e = z.nextEntry ?: break
                if (e.isDirectory || e.name.contains('/') || e.name.contains('\\') || e.name.contains("..")) continue
                if (!Saves.looksLikeSave(e.name)) continue
                val buf = ByteArrayOutputStream()
                z.copyTo(buf)
                total += buf.size()
                if (total > MAX_TOTAL) break
                items += Item(e.name, buf.toByteArray())
            }
        }
        return items
    }

    /** Saves in another folder (a JoiPlay, Kirin or older Gahvoria game folder keep them next to the game). */
    fun readFolder(dir: File): List<Item> =
        Saves.list(dir).filter { it.length() <= MAX_TOTAL }.map { Item(it.name, it.readBytes()) }

    /** Names that already exist in the destination: the ones to ask about before replacing. */
    fun conflicts(items: List<Item>, dest: File): List<String> =
        items.map { it.name }.filter { File(dest, it).exists() }

    /**
     * Copies the saves into the destination. With [replace], an existing save is first renamed to "<name>.antes"
     * (an older ".antes" is overwritten); without it, existing saves are left alone. Returns (written, skipped).
     */
    fun restore(items: List<Item>, dest: File, replace: Boolean): Pair<Int, Int> {
        dest.mkdirs()
        var written = 0
        var skipped = 0
        for (it in items) {
            val target = File(dest, it.name)
            if (target.exists()) {
                if (!replace) { skipped++; continue }
                val old = File(dest, it.name + ".antes")
                old.delete()
                if (!target.renameTo(old)) { skipped++; continue }
            }
            val tmp = File(dest, it.name + ".part")
            tmp.writeBytes(it.bytes)
            if (tmp.renameTo(target)) written++ else { tmp.delete(); skipped++ }
        }
        return written to skipped
    }
}

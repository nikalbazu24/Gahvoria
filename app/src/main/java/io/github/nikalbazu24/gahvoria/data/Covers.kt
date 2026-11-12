package io.github.nikalbazu24.gahvoria.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import io.github.nikalbazu24.gahvoria.core.CoverCatalog
import io.github.nikalbazu24.gahvoria.core.CoverEntry
import io.github.nikalbazu24.gahvoria.core.CoverFinder
import io.github.nikalbazu24.gahvoria.core.Game
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

/**
 * Covers for the library. Priority: the official image its creators published (only games in the public catalog) →
 * an image from the game's own folder → the drawn cover. Everything is a small copy kept in the app's cache, so covers
 * appear at once, even offline, and the game's folder is only ever read.
 */
class Covers(context: Context, private val online: () -> Boolean) {
    private val dir = File(context.cacheDir, "covers").apply { mkdirs() }
    private val memory = object : LruCache<String, Bitmap>(16 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap) = value.byteCount
    }
    @Volatile private var catalog: List<CoverEntry>? = null

    private fun key(g: Game): String =
        MessageDigest.getInstance("SHA-1").digest(g.path.toByteArray()).joinToString("") { "%02x".format(it) }.take(20)

    /** The best copy already in memory or on disk (no network, no folder scan). Fast enough to call while drawing. */
    fun peek(g: Game): Bitmap? {
        val k = key(g)
        return image(File(dir, "$k.web.jpg")) ?: image(File(dir, "$k.local.jpg"))
    }

    /** The file of the best cover copy (for the game screen's loading background), or null. */
    fun bestFile(g: Game): File? {
        val k = key(g)
        return listOf(File(dir, "$k.web.jpg"), File(dir, "$k.local.jpg")).firstOrNull { it.exists() }
    }

    /** Who made the image, when it came from the Internet ("Imagen: EricLostie · Whack a Hack!"). */
    fun credit(g: Game): String? {
        val k = key(g)
        if (!File(dir, "$k.web.jpg").exists()) return null
        return runCatching { File(dir, "$k.web.txt").readText().trim() }.getOrNull()?.takeIf { it.isNotEmpty() }
    }

    fun source(g: Game): Source {
        val k = key(g)
        return when {
            File(dir, "$k.web.jpg").exists() -> Source.WEB
            File(dir, "$k.local.jpg").exists() -> Source.FOLDER
            else -> Source.NONE
        }
    }

    enum class Source { WEB, FOLDER, NONE }

    /** Like [peek] but only from memory: instant, never reads the disk. */
    fun peekMemory(g: Game): Bitmap? {
        val k = key(g)
        return image(File(dir, "$k.web.jpg"), false) ?: image(File(dir, "$k.local.jpg"), false)
    }

    private fun image(f: File, decode: Boolean = true): Bitmap? {
        if (!f.exists()) return null
        memory.get(f.path + f.lastModified())?.let { return it }
        if (!decode) return null
        val o = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.RGB_565 }
        val b = runCatching { BitmapFactory.decodeFile(f.path, o) }.getOrNull() ?: return null
        memory.put(f.path + f.lastModified(), b)
        return b
    }

    /** Offline step: copies the image found in the game's folder. Returns true when something changed. */
    fun refreshLocal(g: Game): Boolean {
        val k = key(g)
        val out = File(dir, "$k.local.jpg")
        val stamp = File(dir, "$k.local.src")
        val src = runCatching { CoverFinder.find(File(g.path)) }.getOrNull()
        if (src == null) {
            if (!out.exists()) return false
            out.delete(); stamp.delete(); return true
        }
        val sig = src.path + "|" + src.lastModified() + "|" + src.length()
        if (out.exists() && runCatching { stamp.readText() }.getOrNull() == sig) return false
        val bmp = decodeScaled { BitmapFactory.decodeFile(src.path, it) } ?: return false
        save(bmp, out)
        stamp.writeText(sig)
        return true
    }

    /** Online step: the official image of a catalog game. Returns true when a new image was saved. */
    fun refreshOnline(g: Game): Boolean {
        if (!online()) return false
        val k = key(g)
        val out = File(dir, "$k.web.jpg")
        if (out.exists()) return false
        val miss = File(dir, "$k.web.miss")
        if (miss.exists() && System.currentTimeMillis() - miss.lastModified() < 24L * 3600_000) return false
        val entry = CoverCatalog.match(loadCatalog(), g.title, File(g.path).name) ?: return false
        for (url in entry.urls) {
            val bmp = runCatching { download(url) }.getOrNull() ?: continue
            save(bmp, out)
            File(dir, "$k.web.txt").writeText(if (entry.credit.isNotEmpty()) "Imagen: ${entry.credit}" else "")
            miss.delete()
            return true
        }
        runCatching { miss.writeText(System.currentTimeMillis().toString()) }
        return false
    }

    private fun loadCatalog(): List<CoverEntry> {
        catalog?.let { return it }
        val f = File(dir, "catalog.json")
        val fresh = f.exists() && System.currentTimeMillis() - f.lastModified() < 6L * 3600_000
        if (!fresh) runCatching {
            val c = AppUpdater.open(CoverCatalog.URL)
            try {
                val bytes = c.inputStream.use { it.readBytes() }
                if (CoverCatalog.parse(bytes.toString(Charsets.UTF_8)).isNotEmpty()) f.writeBytes(bytes)
            } finally { c.disconnect() }
        }
        val list = runCatching { CoverCatalog.parse(f.readText()) }.getOrDefault(emptyList())
        if (list.isNotEmpty()) catalog = list
        return list
    }

    private fun download(url: String): Bitmap? {
        val c = AppUpdater.open(url)
        try {
            if (c.contentLengthLong > MAX_DOWNLOAD) return null
            val out = java.io.ByteArrayOutputStream()
            c.inputStream.use { input ->
                val buf = ByteArray(16 * 1024)
                while (true) {
                    val n = input.read(buf)
                    if (n < 0) break
                    out.write(buf, 0, n)
                    if (out.size() > MAX_DOWNLOAD) return null
                }
            }
            val bytes = out.toByteArray()
            return decodeScaled { BitmapFactory.decodeByteArray(bytes, 0, bytes.size, it) }
        } finally { c.disconnect() }
    }

    private fun decodeScaled(decode: (BitmapFactory.Options) -> Bitmap?): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        decode(bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2
        val o = BitmapFactory.Options().apply { inSampleSize = sample; inPreferredConfig = Bitmap.Config.RGB_565 }
        val b = decode(o) ?: return null
        val side = maxOf(b.width, b.height)
        if (side <= MAX_SIDE) return b
        val k = MAX_SIDE.toFloat() / side
        return Bitmap.createScaledBitmap(b, (b.width * k).toInt().coerceAtLeast(1), (b.height * k).toInt().coerceAtLeast(1), true)
    }

    private fun save(b: Bitmap, out: File) {
        val tmp = File(out.path + ".part")
        FileOutputStream(tmp).use { b.compress(Bitmap.CompressFormat.JPEG, 88, it) }
        check(tmp.renameTo(out)) { "No se pudo guardar la imagen" }
    }

    /** Forgets copies of games that are no longer in the library. */
    fun prune(keep: List<Game>) {
        val keys = keep.map(::key).toSet()
        dir.listFiles()?.forEach { f ->
            val k = f.name.substringBefore('.')
            if (f.name != "catalog.json" && k !in keys) f.delete()
        }
    }

    private companion object {
        const val MAX_SIDE = 900
        const val MAX_DOWNLOAD = 8L * 1024 * 1024
    }
}

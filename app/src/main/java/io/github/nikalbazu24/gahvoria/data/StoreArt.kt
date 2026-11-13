package io.github.nikalbazu24.gahvoria.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import io.github.nikalbazu24.gahvoria.core.StoreGame
import io.github.nikalbazu24.gahvoria.core.StoreImages
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

/**
 * Pictures for the store. They come from the creator's official page, are kept small in the app's cache and are
 * looked for again after a week. Without Internet, or when the page has none, the drawn cartridge stays.
 */
class StoreArt(context: Context, private val online: () -> Boolean) {
    private val dir = File(context.cacheDir, "store-art").apply { mkdirs() }
    private val memory = object : LruCache<String, Bitmap>(8 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap) = value.byteCount
    }

    private fun key(g: StoreGame) =
        MessageDigest.getInstance("SHA-1").digest((g.id + "|" + g.page).toByteArray()).joinToString("") { "%02x".format(it) }.take(20)

    /** The saved picture (null: none yet). Cheap enough to call while drawing. */
    fun peek(g: StoreGame): Bitmap? {
        val f = File(dir, key(g) + ".jpg")
        if (!f.exists()) return null
        val k = f.path + f.lastModified()
        memory.get(k)?.let { return it }
        val b = runCatching { BitmapFactory.decodeFile(f.path, BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.RGB_565 }) }.getOrNull() ?: return null
        memory.put(k, b)
        return b
    }

    /** Where the picture comes from, for the credit line ("Imagen de p-insurgence.com"). */
    fun credit(g: StoreGame): String? {
        if (!File(dir, key(g) + ".jpg").exists()) return null
        val host = runCatching { java.net.URI(g.image.ifEmpty { g.page }).host }.getOrNull() ?: return null
        return "Imagen de $host"
    }

    /** Looks for the picture of [g]. Returns true when a new one was saved. Meant for a worker thread. */
    fun refresh(g: StoreGame): Boolean {
        if (!online()) return false
        val k = key(g)
        val out = File(dir, "$k.jpg")
        val miss = File(dir, "$k.miss")
        val ageLimit = 7L * 24 * 3600_000
        if (out.exists() && System.currentTimeMillis() - out.lastModified() < ageLimit) return false
        if (miss.exists() && System.currentTimeMillis() - miss.lastModified() < 24L * 3600_000) return false
        val url = g.image.ifEmpty {
            if (!StoreImages.hasUsefulImage(g.page)) null
            else runCatching { StoreImages.ogImage(readText(g.page), g.page) }.getOrNull()
        }
        val bmp = url?.let { runCatching { download(it) }.getOrNull() }
        if (bmp == null) { runCatching { miss.writeText(System.currentTimeMillis().toString()) }; return false }
        val tmp = File(out.path + ".part")
        FileOutputStream(tmp).use { bmp.compress(Bitmap.CompressFormat.JPEG, 86, it) }
        if (!tmp.renameTo(out)) return false
        miss.delete()
        return true
    }

    private fun readText(url: String): String {
        val c = AppUpdater.open(url)
        try { return String(readLimited(c.inputStream, 600_000) ?: return "", Charsets.UTF_8) } finally { c.disconnect() }
    }

    private fun download(url: String): Bitmap? {
        val c = AppUpdater.open(url)
        try {
            if (c.contentLengthLong > MAX_IMAGE) return null
            val bytes = readLimited(c.inputStream, MAX_IMAGE) ?: return null
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            if (bounds.outWidth < 120 || bounds.outHeight < 80) return null   // icons and logos, not a picture of the game
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= SIDE) sample *= 2
            val b = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample; inPreferredConfig = Bitmap.Config.RGB_565 }) ?: return null
            val side = maxOf(b.width, b.height)
            if (side <= SIDE) return b
            val k = SIDE.toFloat() / side
            return Bitmap.createScaledBitmap(b, (b.width * k).toInt().coerceAtLeast(1), (b.height * k).toInt().coerceAtLeast(1), true)
        } finally { c.disconnect() }
    }

    /** Reads at most [max] bytes; null when the content is bigger. */
    private fun readLimited(input: java.io.InputStream, max: Long): ByteArray? {
        val out = ByteArrayOutputStream()
        input.use {
            val buf = ByteArray(16 * 1024)
            while (true) {
                val n = it.read(buf)
                if (n < 0) break
                out.write(buf, 0, n)
                if (out.size() > max) return null
            }
        }
        return out.toByteArray()
    }

    private companion object {
        const val SIDE = 720
        const val MAX_IMAGE = 6L * 1024 * 1024
    }
}

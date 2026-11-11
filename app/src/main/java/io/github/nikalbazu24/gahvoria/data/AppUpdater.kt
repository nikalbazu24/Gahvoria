package io.github.nikalbazu24.gahvoria.data

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import io.github.nikalbazu24.gahvoria.BuildConfig
import io.github.nikalbazu24.gahvoria.core.Release
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Gahvoria's own updates, from the "dev" release on GitHub. The new APK is checked (same app,
 * the announced version) before Android's installer opens; nothing is installed without a tap,
 * and games and saves are never touched.
 */
object AppUpdater {

    fun installedCode(c: Context): Int = runCatching {
        @Suppress("DEPRECATION")
        c.packageManager.getPackageInfo(c.packageName, 0).versionCode
    }.getOrDefault(0)

    /** Blocking: call it off the main thread. Null when there is no connection or no valid file. */
    fun latest(): Release? = runCatching {
        Release.parse(get(BuildConfig.UPDATE_BASE + "version.json").readBytes().toString(Charsets.UTF_8))
    }.getOrNull()

    /** Blocking download with progress (0..1). Returns the checked APK. */
    fun download(c: Context, r: Release, progress: (Float) -> Unit): File {
        val dir = File(c.cacheDir, "updates").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val part = File(dir, "gahvoria-${r.versionCode}.part")
        val conn = open(BuildConfig.UPDATE_BASE + r.apk)
        try {
            val total = conn.contentLengthLong
            conn.inputStream.use { input ->
                part.outputStream().use { out ->
                    val buf = ByteArray(64 * 1024)
                    var done = 0L
                    while (true) {
                        val n = input.read(buf)
                        if (n < 0) break
                        out.write(buf, 0, n)
                        done += n
                        if (total > 0) progress((done.toFloat() / total).coerceIn(0f, 1f))
                    }
                }
            }
        } finally { conn.disconnect() }
        val apk = File(dir, "gahvoria-${r.versionCode}.apk")
        check(part.renameTo(apk)) { "No se pudo guardar la descarga" }
        @Suppress("DEPRECATION")
        val info = c.packageManager.getPackageArchiveInfo(apk.path, 0)
        @Suppress("DEPRECATION")
        check(info != null && info.packageName == c.packageName && info.versionCode == r.versionCode) {
            apk.delete(); "El archivo descargado no es la versión anunciada"
        }
        return apk
    }

    /** Android asks once for permission to install apps from Gahvoria. */
    fun canInstall(a: Activity) = Build.VERSION.SDK_INT < 26 || a.packageManager.canRequestPackageInstalls()

    fun askInstallPermission(a: Activity) {
        if (Build.VERSION.SDK_INT >= 26)
            a.startActivity(Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + a.packageName)))
    }

    fun install(a: Activity, apk: File) {
        val uri = FileProvider.getUriForFile(a, a.packageName + ".files", apk)
        a.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun get(url: String): java.io.InputStream {
        val c = open(url)
        return c.inputStream
    }

    private fun open(url: String): HttpURLConnection {
        var target = url
        repeat(5) {
            val c = URL(target).openConnection() as HttpURLConnection
            c.connectTimeout = 10_000
            c.readTimeout = 20_000
            c.instanceFollowRedirects = false
            c.setRequestProperty("User-Agent", "Gahvoria/" + BuildConfig.VERSION_NAME)
            val code = c.responseCode
            if (code in 300..399) {
                val next = c.getHeaderField("Location") ?: error("Redirección sin destino")
                c.disconnect()
                check(next.startsWith("https://")) { "Solo se descargan versiones por HTTPS" }
                target = next
            } else {
                check(code == 200) { c.disconnect(); "El servidor respondió $code" }
                return c
            }
        }
        error("Demasiadas redirecciones")
    }
}

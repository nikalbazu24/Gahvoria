package io.github.nikalbazu24.gahvoria.data

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import io.github.nikalbazu24.gahvoria.BuildConfig
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** What the engine left behind when a game closed with an error. It lives on the device only; sending it is the player's choice. */
object GameReport {

    /**
     * The error of the game session that started at {@code since}, or null. Two places: what the engine wrote
     * (Ruby errors it catches) and errorlog.txt, which Pokémon Essentials writes itself in the game's folder or its
     * save folder when its own error screen appears. The game screen deletes old engine errors when it starts.
     */
    fun errorSince(a: Activity, since: Long, game: File? = null, saves: File? = null): String? {
        val engine = File(a.filesDir, "last_error.txt")
        if (engine.isFile && engine.lastModified() >= since)
            runCatching { engine.readText().take(4000) }.getOrNull()?.takeIf { it.isNotBlank() }?.let { return it }
        for (dir in listOfNotNull(game, saves)) {
            val f = File(dir, "errorlog.txt")
            if (f.isFile && f.lastModified() >= since)
                runCatching { f.readText().take(4000) }.getOrNull()?.takeIf { it.isNotBlank() }?.let { return "Registro del juego (errorlog.txt):\n$it" }
        }
        return null
    }

    /** The most recent error left by this game, whenever it happened (for the "send last report" row). */
    fun latest(a: Activity, game: File?, saves: File?): Pair<Long, String>? {
        val files = listOf(File(a.filesDir, "last_error.txt")) + listOfNotNull(game, saves).map { File(it, "errorlog.txt") }
        val f = files.filter { it.isFile && it.length() > 0 }.maxByOrNull { it.lastModified() } ?: return null
        val text = runCatching { f.readText().take(4000) }.getOrNull()?.takeIf { it.isNotBlank() } ?: return null
        return f.lastModified() to text
    }

    /** "Script 'X' line 12: SystemStackError occured." */
    fun firstLine(text: String): String =
        text.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() }.orEmpty().take(140)

    fun copy(a: Activity, text: String) {
        val cm = a.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        cm.setPrimaryClip(ClipData.newPlainText("Informe de Gahvoria", text))
    }

    /** True when this build can send reports (the token comes from the build, never from the player). */
    fun canSend(): Boolean = BuildConfig.REPORT_TOKEN.isNotEmpty() && BuildConfig.REPORT_URL.startsWith("https://")

    /**
     * Sends the report to the maintainer's folder through the Apps Script web app. Runs on the caller's thread: call it
     * from a background thread. Returns the id of the saved file, or throws with a short message for the player.
     */
    fun send(name: String, text: String): String {
        check(canSend()) { "Este APK no puede enviar informes" }
        val body = "{\"token\":" + json(BuildConfig.REPORT_TOKEN) + ",\"name\":" + json(name) + ",\"text\":" + json(text.take(200_000)) + "}"
        val c = URL(BuildConfig.REPORT_URL).openConnection() as HttpURLConnection
        try {
            c.connectTimeout = 10_000
            c.readTimeout = 20_000
            c.requestMethod = "POST"
            c.doOutput = true
            c.instanceFollowRedirects = true
            c.setRequestProperty("Content-Type", "text/plain;charset=utf-8")
            c.setRequestProperty("User-Agent", "Gahvoria/" + BuildConfig.VERSION_NAME)
            c.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val code = c.responseCode
            check(code in 200..299) { "El servidor respondió $code" }
            val reply = c.inputStream.bufferedReader().use { it.readText() }
            check(reply.contains("\"ok\":true")) { "El informe no fue aceptado" }
            return Regex("\"id\":\"([^\"]+)\"").find(reply)?.groupValues?.get(1) ?: "enviado"
        } finally { c.disconnect() }
    }

    private fun json(v: String): String {
        val b = StringBuilder("\"")
        for (ch in v) when {
            ch == '"' -> b.append("\\\"")
            ch == '\\' -> b.append("\\\\")
            ch == '\n' -> b.append("\\n")
            ch == '\r' -> b.append("\\r")
            ch == '\t' -> b.append("\\t")
            ch < ' ' -> b.append(String.format("\\u%04x", ch.code))
            else -> b.append(ch)
        }
        return b.append('"').toString()
    }
}

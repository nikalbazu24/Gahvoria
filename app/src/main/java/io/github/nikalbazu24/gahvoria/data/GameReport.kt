package io.github.nikalbazu24.gahvoria.data

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import java.io.File

/** What the engine left behind when a game closed with an error. It lives on the device only; sending it is the player's choice. */
object GameReport {

    /** The error of the game session that started at {@code since}, or null. The game screen deletes old ones when it starts. */
    fun errorSince(a: Activity, since: Long): String? {
        val f = File(a.filesDir, "last_error.txt")
        if (!f.isFile || f.lastModified() < since) return null
        return runCatching { f.readText().take(4000) }.getOrNull()?.takeIf { it.isNotBlank() }
    }

    /** "Script 'X' line 12: SystemStackError occured." */
    fun firstLine(text: String): String =
        text.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() }.orEmpty().take(140)

    fun copy(a: Activity, text: String) {
        val cm = a.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        cm.setPrimaryClip(ClipData.newPlainText("Informe de Gahvoria", text))
    }
}

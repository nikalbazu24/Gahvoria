package io.github.nikalbazu24.gahvoria.data

import android.content.Context
import io.github.nikalbazu24.gahvoria.core.Palettes
import io.github.nikalbazu24.gahvoria.core.ThemeMode

/** Gahvoria's few settings. */
class Settings(context: Context) {
    private val prefs = context.getSharedPreferences("gahvoria", Context.MODE_PRIVATE)

    var palette: String
        get() = Palettes.byId(prefs.getString("palette", Palettes.DEFAULT)).id
        set(v) = prefs.edit().putString("palette", v).apply()

    var mode: ThemeMode
        get() = runCatching { ThemeMode.valueOf(prefs.getString("mode", ThemeMode.SYSTEM.name)!!) }.getOrDefault(ThemeMode.SYSTEM)
        set(v) = prefs.edit().putString("mode", v.name).apply()

    /** Look for a new Gahvoria version when the app opens (it never installs without a tap). */
    var autoCheck: Boolean
        get() = prefs.getBoolean("auto_check", true)
        set(v) = prefs.edit().putBoolean("auto_check", v).apply()

    var lastCheck: Long
        get() = prefs.getLong("last_check", 0)
        set(v) = prefs.edit().putLong("last_check", v).apply()

    /** The game that was opened last and when, to add up the time played when the player comes back. */
    fun startSession(path: String, at: Long) = prefs.edit().putString("session_path", path).putLong("session_at", at).apply()

    fun takeSession(): Pair<String, Long>? {
        val path = prefs.getString("session_path", null) ?: return null
        val at = prefs.getLong("session_at", 0)
        prefs.edit().remove("session_path").remove("session_at").apply()
        return path to at
    }
}

package io.github.nikalbazu24.gahvoria.data

import android.content.Context
import io.github.nikalbazu24.gahvoria.core.Depth
import io.github.nikalbazu24.gahvoria.core.Palettes
import io.github.nikalbazu24.gahvoria.core.ThemeMode

/** Gahvoria's few settings. */
class Settings(context: Context) {
    private val prefs = context.getSharedPreferences("gahvoria", Context.MODE_PRIVATE)

    var palette: String
        get() = Palettes.byId(prefs.getString("palette", Palettes.DEFAULT)).id
        set(v) = prefs.edit().putString("palette", v).apply()

    /** The launcher icon takes the colors of the chosen palette. */
    var iconFollows: Boolean
        get() = prefs.getBoolean("icon_follows", true)
        set(v) = prefs.edit().putBoolean("icon_follows", v).apply()

    var mode: ThemeMode
        get() = runCatching { ThemeMode.valueOf(prefs.getString("mode", ThemeMode.SYSTEM.name)!!) }.getOrDefault(ThemeMode.SYSTEM)
        set(v) = prefs.edit().putString("mode", v.name).apply()

    /** Look for a new Gahvoria version when the app opens (it never installs without a tap). */
    var autoCheck: Boolean
        get() = prefs.getBoolean("auto_check", true)
        set(v) = prefs.edit().putBoolean("auto_check", v).apply()

    /** Where the shortcuts for other launchers were written (null: never). New games add theirs there. */
    var shortcutsFolder: String?
        get() = prefs.getString("shortcuts_folder", null)
        set(v) = prefs.edit().putString("shortcuts_folder", v).apply()

    /** Look for the official image of a game on the Internet (only games in the public catalog have one). */
    var coversOnline: Boolean
        get() = prefs.getBoolean("covers_online", true)
        set(v) = prefs.edit().putBoolean("covers_online", v).apply()

    /** The player's arrangement of the touch controls for one game (see TouchLayout), or "". */
    fun pad(path: String): String = prefs.getString("pad:$path", "") ?: ""

    fun setPad(path: String, spec: String) =
        prefs.edit().apply { if (spec.isEmpty()) remove("pad:$path") else putString("pad:$path", spec) }.apply()

    /** "dev" (every build, the default for now) or "stable" (only versions the maintainer promoted). */
    var updateChannel: String
        get() = prefs.getString("update_channel", io.github.nikalbazu24.gahvoria.core.UpdateChannel.DEV)
            ?.takeIf { io.github.nikalbazu24.gahvoria.core.UpdateChannel.valid(it) } ?: io.github.nikalbazu24.gahvoria.core.UpdateChannel.DEV
        set(v) = prefs.edit().putString("update_channel", v).apply()

    /** Shows the creator panel. Off until the player turns it on. */
    var creatorMode: Boolean
        get() = prefs.getBoolean("creator_mode", false)
        set(v) = prefs.edit().putBoolean("creator_mode", v).apply()

    /** The games the player marked as their own (made by them). */
    fun mine(path: String): Boolean = prefs.getBoolean("mine:$path", false)
    fun setMine(path: String, on: Boolean) = prefs.edit().apply { if (on) putBoolean("mine:$path", true) else remove("mine:$path") }.apply()

    /** The engine ("modern"/"classic") a game was last opened with, to know which one failed or worked. */
    fun lastEngine(path: String): String? = prefs.getString("engine_last:$path", null)
    fun setLastEngine(path: String, engine: String) = prefs.edit().putString("engine_last:$path", engine).apply()

    /** The engine that worked for an "automatic" game, learned from real runs. It beats the script analysis. */
    fun learnedEngine(path: String): String? = prefs.getString("engine_ok:$path", null)
    fun setLearnedEngine(path: String, engine: String?) =
        prefs.edit().apply { if (engine == null) remove("engine_ok:$path") else putString("engine_ok:$path", engine) }.apply()

    /** The game failed and was already reopened once with the other engine (so it is not tried a third time). */
    fun retried(path: String): Boolean = prefs.getBoolean("engine_retry:$path", false)
    fun setRetried(path: String, on: Boolean) =
        prefs.edit().apply { if (on) putBoolean("engine_retry:$path", true) else remove("engine_retry:$path") }.apply()

    /** Translation. Off until the player turns it on, and then also on for each game they want it in. */
    var translateOn: Boolean
        get() = prefs.getBoolean("tr_on", false)
        set(v) = prefs.edit().putBoolean("tr_on", v).apply()

    /** "mlkit" (on the phone, free) or "deepl" (the player's own key). */
    var translateProvider: String
        get() = prefs.getString("tr_provider", "mlkit") ?: "mlkit"
        set(v) = prefs.edit().putString("tr_provider", v).apply()

    /** The player's own DeepL key. Stays on this phone; Gahvoria ships none. */
    var deeplKey: String
        get() = prefs.getString("tr_deepl_key", "") ?: ""
        set(v) = prefs.edit().putString("tr_deepl_key", v.trim()).apply()

    var translateTarget: String
        get() = prefs.getString("tr_target", "es") ?: "es"
        set(v) = prefs.edit().putString("tr_target", v).apply()

    fun translateGame(path: String): Boolean = prefs.getBoolean("tr_game:$path", false)
    fun setTranslateGame(path: String, on: Boolean) = prefs.edit().putBoolean("tr_game:$path", on).apply()

    /** Screen for one game: "original", "wide" (wide everywhere, battles too) or "wide_safe" (wide only on the map). */
    fun screen(path: String): String =
        prefs.getString("screen:$path", null) ?: if (prefs.getBoolean("wide:$path", false)) "wide_safe" else "original"
    fun setScreen(path: String, mode: String) = prefs.edit().putString("screen:$path", mode).apply()
    fun retro(path: String): Boolean = prefs.getBoolean("retro:$path", false)
    fun setRetro(path: String, on: Boolean) = prefs.edit().putBoolean("retro:$path", on).apply()
    /** Lean-back angle in degrees for the 2.5D / 3D effect (0 = flat). */
    fun tilt(path: String): Int = Depth.clamp(prefs.getInt("tilt:$path", 0))
    fun setTilt(path: String, deg: Int) = prefs.edit().apply { if (deg <= 0) remove("tilt:$path") else putInt("tilt:$path", Depth.clamp(deg)) }.apply()

    /** Everything kept about one game (touch layout, translation, screen, engine memory) when it is deleted. */
    fun forgetGame(path: String) = prefs.edit().apply {
        for (k in listOf("pad", "tr_game", "wide", "screen", "retro", "tilt", "engine_last", "engine_ok", "engine_retry", "mine")) remove("$k:$path")
    }.apply()

    /** The introduction was finished or skipped once. */
    var introSeen: Boolean
        get() = prefs.getBoolean("intro_seen", false)
        set(v) = prefs.edit().putBoolean("intro_seen", v).apply()

    /** The last big update whose changes were shown (see Intro.UPDATES). */
    var seenUpdate: Int
        get() = prefs.getInt("seen_update", 0)
        set(v) = prefs.edit().putInt("seen_update", v).apply()

    var lastCheck: Long
        get() = prefs.getLong("last_check", 0)
        set(v) = prefs.edit().putLong("last_check", v).apply()

    /** The game that was opened last and when, to add up the time played when the player comes back. */
    fun startSession(path: String, at: Long) = prefs.edit().putString("session_path", path).putLong("session_at", at).apply()

    /** A game was opened and Gahvoria hasn't counted its time yet: the player is coming back from it. */
    fun hasSession() = prefs.contains("session_path")

    fun takeSession(): Pair<String, Long>? {
        val path = prefs.getString("session_path", null) ?: return null
        val at = prefs.getLong("session_at", 0)
        prefs.edit().remove("session_path").remove("session_at").apply()
        return path to at
    }
}

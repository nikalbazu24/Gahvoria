package io.github.nikalbazu24.gahvoria.data

import android.app.Activity
import android.content.res.Configuration
import com.hatkid.mkxpz.GameActivity
import io.github.nikalbazu24.gahvoria.data.Covers
import io.github.nikalbazu24.gahvoria.core.Game
import io.github.nikalbazu24.gahvoria.core.GameProfile
import io.github.nikalbazu24.gahvoria.core.LoadRecord
import io.github.nikalbazu24.gahvoria.core.LoadTimes
import io.github.nikalbazu24.gahvoria.core.Palettes
import io.github.nikalbazu24.gahvoria.core.Saves
import java.io.File

/** Opens a game with the reused mkxp-z engine. */
object Launcher {

    fun classicAvailable(a: Activity) = GameActivity.classicAvailable(a)

    fun saveDir(game: Game): File {
        val dir = File(game.path)
        return Saves.dirFor(dir, game.title, Storage.root(), Storage.canWrite(dir))
    }

    /** What the game's scripts say (Essentials version...). Cheap enough to read when a game is opened or shown. */
    fun profile(game: Game): GameProfile = GameProfile.read(File(game.path))

    /** The engine "auto" picks: classic for Essentials 17-18 (Ruby 1.8 era), modern otherwise. */
    fun autoEngine(game: Game): String = profile(game).recommendedEngine

    /** The engine a game really starts with. The classic one only when the APK carries it. */
    fun engineFor(a: Activity, game: Game): String {
        val wanted = if (game.engine == Game.ENGINE_AUTO) (Settings(a).learnedEngine(game.path) ?: autoEngine(game)) else game.engine
        return if (wanted == Game.ENGINE_CLASSIC && classicAvailable(a)) GameActivity.ENGINE_CLASSIC else GameActivity.ENGINE_MODERN
    }

    /** The line under the title on the loading screen: "Essentials 17.2 · motor clásico". */
    fun info(game: Game, engine: String): String {
        val p = profile(game)
        val what = p.label.ifEmpty { game.kind.label }
        return what + " · motor " + if (engine == GameActivity.ENGINE_CLASSIC) "clásico" else "moderno"
    }

    /** The palette in use, for the game's own screens (loading screen, dialogs): bg, bg2, ink, muted, accent x3, onAccent, ok, warn. */
    private fun colors(a: Activity): IntArray {
        val night = (a.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val settings = Settings(a)
        val s = Palettes.scheme(settings.palette, settings.mode, night)
        return longArrayOf(s.bg, s.bg2, s.ink, s.muted, s.accent, s.accent2, s.accent3, s.onAccent, s.ok, s.warn).map { it.toInt() }.toIntArray()
    }

    /** The last time this game finished loading, from the times the game screen keeps on the device. */
    fun lastLoad(a: Activity, game: Game): LoadRecord? =
        runCatching { LoadTimes.lastFor(LoadTimes.parse(File(a.filesDir, "load_times.tsv").readText()), game.title) }.getOrNull()

    /**
     * When the game's process (":game") ended after [since] in an abnormal way, a sentence saying how Android saw it
     * (memory, native crash, signal...); null if it ended normally or Android (before 11) does not tell. A game that closes
     * "out of nowhere" leaves no Ruby error, so this is the only trace of why.
     */
    fun abnormalExit(a: Activity, since: Long): String? {
        if (android.os.Build.VERSION.SDK_INT < 30) return null
        val am = a.getSystemService(android.content.Context.ACTIVITY_SERVICE) as? android.app.ActivityManager ?: return null
        val infos = runCatching { am.getHistoricalProcessExitReasons(a.packageName, 0, 12) }.getOrNull().orEmpty()
        val i = infos.filter { it.processName == a.packageName + ":game" && it.timestamp >= since }.maxByOrNull { it.timestamp } ?: return null
        val reason = when (i.reason) {
            android.app.ApplicationExitInfo.REASON_EXIT_SELF -> "salió por sí mismo"
            android.app.ApplicationExitInfo.REASON_SIGNALED -> "lo terminó una señal del sistema"
            android.app.ApplicationExitInfo.REASON_LOW_MEMORY -> "el sistema lo cerró por poca memoria"
            android.app.ApplicationExitInfo.REASON_CRASH -> "excepción de Java"
            android.app.ApplicationExitInfo.REASON_CRASH_NATIVE -> "fallo del código nativo del motor"
            android.app.ApplicationExitInfo.REASON_ANR -> "dejó de responder"
            android.app.ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE -> "usó demasiados recursos"
            android.app.ApplicationExitInfo.REASON_USER_REQUESTED -> "lo cerraste tú"
            android.app.ApplicationExitInfo.REASON_USER_STOPPED -> "lo detuvo el usuario"
            else -> "motivo " + i.reason
        }
        val abnormal = when (i.reason) {
            android.app.ApplicationExitInfo.REASON_SIGNALED, android.app.ApplicationExitInfo.REASON_LOW_MEMORY,
            android.app.ApplicationExitInfo.REASON_CRASH, android.app.ApplicationExitInfo.REASON_CRASH_NATIVE,
            android.app.ApplicationExitInfo.REASON_ANR, android.app.ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE -> true
            android.app.ApplicationExitInfo.REASON_EXIT_SELF -> i.status != 0
            else -> false
        }
        if (!abnormal) return null
        return "Android dice: $reason (estado ${i.status}). ${i.description.orEmpty()} Memoria al cerrar: ${i.pss / 1024} MB (RSS ${i.rss / 1024} MB)."
    }

    /** The other engine, or null when it is not in this APK. */
    fun otherEngine(a: Activity, used: String): String? = when {
        used == GameActivity.ENGINE_CLASSIC -> GameActivity.ENGINE_MODERN
        classicAvailable(a) -> GameActivity.ENGINE_CLASSIC
        else -> null
    }

    private fun gameProcesses(a: Activity): List<android.app.ActivityManager.RunningAppProcessInfo> =
        (a.getSystemService(android.content.Context.ACTIVITY_SERVICE) as? android.app.ActivityManager)
            ?.runningAppProcesses.orEmpty().filter { it.processName == a.packageName + ":game" }

    /** True while the game's process still exists (it ends by itself a moment after the game closes). */
    fun gameRunning(a: Activity) = gameProcesses(a).isNotEmpty()

    /**
     * Ends the game's process and waits (at most 3 s) until it is gone. Ruby cannot start twice in one process, so a new
     * game must never start inside one that is still shutting down: that left the screen stuck. Blocking: call it off the main thread.
     */
    fun stopGame(a: Activity) {
        gameProcesses(a).forEach { android.os.Process.killProcess(it.pid) }
        val end = System.currentTimeMillis() + 3000
        while (gameRunning(a) && System.currentTimeMillis() < end) Thread.sleep(80)
    }

    fun scanTranslation(a: Activity, game: Game, catalog: File) {
        val dir = File(game.path)
        // The scan never executes the game or initializes its save directory.
        val intent = GameActivity.intent(a, dir, game.title, dir, engineFor(a, game),
            GameActivity.SCREEN_ORIGINAL, Storage.rtpRoot(), false, "Preparar traducción", colors(a))
            .putExtra(GameActivity.EXTRA_TRANSLATION_CATALOG, catalog.absolutePath)
        a.startActivity(intent)
    }

    /** Opens the game; [override] forces one engine (the automatic retry). Returns the engine it started with. */
    fun play(a: Activity, game: Game, override: String? = null, launchAt: Long = 0, skipTranslationPreparation: Boolean = false): String {
        val dir = File(game.path)
        val saves = saveDir(game)
        val engine = override ?: engineFor(a, game)
        Settings(a).setLastEngine(game.path, engine)
        val cover = Covers(a) { false }.bestFile(game)?.absolutePath.orEmpty()
        val st = Settings(a)
        val intent = GameActivity.intent(a, dir, game.title, saves, engine,
            when (st.screen(game.path)) {
                "wide" -> GameActivity.SCREEN_WIDE_FULL
                "wide_safe" -> GameActivity.SCREEN_WIDE
                else -> GameActivity.SCREEN_ORIGINAL
            },
            Storage.rtpRoot(), game.touch, info(game, engine), colors(a))
            .putExtra(GameActivity.EXTRA_LAUNCH_AT, launchAt)
            .putExtra(GameActivity.EXTRA_SKIP_TR_PREPARATION, skipTranslationPreparation)
            .putExtra(GameActivity.EXTRA_PAD, st.pad(game.path))
            .putExtra(GameActivity.EXTRA_COVER, cover)
            .putExtra(GameActivity.EXTRA_FILTER, if (st.retro(game.path)) "crt" else "")
            .putExtra(GameActivity.EXTRA_TILT, st.tilt(game.path))
        // The private memory is available even when the selected provider has no key.
        if (st.translateOn && st.translateGame(game.path)) {
            intent.putExtra(GameActivity.EXTRA_TR_DIR, Translations.dirFor(a, game).absolutePath)
                .putExtra(GameActivity.EXTRA_TR_PROVIDER, st.translateProvider)
                .putExtra(GameActivity.EXTRA_TR_TARGET, st.translateTarget)
                .putExtra(GameActivity.EXTRA_TR_KEY, st.deeplKey)
        }
        a.startActivity(intent)
        return engine
    }
}

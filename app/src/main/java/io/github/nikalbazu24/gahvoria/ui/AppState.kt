package io.github.nikalbazu24.gahvoria.ui

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.nikalbazu24.gahvoria.core.Game
import io.github.nikalbazu24.gahvoria.core.GameInspector
import io.github.nikalbazu24.gahvoria.core.Inspection
import io.github.nikalbazu24.gahvoria.core.Library
import io.github.nikalbazu24.gahvoria.core.Release
import io.github.nikalbazu24.gahvoria.core.Shortcuts
import io.github.nikalbazu24.gahvoria.core.ThemeMode
import io.github.nikalbazu24.gahvoria.BuildConfig
import io.github.nikalbazu24.gahvoria.core.LoadRecord
import io.github.nikalbazu24.gahvoria.data.AppUpdater
import io.github.nikalbazu24.gahvoria.data.Covers
import io.github.nikalbazu24.gahvoria.data.GameReport
import io.github.nikalbazu24.gahvoria.data.Launcher
import io.github.nikalbazu24.gahvoria.data.Settings
import io.github.nikalbazu24.gahvoria.data.Storage
import java.io.File
import java.util.concurrent.Executors

enum class Tab { LIBRARY, ADD, UPDATES, SETTINGS }

enum class NoticeKind { INFO, OK, WARN, ERROR }

/** A short message in the app's style. With an action it stays longer (8 s instead of 4 s). */
data class Notice(val text: String, val kind: NoticeKind = NoticeKind.INFO, val action: String? = null, val onAction: (() -> Unit)? = null)

/** What the add flow found in the chosen folder. */
data class Found(val dir: File, val inspection: Inspection)

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data object UpToDate : UpdateState
    data object Offline : UpdateState
    data class Available(val release: Release) : UpdateState
    data class Downloading(val release: Release, val progress: Float) : UpdateState
    data class Ready(val release: Release, val apk: File) : UpdateState
    data class Failed(val message: String) : UpdateState
}

/** Everything the screens show, in one place. Slow work runs on a background thread. */
/** An error a game left, ready to show: the game, the report text and the plain-words diagnosis. */
data class ErrorReport(val game: Game, val text: String, val diagnosis: io.github.nikalbazu24.gahvoria.core.Diagnosis)

class AppState(private val activity: Activity) {
    val settings = Settings(activity)
    private val library = Library(File(activity.filesDir, "library.tsv"))
    private val worker = Executors.newSingleThreadExecutor()

    var tab by mutableStateOf(Tab.LIBRARY)
    /** The game whose options are open (null: none). */
    var optionsFor by mutableStateOf<Game?>(null)
    var games by mutableStateOf(Library.ordered(library.load()))
        private set
    var selected by mutableIntStateOf(0)

    var palette by mutableStateOf(settings.palette)
        private set
    var mode by mutableStateOf(settings.mode)
        private set
    var hasAccess by mutableStateOf(Storage.hasAccess(activity))
        private set

    var found by mutableStateOf<Found?>(null)
    var addMessage by mutableStateOf<String?>(null)
    var update by mutableStateOf<UpdateState>(UpdateState.Idle)
        private set
    var autoCheck by mutableStateOf(settings.autoCheck)
        private set
    var lastCheck by mutableStateOf(settings.lastCheck)
        private set
    val installedCode = AppUpdater.installedCode(activity)
    var notice by mutableStateOf<Notice?>(null)
    /** The welcome animation: once per app start, never when coming back from a game. */
    var splash by mutableStateOf(!splashDone && !settings.hasSession())
        private set

    fun endSplash() { splash = false; splashDone = true }

    /** Screens of the introduction or of a big update's changes, shown over the app (null: none). */
    var intro by mutableStateOf<List<io.github.nikalbazu24.gahvoria.core.Slide>?>(
        io.github.nikalbazu24.gahvoria.core.Intro.pending(settings.introSeen, settings.seenUpdate, games.isNotEmpty()))
    /** True while showing the changes of an update (the title says "Novedades"), false for the welcome. */
    val introIsUpdate get() = intro != null && intro !== io.github.nikalbazu24.gahvoria.core.Intro.WELCOME

    fun finishIntro() {
        settings.introSeen = true
        settings.seenUpdate = io.github.nikalbazu24.gahvoria.core.Intro.LATEST
        intro = null
    }

    fun showIntro() { intro = io.github.nikalbazu24.gahvoria.core.Intro.WELCOME }

    /** The game that closed with an error, shown as a window over the blurred library until the player closes it. */
    var errorReport by mutableStateOf<ErrorReport?>(null)
    val canSendReports get() = GameReport.canSend()
    var coversOnline by mutableStateOf(settings.coversOnline)
        private set
    val covers = Covers(activity) { settings.coversOnline }
    /** Goes up each time a cover changes, so the screens that draw covers look again. */
    var coverVersion by mutableIntStateOf(0)
        private set
    var shortcutsOn by mutableStateOf(settings.shortcutsFolder != null)
        private set
    /** Where the shortcuts live (null: not created yet). */
    var shortcutsPath by mutableStateOf(settings.shortcutsFolder)
        private set

    fun say(text: String, kind: NoticeKind = NoticeKind.INFO, action: String? = null, onAction: (() -> Unit)? = null) {
        notice = Notice(text, kind, action, onAction)
    }

    fun choosePalette(id: String) { settings.palette = id; palette = id }
    fun chooseMode(m: ThemeMode) { settings.mode = m; mode = m }
    fun chooseAutoCheck(on: Boolean) { settings.autoCheck = on; autoCheck = on }

    fun chooseCoversOnline(on: Boolean) {
        settings.coversOnline = on; coversOnline = on
        if (on) refreshCovers()
    }

    /** Two steps, so the library never waits: images from the game folders first (no network), then the official ones. */
    fun refreshCovers() {
        val list = games
        worker.execute {
            covers.prune(list)
            var changed = false
            for (g in list) if (covers.refreshLocal(g)) changed = true
            if (changed) bumpCovers()
            for (g in list) if (covers.refreshOnline(g)) bumpCovers()
        }
    }

    private fun bumpCovers() = activity.runOnUiThread { coverVersion++ }

    /** Back from the game or from Android's settings. */
    fun onResume() {
        hasAccess = Storage.hasAccess(activity)
        settings.takeSession()?.let { (path, at) ->
            val played = System.currentTimeMillis() - at
            // Sessions longer than 12 h are not counted: the game was probably left in the background.
            if (played in 1..12L * 3600_000) library.update(path) { it.copy(playMs = it.playMs + played) }
            val played0 = library.load().firstOrNull { it.path == path }
            GameReport.errorSince(activity, at, File(path), played0?.let { saveDir(it) })?.let { raw ->
                if (played0 != null) {
                    val err = GameReport.withScript(File(path), raw)
                    val modern = Launcher.engineFor(activity, played0) != "classic"
                    val d = io.github.nikalbazu24.gahvoria.core.ErrorDiagnosis.diagnose(err, modern, classicAvailable())
                    errorReport = ErrorReport(played0, err, d)
                }
            }
        }
        games = Library.ordered(library.load())
        if (selected >= games.size) selected = 0
        if (autoCheck && System.currentTimeMillis() - lastCheck > 6L * 3600_000 && update is UpdateState.Idle) checkUpdate()
        refreshCovers()
    }

    fun askAccess() = Storage.askAccess(activity)

    // ---------------------------------------------------------------- library

    fun play(game: Game) {
        val dir = File(game.path)
        if (!hasAccess) { say("Gahvoria necesita acceso a tus archivos para abrir juegos.", NoticeKind.WARN, "Dar acceso") { askAccess() }; return }
        if (!dir.isDirectory) { say("No encuentro la carpeta de ${game.title}. ¿La moviste o borraste?", NoticeKind.ERROR); return }
        if (!game.kind.isRpgMaker) { say("Este motor todavía no se puede jugar en Gahvoria.", NoticeKind.WARN); return }
        val now = System.currentTimeMillis()
        library.update(game.path) { it.copy(lastPlayed = now) }
        settings.startSession(game.path, now)
        Launcher.play(activity, game)
        selected = 0
    }

    fun changeGame(path: String, change: (Game) -> Game) {
        games = Library.ordered(library.update(path, change))
        optionsFor = games.firstOrNull { it.path == path }
    }

    /** Only the entry: the folder and the saves stay where they are. */
    fun forget(game: Game) {
        games = Library.ordered(library.remove(game.path))
        optionsFor = null
        selected = 0
        say("${game.title} ya no está en tu biblioteca. Sus archivos y partidas siguen en su carpeta.")
    }

    fun classicAvailable() = Launcher.classicAvailable(activity)
    fun profile(game: Game) = Launcher.profile(game)
    fun lastLoad(game: Game): LoadRecord? = Launcher.lastLoad(activity, game)

    fun saveDir(game: Game) = Launcher.saveDir(game)

    /** The diagnosis said the classic engine may fix it: switch this game to it and open it again. */
    fun retryClassic(e: ErrorReport) {
        errorReport = null
        changeGame(e.game.path) { it.copy(engine = Game.ENGINE_CLASSIC) }
        games.firstOrNull { it.path == e.game.path }?.let { play(it) }
    }

    /** Sends (or copies, if this build cannot send) the last error this game left, whenever it happened. */
    fun lastReport(game: Game) = GameReport.latest(activity, File(game.path), saveDir(game))
        ?.let { (at, text) -> at to GameReport.withScript(File(game.path), text) }

    fun sendReport(game: Game, text: String) {
        val report = "Gahvoria ${BuildConfig.VERSION_NAME}\nJuego: ${game.title}\n\n$text"
        if (!GameReport.canSend()) {
            GameReport.copy(activity, report)
            say("Informe copiado. Pégalo donde quieras enviarlo.", NoticeKind.OK)
            return
        }
        say("Enviando informe…", NoticeKind.INFO)
        worker.execute {
            val ok = runCatching { GameReport.send("informe-" + game.title, report) }
            activity.runOnUiThread {
                ok.onSuccess { say("Informe enviado. Gracias.", NoticeKind.OK) }
                    .onFailure { say("No se pudo enviar: " + (it.message ?: "sin conexión") + ".", NoticeKind.WARN) }
            }
        }
    }

    // ---------------------------------------------------------------- add

    fun inspectFolder(dir: File?) {
        found = null
        addMessage = null
        if (dir == null) { addMessage = "No pude leer esa carpeta. Prueba con otra."; return }
        if (!hasAccess) { addMessage = "Primero da acceso a tus archivos."; return }
        worker.execute {
            val i = GameInspector.inspect(dir)
            activity.runOnUiThread { found = Found(dir, i) }
        }
    }

    fun addFound() {
        val f = found ?: return
        val g = Game(f.dir.absolutePath, f.inspection.title, f.inspection.kind, f.inspection.essentials, addedAt = System.currentTimeMillis())
        games = Library.ordered(library.add(g))
        selected = games.indexOfFirst { it.path == g.path }.coerceAtLeast(0)
        found = null
        addMessage = null
        tab = Tab.LIBRARY
        say("${g.title} está en tu biblioteca.", NoticeKind.OK)
        refreshShortcuts()
        refreshCovers()
    }

    // ---------------------------------------------------------------- other launchers

    /** One shortcut file per game in ROMs/Gahvoria, for Díptico, Daijishō, ES-DE and the like. */
    fun createShortcuts(target: File? = null) {
        if (!hasAccess) { say("Primero da acceso a tus archivos.", NoticeKind.WARN, "Dar acceso") { askAccess() }; return }
        val folder = target ?: settings.shortcutsFolder?.let { File(it) } ?: File(Storage.root(), Shortcuts.FOLDER)
        val list = games
        worker.execute {
            val n = Shortcuts.export(list, folder)
            activity.runOnUiThread {
                if (n > 0) { settings.shortcutsFolder = folder.absolutePath; shortcutsOn = true; shortcutsPath = folder.absolutePath }
                if (n > 0) say("Creé $n accesos en ${folder.absolutePath}. Apunta tu launcher a esa carpeta.", NoticeKind.OK)
                else say("Todavía no hay juegos que se puedan jugar para crear accesos.", NoticeKind.WARN)
            }
        }
    }

    /** New games get their shortcut too, once the player has created them. Nothing is ever deleted. */
    private fun refreshShortcuts() {
        val folder = settings.shortcutsFolder ?: return
        if (!hasAccess) return
        val list = games
        worker.execute { Shortcuts.export(list, File(folder)) }
    }

    // ---------------------------------------------------------------- updates

    fun checkUpdate() {
        update = UpdateState.Checking
        worker.execute {
            val r = AppUpdater.latest()
            val now = System.currentTimeMillis()
            activity.runOnUiThread {
                settings.lastCheck = now; lastCheck = now
                update = when {
                    r == null -> UpdateState.Offline
                    r.isNewerThan(installedCode) -> UpdateState.Available(r)
                    else -> UpdateState.UpToDate
                }
            }
        }
    }

    fun downloadUpdate(r: Release) {
        if (!AppUpdater.canInstall(activity)) {
            say("Android pedirá permiso para que Gahvoria instale sus actualizaciones. Actívalo y vuelve.", NoticeKind.INFO)
            AppUpdater.askInstallPermission(activity)
            return
        }
        update = UpdateState.Downloading(r, 0f)
        worker.execute {
            val result = runCatching {
                AppUpdater.download(activity, r) { p -> activity.runOnUiThread { update = UpdateState.Downloading(r, p) } }
            }
            activity.runOnUiThread {
                update = result.fold({ UpdateState.Ready(r, it) }, { UpdateState.Failed(it.message ?: "No se pudo descargar") })
                (update as? UpdateState.Ready)?.let { AppUpdater.install(activity, it.apk) }
            }
        }
    }

    fun installReady(s: UpdateState.Ready) = AppUpdater.install(activity, s.apk)
}

/** Survives the screen turning (the activity is recreated) but not the app closing: the animation shows once per start. */
private var splashDone = false

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
import io.github.nikalbazu24.gahvoria.data.AppUpdater
import io.github.nikalbazu24.gahvoria.data.Launcher
import io.github.nikalbazu24.gahvoria.data.Settings
import io.github.nikalbazu24.gahvoria.data.Storage
import java.io.File
import java.util.concurrent.Executors

enum class Tab { LIBRARY, ADD, UPDATES, SETTINGS }

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
    var notice by mutableStateOf<String?>(null)
    var shortcutsOn by mutableStateOf(settings.shortcutsFolder != null)
        private set

    fun choosePalette(id: String) { settings.palette = id; palette = id }
    fun chooseMode(m: ThemeMode) { settings.mode = m; mode = m }
    fun chooseAutoCheck(on: Boolean) { settings.autoCheck = on; autoCheck = on }

    /** Back from the game or from Android's settings. */
    fun onResume() {
        hasAccess = Storage.hasAccess(activity)
        settings.takeSession()?.let { (path, at) ->
            val played = System.currentTimeMillis() - at
            // Sessions longer than 12 h are not counted: the game was probably left in the background.
            if (played in 1..12L * 3600_000) library.update(path) { it.copy(playMs = it.playMs + played) }
        }
        games = Library.ordered(library.load())
        if (selected >= games.size) selected = 0
        if (autoCheck && System.currentTimeMillis() - lastCheck > 6L * 3600_000 && update is UpdateState.Idle) checkUpdate()
    }

    fun askAccess() = Storage.askAccess(activity)

    // ---------------------------------------------------------------- library

    fun play(game: Game) {
        val dir = File(game.path)
        if (!hasAccess) { notice = "Gahvoria necesita acceso a tus archivos para abrir juegos."; return }
        if (!dir.isDirectory) { notice = "No encuentro la carpeta de ${game.title}. ¿La moviste o borraste?"; return }
        if (!game.kind.isRpgMaker) { notice = "Este motor todavía no se puede jugar en Gahvoria."; return }
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
        notice = "${game.title} ya no está en tu biblioteca. Sus archivos y partidas siguen en su carpeta."
    }

    fun classicAvailable() = Launcher.classicAvailable(activity)
    fun profile(game: Game) = Launcher.profile(game)

    fun saveDir(game: Game) = Launcher.saveDir(game)

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
        notice = "${g.title} está en tu biblioteca."
        refreshShortcuts()
    }

    // ---------------------------------------------------------------- other launchers

    /** One shortcut file per game in ROMs/Gahvoria, for Díptico, Daijishō, ES-DE and the like. */
    fun createShortcuts() {
        if (!hasAccess) { notice = "Primero da acceso a tus archivos."; return }
        val folder = File(Storage.root(), Shortcuts.FOLDER)
        val list = games
        worker.execute {
            val n = Shortcuts.export(list, folder)
            activity.runOnUiThread {
                if (n > 0) { settings.shortcutsFolder = folder.absolutePath; shortcutsOn = true }
                notice = if (n > 0) "Creé $n accesos en ${Shortcuts.FOLDER}. Tu launcher ya puede abrir esos juegos con Gahvoria."
                else "Todavía no hay juegos que se puedan jugar para crear accesos."
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
            notice = "Android pedirá permiso para que Gahvoria instale sus actualizaciones. Actívalo y vuelve."
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

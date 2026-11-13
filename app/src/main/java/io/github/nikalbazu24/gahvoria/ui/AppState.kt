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
import io.github.nikalbazu24.gahvoria.core.LoadTimes
import io.github.nikalbazu24.gahvoria.data.AppUpdater
import io.github.nikalbazu24.gahvoria.data.Covers
import io.github.nikalbazu24.gahvoria.data.GameReport
import io.github.nikalbazu24.gahvoria.data.Launcher
import io.github.nikalbazu24.gahvoria.data.Settings
import io.github.nikalbazu24.gahvoria.data.Storage
import io.github.nikalbazu24.gahvoria.data.Translations
import io.github.nikalbazu24.gahvoria.data.TranslationPreparationService
import android.net.Uri
import android.content.Intent
import io.github.nikalbazu24.gahvoria.core.StoreCatalog
import io.github.nikalbazu24.gahvoria.core.StoreGame
import io.github.nikalbazu24.gahvoria.core.ZipInstaller
import io.github.nikalbazu24.gahvoria.core.GameRemoval
import io.github.nikalbazu24.gahvoria.core.SaveBackup
import java.io.File
import java.util.concurrent.Executors

enum class Tab { LIBRARY, ADD, STORE, UPDATES, SETTINGS }

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
    /** The chosen channel has no published version yet (only the stable one can be in this state). */
    data object NoStable : UpdateState
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
    var iconFollows by mutableStateOf(settings.iconFollows)
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

    /** The icon follows when Gahvoria goes to the background (see [syncIcon]), never while it is open. */
    fun choosePalette(id: String) { settings.palette = id; palette = id }
    fun chooseIconFollows(on: Boolean) {
        settings.iconFollows = on; iconFollows = on
        if (on) say("El icono cambiará cuando salgas de Gahvoria. Si un acceso que pusiste a mano en el inicio desaparece, agrégalo de nuevo.")
    }
    /** When Gahvoria goes to the background: make the launcher icon match the palette (does nothing when it already does). */
    fun syncIcon() { worker.execute { io.github.nikalbazu24.gahvoria.data.AppIcon.sync(activity, settings.palette, settings.iconFollows) } }
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

    private var retryWith: Pair<Game, String>? = null

    /** Back from the game or from Android's settings. */
    fun onResume() {
        hasAccess = Storage.hasAccess(activity)
        translationsVersion++
        settings.takeSession()?.let { (path, at) ->
            val interruptedPreparation = com.hatkid.mkxpz.GameActivity.translationPreparationMarker(activity, at).delete()
            val played = System.currentTimeMillis() - at
            // Sessions longer than 12 h are not counted: the game was probably left in the background.
            if (played in 1..12L * 3600_000) library.update(path) { it.copy(playMs = it.playMs + played) }
            val played0 = library.load().firstOrNull { it.path == path }
            val rubyError = GameReport.errorSince(activity, at, File(path), played0?.let { saveDir(it) })
            // No Ruby error but the game's process died in an abnormal way (memory, native crash...): ask Android why.
            val exitNote = if (rubyError == null && played0 != null) Launcher.abnormalExit(activity, at) else null
            val errRaw: String? = rubyError ?: exitNote?.let {
                "Gahvoria ${BuildConfig.VERSION_NAME}\nJuego: ${played0?.title}\n\nEl proceso del juego terminó de forma inesperada.\n$it"
            }
            val usedEngine = settings.lastEngine(path)
            val auto = played0 != null && played0.engine == Game.ENGINE_AUTO
            // A creator's own game keeps the history of its errors, including the ones that were retried.
            if (errRaw != null && played0 != null && settings.mine(path))
                runCatching { creatorLog.add(creatorLog.key(path, played0.title), System.currentTimeMillis(), GameReport.withScript(File(path), errRaw)) }
            if (interruptedPreparation && errRaw != null && played0 != null && usedEngine != null) {
                say("La preparación de traducción se interrumpió. Reabro el juego con lo que ya está traducido.", NoticeKind.WARN)
                play(played0, usedEngine, force = true, skipTranslationPreparation = true)
            } else if (errRaw == null) {
                // It ran: for "automatic" games remember the engine that worked, so the next start uses it directly.
                if (!interruptedPreparation && auto && usedEngine != null && played >= 20_000) settings.setLearnedEngine(path, usedEngine)
                settings.setRetried(path, false)
            } else if (played0 != null) {
                val other = usedEngine?.let { Launcher.otherEngine(activity, it) }
                // A missing file is not fixed by the other engine: reopening would only close the game a second time.
                val engineFixable = !errRaw.contains("No such file or directory", ignoreCase = true) &&
                    (other != Game.ENGINE_CLASSIC || Launcher.profile(played0).clues.modern.isEmpty())
                // Failed while starting (first 90 s), or with the classic Ruby 1.8 / modern Ruby 3 clash at any moment (Pokémon Ópalo: after the new game).
                val clash = io.github.nikalbazu24.gahvoria.core.ErrorDiagnosis.engineClash(errRaw, (usedEngine ?: "modern") != "classic")
                if (!interruptedPreparation && auto && other != null && engineFixable && !settings.retried(path) && (played < 90_000 || clash)) {
                    // Failed while starting: reopen once with the other engine, without asking.
                    settings.setRetried(path, true)
                    settings.setLearnedEngine(path, null)
                    val name = if (other == "classic") "clásico" else "moderno"
                    say("${played0.title} falló al abrir. Lo vuelvo a abrir con el motor $name.", NoticeKind.WARN)
                    retryWith = played0 to other
                } else {
                    settings.setRetried(path, false)
                    val err = GameReport.withScript(File(path), errRaw)
                    val modern = (usedEngine ?: Launcher.engineFor(activity, played0)) != "classic"
                    val d = io.github.nikalbazu24.gahvoria.core.ErrorDiagnosis.diagnose(err, modern, classicAvailable())
                    errorReport = ErrorReport(played0, err, d)
                }
            }
        }
        retryWith?.let { (g, e) -> retryWith = null; play(g, e) }
        games = Library.ordered(library.load())
        if (selected >= games.size) selected = 0
        if (autoCheck && System.currentTimeMillis() - lastCheck > 6L * 3600_000 && update is UpdateState.Idle) checkUpdate()
        refreshCovers()
    }

    fun askAccess() = Storage.askAccess(activity)

    // ---------------------------------------------------------------- library

    fun play(game: Game, engine: String? = null, force: Boolean = false, skipTranslationPreparation: Boolean = false) {
        val dir = File(game.path)
        if (!hasAccess) { say("Gahvoria necesita acceso a tus archivos para abrir juegos.", NoticeKind.WARN, "Dar acceso") { askAccess() }; return }
        if (!dir.isDirectory) { say("No encuentro la carpeta de ${game.title}. ¿La moviste o borraste?", NoticeKind.ERROR); return }
        if (!game.kind.isRpgMaker) { say("Este motor todavía no se puede jugar en Gahvoria.", NoticeKind.WARN); return }
        // Before opening: a game missing files of its Data folder closes by itself at start-up (Solar Eclipse, copied or unpacked halfway).
        if (!force) {
            val missing = io.github.nikalbazu24.gahvoria.core.GameCheck.missingData(dir)
            if (missing.isNotEmpty()) {
                say("${game.title} no se abrirá: le faltan ${missing.joinToString(", ") { "$it.rxdata" }} en Data. Suele pasar si se descomprimió a medias; vuelve a instalarlo desde su archivo original.",
                    NoticeKind.ERROR, "Abrir igual") { play(game, engine, force = true, skipTranslationPreparation = skipTranslationPreparation) }
                return
            }
        }
        val preparing = TranslationPreparationService.active
        Thread({
            if (!TranslationPreparationService.pauseAndWait()) {
                activity.runOnUiThread { say("La traducción está guardando el último lote. Vuelve a abrir el juego en unos segundos.", NoticeKind.WARN) }
                return@Thread
            }
            if (Launcher.gameRunning(activity)) Launcher.stopGame(activity)
            runCatching { Launcher.profile(game) }
            activity.runOnUiThread {
                val now = System.currentTimeMillis()
                library.update(game.path) { it.copy(lastPlayed = now) }
                settings.startSession(game.path, now)
                if (engine == null) settings.setRetried(game.path, false)
                val status = preparationStatus(game)
                val prepared = translateTarget == "es" && status != null && status.total > 0 && status.reviewed == status.total
                Launcher.play(activity, game, engine, now, skipTranslationPreparation || preparing || prepared)
            }
        }, "GahvoriaPlay").start()
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
    /** The app itself closed on its own last time: say so and offer to send what Android recorded (it is the player's choice). */
    fun offerCrashReport(text: String) {
        if (!GameReport.canSend()) { say("Gahvoria se cerró la última vez, pero esta versión no puede enviar informes.", NoticeKind.WARN); return }
        say("Gahvoria se cerró sola la última vez.", NoticeKind.WARN, "Enviar informe") {
            Thread({
                val ok = runCatching { GameReport.send("app-cierre", text) }.isSuccess
                activity.runOnUiThread { say(if (ok) "Informe enviado. Gracias." else "No se pudo enviar el informe.", if (ok) NoticeKind.OK else NoticeKind.WARN) }
            }, "GahvoriaCrashReport").start()
        }
    }

    /** Goes up when a game's scripts have been read in the background, so the screens that show them look again. */
    var profileTick by mutableIntStateOf(0)
        private set
    private val profilesLoading = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()

    /**
     * What a game's scripts say. Reading them (several MB for a big game) is done in a background thread: until it is ready the
     * screens get an empty profile and draw again when it arrives, so opening a screen never freezes the app.
     */
    fun profile(game: Game): io.github.nikalbazu24.gahvoria.core.GameProfile {
        profileTick  // read: whoever draws with this profile is told when it changes
        io.github.nikalbazu24.gahvoria.core.GameProfile.cached(File(game.path))?.let { return it }
        if (profilesLoading.add(game.path)) Thread({
            runCatching { Launcher.profile(game) }
            profilesLoading.remove(game.path)
            activity.runOnUiThread { profileTick++ }
        }, "GahvoriaProfile").start()
        return io.github.nikalbazu24.gahvoria.core.GameProfile()
    }
    fun lastLoad(game: Game): LoadRecord? = Launcher.lastLoad(activity, game)

    fun saveDir(game: Game) = Launcher.saveDir(game)

    /** The player's arrangement of the touch controls for a game. */
    fun padFor(game: Game) = io.github.nikalbazu24.gahvoria.core.TouchLayout.decode(settings.pad(game.path))

    fun savePad(game: Game, layout: io.github.nikalbazu24.gahvoria.core.TouchLayout) = settings.setPad(game.path, layout.encode())

    // ---------------------------------------------------------------- translation, screen

    var translateOn by mutableStateOf(settings.translateOn)
    var translateProvider by mutableStateOf(settings.translateProvider)
    var translateTarget by mutableStateOf(settings.translateTarget)
    var hasDeeplKey by mutableStateOf(settings.deeplKey.isNotEmpty())
    /** Bumps to refresh the sizes shown in Ajustes after clearing. */
    var translationsVersion by mutableIntStateOf(0)

    fun chooseTranslateOn(on: Boolean) { settings.translateOn = on; translateOn = on }
    fun chooseTranslateProvider(p: String) { settings.translateProvider = p; translateProvider = p }
    fun chooseTranslateTarget(t: String) { settings.translateTarget = t; translateTarget = t }
    /** What happened when the DeepL key was checked, shown under the key field. */
    var deeplState by mutableStateOf<String?>(null)

    /** Saves the key and asks DeepL if it works, so the player knows right away. */
    fun saveDeeplKey(key: String) {
        settings.deeplKey = key; hasDeeplKey = key.isNotBlank()
        if (key.isBlank()) { deeplState = null; return }
        deeplState = "Comprobando la clave…"
        worker.execute {
            val r = io.github.nikalbazu24.gahvoria.data.Deepl.verify(key.trim())
            activity.runOnUiThread {
                deeplState = r.message
                say(r.message, if (r.ok) NoticeKind.OK else if (r.saved) NoticeKind.WARN else NoticeKind.ERROR)
            }
        }
    }
    var startingPreparation by mutableStateOf(false)
        private set

    fun preparationStatus(g: Game) = TranslationPreparationService.status(Translations.catalog(activity, g))

    fun prepareTranslation(g: Game) {
        if (preparationStatus(g)?.active == true) {
            TranslationPreparationService.pause()
            return
        }
        if (startingPreparation || TranslationPreparationService.active) {
            say("Ya hay una traducción en preparación.", NoticeKind.WARN)
            return
        }
        if (!g.kind.isRpgMaker || !hasAccess || !File(g.path).isDirectory) {
            say("Necesito acceso a la carpeta RPG Maker de este juego para preparar su traducción.", NoticeKind.WARN)
            return
        }
        if (translateTarget != "es") {
            say("La preparación Neural está disponible en español. Elige español en Ajustes → Traducción.", NoticeKind.WARN)
            return
        }
        startingPreparation = true
        val base = Translations.catalog(activity, g)
        worker.execute {
            val result = runCatching {
                val dir = base.parentFile ?: error("Falta la carpeta de traducción")
                if (!dir.mkdirs() && !dir.isDirectory) error("No pude crear la memoria de traducción")
                val revisionFile = File(base.path + ".revision")
                val revision = Translations.catalogRevision(g)
                val ready = File(base.path + ".ready")
                val oldRevision = runCatching { revisionFile.readText() }.getOrNull()
                if (oldRevision != revision) {
                    if (ready.exists() && !ready.delete()) error("No pude renovar el catálogo")
                    val opening = File(dir, "opening-es.txt")
                    if (opening.exists() && !opening.delete()) error("No pude renovar los textos de inicio")
                    File(base.path + ".limited").delete()
                    File(base.path + ".status").delete()
                    revisionFile.writeText(revision)
                }
                if (!ready.isFile) Launcher.profile(g)
                for (suffix in listOf(".error", ".skip")) {
                    val marker = File(base.path + suffix)
                    if (marker.exists() && !marker.delete()) error("No pude reanudar la lectura")
                }
                !ready.isFile
            }
            activity.runOnUiThread {
                startingPreparation = false
                val needsScan = result.getOrElse {
                    say(it.message ?: "No pude preparar la traducción.", NoticeKind.ERROR)
                    return@runOnUiThread
                }
                if (activity.isFinishing || activity.isDestroyed) return@runOnUiThread
                if (Launcher.gameRunning(activity)) {
                    say("Cierra el juego para preparar su traducción.", NoticeKind.WARN)
                    return@runOnUiThread
                }
                if (!TranslationPreparationService.start(activity, base, g.title)) {
                    say("No pude iniciar la preparación en segundo plano.", NoticeKind.ERROR)
                    return@runOnUiThread
                }
                chooseTranslateOn(true)
                setTranslateGame(g, true)
                if (needsScan) runCatching { Launcher.scanTranslation(activity, g, base) }.onFailure {
                    TranslationPreparationService.pause()
                    say("No pude abrir el lector del juego.", NoticeKind.ERROR)
                }
            }
        }
    }

    fun translateGame(g: Game) = settings.translateGame(g.path)
    fun setTranslateGame(g: Game, on: Boolean) { settings.setTranslateGame(g.path, on); coverVersion++ }
    fun screenMode(g: Game) = settings.screen(g.path)
    fun setScreenMode(g: Game, mode: String) { settings.setScreen(g.path, mode); coverVersion++ }
    fun retro(g: Game) = settings.retro(g.path)
    fun setRetro(g: Game, on: Boolean) { settings.setRetro(g.path, on); coverVersion++ }
    fun tilt(g: Game) = settings.tilt(g.path)
    fun setTilt(g: Game, deg: Int) = settings.setTilt(g.path, deg)
    fun translationsTotal() = io.github.nikalbazu24.gahvoria.data.Translations.total(activity)
    fun translateStatus() = io.github.nikalbazu24.gahvoria.data.Translations.status(activity)
    fun clearTranslations() {
        if (startingPreparation || TranslationPreparationService.active) {
            say("Pausa la preparación antes de borrar las traducciones.", NoticeKind.WARN)
            return
        }
        TranslationPreparationService.forgetStatus()
        io.github.nikalbazu24.gahvoria.data.Translations.clearAll(activity)
        translationsVersion++
        say("Borré las traducciones guardadas. Tus juegos y partidas no se tocaron.", NoticeKind.OK)
    }

    // ---------------------------------------------------------------- delete and backup

    /** The game being deleted for good (the two confirmations are shown while this is set). */
    var deleting by mutableStateOf<Game?>(null)

    /** Saves found in a backup or another emulator's folder, waiting for the player to decide how to import them. */
    data class ImportOffer(val game: Game, val items: List<SaveBackup.Item>, val conflicts: List<String>, val source: String)
    var importOffer by mutableStateOf<ImportOffer?>(null)

    fun deletePlan(g: Game) = GameRemoval.plan(g, saveDir(g), settings.shortcutsFolder?.let { File(it) }, Storage.root(),
        games.filter { it.path != g.path }.map { it.path })

    /** Deletes the game's folder, saves, shortcuts, cover and what Gahvoria kept about it. Only after both confirmations. */
    fun deleteGame(g: Game) {
        if (startingPreparation || preparationStatus(g)?.active == true) {
            deleting = null
            say("Pausa la preparación antes de borrar el juego.", NoticeKind.WARN)
            return
        }
        TranslationPreparationService.forgetStatus()
        val plan = deletePlan(g)
        deleting = null
        worker.execute {
            val r = GameRemoval.execute(plan)
            runCatching { io.github.nikalbazu24.gahvoria.data.Translations.clear(activity, g) }
            runCatching {
                val f = File(activity.filesDir, "load_times.tsv")
                if (f.isFile) f.writeText(f.readLines().filterNot { it.split('\t').getOrNull(2) == g.title }.joinToString("") { it + "\n" })
            }
            activity.runOnUiThread {
                games = Library.ordered(library.remove(g.path))
                settings.forgetGame(g.path)
                covers.prune(games)
                optionsFor = null
                selected = 0
                coverVersion++
                if (r.failed.isEmpty() && plan.deletesFolder) say("${g.title} se borró por completo.", NoticeKind.OK)
                else if (!plan.deletesFolder) say("${g.title} salió de la biblioteca y se borraron sus partidas y accesos, pero conservé su carpeta: ${plan.folderRefusal}", NoticeKind.WARN)
                else say("${g.title} se borró, pero ${r.failed.size} archivos no se pudieron eliminar.", NoticeKind.WARN)
            }
        }
    }

    /** Writes the game's saves into a zip the player chose. */
    fun exportSaves(g: Game, uri: Uri, then: (Boolean) -> Unit = {}) {
        val dir = saveDir(g)
        worker.execute {
            val n = runCatching { activity.contentResolver.openOutputStream(uri)!!.use { SaveBackup.export(dir, it, g.title) } }.getOrNull()
            activity.runOnUiThread {
                if (n == null) say("No pude guardar el respaldo.", NoticeKind.ERROR)
                else say(if (n == 0) "Respaldo creado, pero ${g.title} todavía no tiene partidas." else "Respaldo de $n partidas guardado.", NoticeKind.OK)
                then(n != null)
            }
        }
    }

    /** Reads a Gahvoria zip (a file) or another emulator's folder, and offers to import its saves. */
    fun readSaves(g: Game, uri: Uri, isFolder: Boolean) {
        val dir = saveDir(g)
        worker.execute {
            val items = runCatching {
                if (isFolder) Storage.pathOf(uri)?.let { SaveBackup.readFolder(it) }.orEmpty()
                else activity.contentResolver.openInputStream(uri)!!.use { SaveBackup.readZip(it) }
            }.getOrDefault(emptyList())
            activity.runOnUiThread {
                if (items.isEmpty()) say("No encontré partidas en eso. Elige un respaldo .zip de Gahvoria o la carpeta de otro emulador con archivos Save.", NoticeKind.WARN)
                else importOffer = ImportOffer(g, items, SaveBackup.conflicts(items, dir), if (isFolder) "la carpeta" else "el respaldo")
            }
        }
    }

    fun importSaves(offer: ImportOffer, replace: Boolean) {
        importOffer = null
        val dir = saveDir(offer.game)
        worker.execute {
            val (w, k) = runCatching { SaveBackup.restore(offer.items, dir, replace) }.getOrDefault(0 to offer.items.size)
            activity.runOnUiThread {
                coverVersion++
                say(if (w == 0) "No se importó ninguna partida." else "Importé $w partidas." +
                    (if (replace && offer.conflicts.isNotEmpty()) " Las anteriores quedaron con .antes." else "") +
                    (if (k > 0) " Conservé $k que ya existían." else ""), if (w == 0) NoticeKind.WARN else NoticeKind.OK)
            }
        }
    }

    /** Applies the engine selected from the error report. */
    fun retryEngine(e: ErrorReport, engine: String) {
        errorReport = null
        changeGame(e.game.path) { it.copy(engine = engine) }
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

    fun addFound(prepare: Boolean = false) {
        val f = found ?: return
        val g = Game(f.dir.absolutePath, f.inspection.title, f.inspection.kind, f.inspection.essentials, addedAt = System.currentTimeMillis())
        games = Library.ordered(library.add(g))
        selected = games.indexOfFirst { it.path == g.path }.coerceAtLeast(0)
        found = null
        addMessage = null
        tab = Tab.LIBRARY
        say("${g.title} está en tu biblioteca.", NoticeKind.OK)
        if (prepare) { optionsFor = g; prepareTranslation(g) }
        refreshShortcuts()
        refreshCovers()
    }

    // ---------------------------------------------------------------- store

    var storeGames by mutableStateOf<List<StoreGame>>(emptyList())
    val storeArt = io.github.nikalbazu24.gahvoria.data.StoreArt(activity) { settings.coversOnline }
    /** Goes up each time a store picture arrives, so the store draws again. */
    var storeArtVersion by mutableIntStateOf(0)
        private set
    private var storeArtBusy = false

    /** Looks for the pictures of the store games, one by one and without blocking anything. */
    fun refreshStoreArt() {
        if (storeArtBusy || !settings.coversOnline) return
        storeArtBusy = true
        val list = storeGames
        Thread {
            try { for (g in list) if (storeArt.refresh(g)) activity.runOnUiThread { storeArtVersion++ } }
            finally { storeArtBusy = false }
        }.start()
    }

    /** The store's list: the one shipped with the app, replaced by a newer one from the repository when there is Internet. */
    fun loadStore() {
        val cache = File(activity.filesDir, "store-${BuildConfig.VERSION_CODE}.json")
        val bundled = runCatching { activity.assets.open("store.json").bufferedReader().use { it.readText() } }.getOrDefault("")
        storeGames = StoreCatalog.parse(runCatching { cache.readText() }.getOrDefault("")).ifEmpty { StoreCatalog.parse(bundled) }
        refreshStoreArt()
        worker.execute {
            runCatching {
                val c = AppUpdater.open(StoreCatalog.URL)
                try {
                    val text = c.inputStream.use { it.readBytes() }.toString(Charsets.UTF_8)
                    val list = StoreCatalog.parse(text)
                    if (list.isNotEmpty()) { cache.writeText(text); activity.runOnUiThread { storeGames = list; refreshStoreArt() } }
                } finally { c.disconnect() }
            }
        }
    }

    /** Opens a creator's official page in the browser. Only secure links come from the catalog. */
    fun openPage(url: String) {
        if (!url.startsWith("https://")) return
        runCatching { activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            .onFailure { say("No encontré un navegador para abrir la página.", NoticeKind.WARN) }
    }

    data class ZipPick(val uri: Uri, val name: String, val plan: ZipInstaller.Plan?, val problem: String?,
                       val kind: io.github.nikalbazu24.gahvoria.core.ArchiveKind = io.github.nikalbazu24.gahvoria.core.ArchiveKind.ZIP)
    var zipPick by mutableStateOf<ZipPick?>(null)
    /** What the installation is doing now ("Descomprimiendo… 120 archivos"), or null when idle. */
    var zipProgress by mutableStateOf<String?>(null)

    private fun displayName(uri: Uri): String = runCatching {
        activity.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
            if (it.moveToFirst()) it.getString(0) else null
        }
    }.getOrNull() ?: "juego.zip"

    /** Looks inside the chosen .zip, .7z or .rar without extracting anything. */
    fun pickZip(uri: Uri) {
        zipPick = null
        val name = displayName(uri)
        worker.execute {
            val kind = runCatching { io.github.nikalbazu24.gahvoria.data.Archives.kindOf(activity, uri) }
                .getOrDefault(io.github.nikalbazu24.gahvoria.core.ArchiveKind.UNKNOWN)
            val pick = if (kind == io.github.nikalbazu24.gahvoria.core.ArchiveKind.UNKNOWN) {
                ZipPick(uri, name, null, "Ese archivo no es un .zip, .7z ni .rar. Si es un instalador (.exe), no se puede instalar aquí.", kind)
            } else runCatching {
                val src = io.github.nikalbazu24.gahvoria.data.Archives.open(activity, uri, kind)
                val names = ZipInstaller.listNames(src)
                ZipInstaller.plan(names) to ZipInstaller.whyNot(names)
            }.fold(
                { (plan, why) -> ZipPick(uri, name, plan, if (plan == null) why else null, kind) },
                { ZipPick(uri, name, null, it.message?.takeIf { m -> m.isNotBlank() } ?: "No pude leer ese archivo.", kind) },
            )
            activity.runOnUiThread { zipPick = pick }
        }
    }

    /** Extracts the game to Juegos/<name> (never over an existing folder) and adds it to the library. */
    fun installZip() {
        val pick = zipPick ?: return
        val plan = pick.plan ?: return
        if (!hasAccess) { say("Primero da acceso a tus archivos.", NoticeKind.WARN, "Dar acceso") { askAccess() }; return }
        if (zipProgress != null) return
        zipProgress = "Preparando…"
        val name = pick.name
        io.github.nikalbazu24.gahvoria.data.InstallService.start(activity, "Instalando $name…")
        if (android.os.Build.VERSION.SDK_INT >= 33)
            runCatching { activity.requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 41) }
        // Its own thread (not the shared worker): the rest of the app keeps working, and the service keeps the process alive.
        Thread({
            val result = runCatching {
                val base = File(Storage.root(), "Juegos").also { it.mkdirs() }
                val title = plan.root.trimEnd('/').substringAfterLast('/').ifEmpty { pick.name.substringBeforeLast('.') }
                val dest = ZipInstaller.freeFolder(base, title)
                val src = io.github.nikalbazu24.gahvoria.data.Archives.open(activity, pick.uri, pick.kind)
                val n = ZipInstaller.extract(src, plan, dest) { c ->
                    io.github.nikalbazu24.gahvoria.data.InstallService.progress(activity, "$name: $c de ${plan.files} archivos", c, plan.files)
                    activity.runOnUiThread { zipProgress = "Descomprimiendo… $c de ${plan.files} archivos" }
                }
                dest to n
            }
            io.github.nikalbazu24.gahvoria.data.InstallService.finish(activity,
                result.fold({ (dest, _) -> "${dest.name} está instalado y en tu biblioteca." }, { "No pude instalar $name. No quedó nada a medias." }))
            activity.runOnUiThread {
                zipProgress = null
                result.onSuccess { (dest, _) ->
                    val i = GameInspector.inspect(dest)
                    val g = Game(dest.absolutePath, i.title, i.kind, i.essentials, addedAt = System.currentTimeMillis())
                    games = Library.ordered(library.add(g))
                    selected = games.indexOfFirst { it.path == g.path }.coerceAtLeast(0)
                    zipPick = null
                    say("${g.title} está instalado en ${dest.absolutePath} y en tu biblioteca.", NoticeKind.OK)
                    refreshShortcuts()
                    refreshCovers()
                }.onFailure { say("No pude instalar el juego: " + (it.message ?: "error desconocido") + ". No quedó nada a medias.", NoticeKind.ERROR) }
            }
        }, "GahvoriaInstall").start()
    }

    /** A creator proposes a game: the link and a short description go to the review box (the same private place as reports). */
    fun sendProposal(name: String, page: String, lang: String, contact: String, detected: String = "", done: (Boolean) -> Unit) {
        if (!GameReport.canSend()) { say("Esta versión no puede enviar propuestas.", NoticeKind.WARN); done(false); return }
        val text = io.github.nikalbazu24.gahvoria.core.CreatorText.proposal(name, page, lang, contact, detected.ifEmpty { "sin datos" })
        worker.execute {
            val ok = runCatching { GameReport.send("tienda-" + name, text) }
            activity.runOnUiThread {
                ok.onSuccess { say("Propuesta enviada. Se revisa antes de aparecer en la tienda.", NoticeKind.OK) }
                    .onFailure { say("No se pudo enviar: " + (it.message ?: "sin conexión") + ".", NoticeKind.WARN) }
                done(ok.isSuccess)
            }
        }
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
            val check = AppUpdater.latest(updateChannel)
            val r = check.release
            val now = System.currentTimeMillis()
            activity.runOnUiThread {
                settings.lastCheck = now; lastCheck = now
                update = when {
                    r == null && check.missing -> UpdateState.NoStable
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
                AppUpdater.download(activity, r, updateChannel) { p -> activity.runOnUiThread { update = UpdateState.Downloading(r, p) } }
            }
            activity.runOnUiThread {
                update = result.fold({ UpdateState.Ready(r, it) }, { UpdateState.Failed(it.message ?: "No se pudo descargar") })
                (update as? UpdateState.Ready)?.let { AppUpdater.install(activity, it.apk) }
            }
        }
    }

    /** "dev" or "stable". Changing it forgets the last result so the next check uses the new channel. */
    var updateChannel by mutableStateOf(settings.updateChannel)
        private set

    fun chooseChannel(id: String) {
        if (id == updateChannel) return
        settings.updateChannel = id; updateChannel = id
        if (update !is UpdateState.Downloading) update = UpdateState.Idle
        checkUpdate()
    }

    // ---------------------------------------------------------------- creator panel

    val creatorLog = io.github.nikalbazu24.gahvoria.core.CreatorLog(File(activity.filesDir, "creator"))

    var creatorMode by mutableStateOf(settings.creatorMode)
        private set

    fun chooseCreatorMode(on: Boolean) { settings.creatorMode = on; creatorMode = on; if (!on) { creatorOpen = false; creatorFor = null } }

    /** The creator panel is showing (its list, or the game in [creatorFor]). */
    var creatorOpen by mutableStateOf(false)

    fun openCreator(game: Game? = null) { optionsFor = null; creatorFor = game; creatorOpen = true }

    /** The creator panel is open for this game (null: closed). */
    var creatorFor by mutableStateOf<Game?>(null)

    /** The store proposal form is being opened from the panel for this game. */
    var proposalFor by mutableStateOf<Game?>(null)

    /** Bumped when "mine" changes, so lists redraw. */
    var mineVersion by mutableIntStateOf(0)
        private set

    fun isMine(game: Game) = settings.mine(game.path)

    fun setMine(game: Game, on: Boolean) { settings.setMine(game.path, on); mineVersion++ }

    fun myGames(): List<Game> = games.filter { settings.mine(it.path) }

    fun logKey(game: Game) = creatorLog.key(game.path, game.title)

    fun check(game: Game) = io.github.nikalbazu24.gahvoria.core.GameCheck.check(File(game.path))

    /** The store's entry for this game, when it lists a newer version than the one installed. */
    fun storeUpdate(game: Game): StoreGame? {
        val entry = StoreCatalog.match(storeGames, game.title) ?: return null
        val mine = profile(game).gameVersion
        return entry.takeIf { StoreCatalog.isNewer(it.version, mine) }
    }

    fun summaryText(game: Game): String {
        val p = profile(game)
        val mode = screenMode(game)
        return io.github.nikalbazu24.gahvoria.core.CreatorText.summary(
            game.title, BuildConfig.VERSION_NAME, p.label, p.gameVersion,
            when (game.engine) { Game.ENGINE_CLASSIC -> "clásico"; Game.ENGINE_MODERN -> "moderno"; else -> "automático" },
            when (mode) { "wide" -> "panorámica completa"; "wide_safe" -> "panorámica solo en el mapa"; else -> "original (4:3)" },
            lastLoad(game)?.let { LoadTimes.summary(it) }, game.playMs / 60_000, game.path,
            io.github.nikalbazu24.gahvoria.core.Saves.list(saveDir(game)).size,
        )
    }

    fun copyText(text: String, what: String) { GameReport.copy(activity, text); say("$what copiado.", NoticeKind.OK) }

    /** Sends [text] to the maintainer's private inbox under [name], or copies it when this build cannot send. */
    fun sendCreator(name: String, text: String, done: (Boolean) -> Unit = {}) {
        if (!GameReport.canSend()) { GameReport.copy(activity, text); say("Copiado. Pégalo donde quieras enviarlo.", NoticeKind.OK); done(false); return }
        worker.execute {
            val ok = runCatching { GameReport.send(name, text) }
            activity.runOnUiThread {
                ok.onSuccess { say("Enviado. Gracias.", NoticeKind.OK) }
                    .onFailure { say("No se pudo enviar: " + (it.message ?: "sin conexión") + ".", NoticeKind.WARN) }
                done(ok.isSuccess)
            }
        }
    }

    fun installReady(s: UpdateState.Ready) = AppUpdater.install(activity, s.apk)
}

/** Survives the screen turning (the activity is recreated) but not the app closing: the animation shows once per start. */
private var splashDone = false

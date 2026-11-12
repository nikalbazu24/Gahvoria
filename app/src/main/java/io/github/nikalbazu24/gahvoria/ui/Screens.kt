package io.github.nikalbazu24.gahvoria.ui

import android.text.format.DateUtils
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.nikalbazu24.gahvoria.BuildConfig
import io.github.nikalbazu24.gahvoria.core.Game
import io.github.nikalbazu24.gahvoria.data.GameReport
import io.github.nikalbazu24.gahvoria.core.LoadTimes
import io.github.nikalbazu24.gahvoria.core.Palettes
import io.github.nikalbazu24.gahvoria.core.Saves
import io.github.nikalbazu24.gahvoria.core.Support
import io.github.nikalbazu24.gahvoria.core.ThemeMode
import io.github.nikalbazu24.gahvoria.data.Storage

/** A screen with a title, scrolling content and a width that stays readable on wide screens. */
@Composable
fun Page(title: String, onBack: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = 560.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(44.dp)) {
                if (onBack != null) {
                    val c = LocalColors.current
                    val shape = RoundedCornerShape(14.dp)
                    Box(Modifier.size(40.dp).press(shape, onClick = onBack).background(c.glass, shape).border(1.dp, c.line, shape),
                        contentAlignment = Alignment.Center) { T("‹", 22.sp, FontWeight.Bold) }
                    Spacer(Modifier.size(12.dp))
                }
                T(title, 22.sp, FontWeight.ExtraBold)
            }
            content()
            Spacer(Modifier.height(8.dp))
        }
    }
}

// -------------------------------------------------------------------- game options

@Composable
fun GameScreen(s: AppState, g: Game) {
    val c = LocalColors.current
    BackHandler { s.optionsFor = null }
    var tab by remember(g.path) { mutableIntStateOf(0) }
    Page(g.title, onBack = { s.optionsFor = null }) {
        Segmented(listOf("Partidas", "Mando", "Pantalla", "Motor"), tab) { tab = it }
        when (tab) {
            0 -> {
                val dir = remember(g.path) { s.saveDir(g) }
                val saves = remember(g.path) { Saves.list(dir) }
                Glass(Modifier.fillMaxWidth()) {
                    if (saves.isEmpty()) RowItem("Todavía no hay partidas", "Cuando guardes dentro del juego aparecerán aquí.")
                    saves.forEachIndexed { i, f ->
                        if (i > 0) Divider()
                        RowItem(f.nameWithoutExtension, DateUtils.getRelativeTimeSpanString(f.lastModified(),
                            System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString())
                    }
                }
                T("Se guardan en ${dir.absolutePath}. Es la misma carpeta que usan Essentia y JoiPlay.", 12.sp, FontWeight.Medium, c.muted)
                val saver = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
                    if (uri != null) s.exportSaves(g, uri)
                }
                val zipPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                    if (uri != null) s.readSaves(g, uri, false)
                }
                val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
                    if (uri != null) s.readSaves(g, uri, true)
                }
                Glass(Modifier.fillMaxWidth()) {
                    RowItem("Guardar respaldo", "Un .zip con tus partidas.", onClick = { saver.launch("Respaldo ${Saves.safeName(g.title)}.zip") }) {
                        T("Guardar", 12.sp, FontWeight.Bold, c.accent)
                    }
                    Divider()
                    RowItem("Importar de un respaldo", "Un .zip hecho por Gahvoria. Antes de reemplazar te pregunta.",
                        onClick = { zipPicker.launch(arrayOf("application/zip", "application/octet-stream")) }) { T("Elegir", 12.sp, FontWeight.Bold, c.accent) }
                    Divider()
                    RowItem("Importar de otro emulador", "Elige la carpeta del juego o de las partidas en JoiPlay, Kirin, Navia XP u otro.",
                        onClick = { folderPicker.launch(null) }) { T("Elegir", 12.sp, FontWeight.Bold, c.accent) }
                }
            }
            1 -> {
                Glass(Modifier.fillMaxWidth()) {
                    RowItem("Controles táctiles", "Botones en pantalla. Se ocultan solos al usar un mando.", role = Role.Switch,
                        onClick = { s.changeGame(g.path) { it.copy(touch = !it.touch) } }) { Toggle(g.touch) }
                }
                if (g.touch) PadEditor(s, g)
            }
            2 -> {
                var rev by remember(g.path) { mutableIntStateOf(0) }
                val mode = remember(g.path, rev) { s.screenMode(g) }
                val retro = remember(g.path, rev) { s.retro(g) }
                val tr = remember(g.path, rev) { s.translateGame(g) }
                Glass(Modifier.fillMaxWidth()) {
                    RowItem("Traducir este juego",
                        if (!s.translateOn) "Primero activa «Traducir mientras juegas» en Ajustes → Traducción."
                        else if (s.translateProvider == "deepl" && !s.hasDeeplKey) "Falta tu clave de DeepL en Ajustes → Traducción."
                        else "Traduce el texto del juego al idioma elegido. Las frases quedan guardadas para la próxima vez. No traduce texto dibujado en imágenes.",
                        role = Role.Switch, onClick = { s.setTranslateGame(g, !tr); rev++ }) { Toggle(tr) }
                }
                Glass(Modifier.fillMaxWidth()) {
                    RowItem("Original (4:3)", "La forma con que se hizo el juego, con barras a los lados.", role = Role.RadioButton,
                        onClick = { s.setScreenMode(g, "original"); rev++ }) { Radio(mode == "original") }
                    Divider()
                    RowItem("Panorámica completa (experimental)", "Modo experimental: en batallas y menús todavía hay bugs visuales.", role = Role.RadioButton,
                        onClick = { s.setScreenMode(g, "wide"); rev++ }) { Radio(mode == "wide") }
                    Divider()
                    RowItem("Panorámica solo en el mapa", "Más mapa a los lados; menús y batallas conservan su forma, con los lados difuminados.", role = Role.RadioButton,
                        onClick = { s.setScreenMode(g, "wide_safe"); rev++ }) { Radio(mode == "wide_safe") }
                }
                Glass(Modifier.fillMaxWidth()) {
                    RowItem("Pantalla retro", "Líneas finas como de tele antigua sobre la imagen.",
                        role = Role.Switch, onClick = { s.setRetro(g, !retro); rev++ }) { Toggle(retro) }
                }
                T("Los cambios se aplican la próxima vez que abras el juego.", 12.sp, FontWeight.Medium, c.muted)
            }
            else -> {
                val classic = s.classicAvailable()
                val profile = remember(g.path) { s.profile(g) }
                val autoPick = if (profile.recommendedEngine == Game.ENGINE_CLASSIC && classic) "clásico" else "moderno"
                Glass(Modifier.fillMaxWidth()) {
                    val options = listOf(
                        Triple(Game.ENGINE_AUTO, "Automático",
                            (if (profile.label.isNotEmpty()) "${profile.label}: " else "") + "usa el motor $autoPick. Si falla al abrir, prueba el otro y recuerda el que funciona."),
                        Triple(Game.ENGINE_MODERN, "Moderno", "Ruby 3.1. Para la mayoría de juegos."),
                        Triple(Game.ENGINE_CLASSIC, "Clásico", if (classic) "Ruby 1.8. Para juegos antiguos de RPG Maker XP." else "No incluido en esta versión."),
                    )
                    options.forEachIndexed { i, (id, name, detail) ->
                        if (i > 0) Divider()
                        val enabled = id != Game.ENGINE_CLASSIC || classic
                        RowItem(name, detail, role = Role.RadioButton,
                            onClick = if (enabled) ({ s.changeGame(g.path) { it.copy(engine = id) } }) else null) { Radio(g.engine == id) }
                    }
                }
                val last = remember(g.path) { s.lastLoad(g) }
                Glass(Modifier.fillMaxWidth()) {
                    RowItem("Última carga", last?.let { LoadTimes.summary(it) } ?: "Todavía no hay tiempos: aparecen después de abrir el juego.")
                }
            }
        }
        val coverSource = s.covers.source(g)
        val coverText = when (coverSource) {
            io.github.nikalbazu24.gahvoria.data.Covers.Source.WEB -> s.covers.credit(g) ?: "Imagen oficial de Internet."
            io.github.nikalbazu24.gahvoria.data.Covers.Source.FOLDER -> "Imagen de la carpeta del juego."
            else -> if (s.coversOnline) "Sin imagen: dibujada por Gahvoria. Si el juego trae una o está en el catálogo, aparecerá sola."
                    else "Sin imagen: dibujada por Gahvoria."
        }
        Glass(Modifier.fillMaxWidth()) { RowItem("Carátula", coverText) }
        val lastErr = remember(g.path, s.coverVersion) { s.lastReport(g) }
        Glass(Modifier.fillMaxWidth()) {
            if (lastErr == null) RowItem("Informe de error", "Este juego no ha dejado errores.")
            else RowItem("Enviar último informe", "Error del " + DateUtils.getRelativeTimeSpanString(lastErr.first,
                System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS) + ": " + GameReport.firstLine(lastErr.second),
                onClick = { s.sendReport(g, lastErr.second) }) { T("Enviar", 12.sp, FontWeight.Bold, c.accent) }
        }
        LaunchedEffect(Unit) { if (s.storeGames.isEmpty()) s.loadStore() }
        s.storeUpdate(g)?.let { n ->
            Glass(Modifier.fillMaxWidth()) {
                RowItem("Versión nueva en la tienda", "El creador publicó la ${n.version} y tienes la ${s.profile(g).gameVersion}. Tus partidas no se tocan al actualizar.",
                    titleColor = c.accent, onClick = { s.openPage(n.page) }) { T("Abrir", 12.sp, FontWeight.Bold, c.accent) }
            }
        }
        if (s.creatorMode) {
            s.mineVersion
            val mine = s.isMine(g)
            Glass(Modifier.fillMaxWidth()) {
                RowItem("Este juego es mío", "Guarda el historial de sus errores y activa su panel de creador.",
                    role = Role.Switch, onClick = { s.setMine(g, !mine) }) { Toggle(mine) }
                if (mine) {
                    Divider()
                    RowItem("Panel de creador", "Resumen, chequeo, informes y pedidos de ajuste.", onClick = { s.openCreator(g) }) { T("→", 16.sp, FontWeight.Bold, c.faint) }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        var step by remember(g.path) { mutableIntStateOf(0) }
        Glass(Modifier.fillMaxWidth()) {
            RowItem(
                when (step) { 0 -> "Quitar de la biblioteca"; else -> "Toca otra vez para confirmar" },
                "No borra la carpeta del juego ni sus partidas.", titleColor = c.accent,
                onClick = { if (step == 0) step = 1 else s.forget(g) },
            ) { T("→", 16.sp, FontWeight.Bold, c.faint) }
            Divider()
            RowItem("Borrar juego por completo", "Borra su carpeta, partidas y accesos. Pide dos confirmaciones.",
                titleColor = if (c.dark) androidx.compose.ui.graphics.Color(0xFFFF8E8E) else androidx.compose.ui.graphics.Color(0xFFC62F3A),
                onClick = { s.deleting = g }) { T("→", 16.sp, FontWeight.Bold, c.faint) }
        }
    }
}

// -------------------------------------------------------------------- add

@Composable
fun AddScreen(s: AppState) {
    val c = LocalColors.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) s.inspectFolder(Storage.pathOf(uri))
    }
    val f = s.found
    Page("Añadir juego") {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val done = if (f == null) 1 else 2
            repeat(3) { i ->
                Box(Modifier.weight(1f).height(4.dp).clip(CircleShape).background(if (i < done) c.accent else c.glassStrong))
            }
        }
        if (!s.hasAccess) {
            T("Primero da acceso a tus archivos en Ajustes. Después vuelve aquí para elegir tu juego.", 14.sp, FontWeight.Medium, c.muted)
            PrimaryButton("Ir a Ajustes", Modifier.fillMaxWidth()) { s.tab = Tab.SETTINGS }
            return@Page
        }
        T("Elige la carpeta donde está el juego. Gahvoria la lee para saber qué motor usa; no cambia ningún archivo.", 14.sp, FontWeight.Medium, c.muted)
        PrimaryButton(if (f == null) "Elegir carpeta" else "Elegir otra carpeta", Modifier.fillMaxWidth()) { picker.launch(null) }
        s.addMessage?.let { T(it, 13.sp, FontWeight.SemiBold, c.warn) }
        if (f != null) {
            val i = f.inspection
            Glass(Modifier.fillMaxWidth()) {
                RowItem("Motor", i.engineLabel) {
                    val (t, col) = when (i.support) {
                        Support.PLAYABLE -> "Listo" to c.ok
                        Support.NEXT_PHASE -> "Próxima fase" to c.warn
                        Support.RESEARCH -> "En investigación" to c.warn
                        Support.NOT_A_GAME -> "No es un juego" to c.warn
                    }
                    T(t, 12.sp, FontWeight.Bold, col)
                }
                Divider(); RowItem("Título", i.title)
                Divider(); RowItem("Carpeta", f.dir.absolutePath)
            }
            when (i.support) {
                Support.NOT_A_GAME -> T("No encontré los archivos de un juego en esta carpeta. Elige la carpeta que contiene Game.ini, Data o project.godot.", 13.sp, FontWeight.Medium, c.muted)
                else -> {
                    if (i.support != Support.PLAYABLE) T("Puedes añadirlo para tenerlo a mano; se podrá jugar cuando su motor esté listo.", 13.sp, FontWeight.Medium, c.muted)
                    PrimaryButton("Añadir a la biblioteca", Modifier.fillMaxWidth()) { s.addFound() }
                }
            }
        }
    }
}

// -------------------------------------------------------------------- updates

@Composable
fun UpdatesScreen(s: AppState) {
    val c = LocalColors.current
    val u = s.update
    Page("Actualizaciones") {
        Glass(Modifier.fillMaxWidth(), radius = 24.dp) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val badge = when (u) {
                    is UpdateState.Available, is UpdateState.Downloading, is UpdateState.Ready -> "Nueva versión"
                    is UpdateState.UpToDate -> "Estás al día"
                    is UpdateState.Offline -> "Sin conexión"
                    is UpdateState.NoStable -> "Sin versión estable"
                    is UpdateState.Failed -> "No se pudo"
                    else -> "Versión instalada"
                }
                T(badge.uppercase(), 10.sp, FontWeight.ExtraBold, c.accent,
                    Modifier.clip(CircleShape).background(c.accent.copy(alpha = .16f)).padding(horizontal = 10.dp, vertical = 4.dp), spacing = 1.4.sp)
                val shown = when (u) { is UpdateState.Available -> u.release.versionName; is UpdateState.Downloading -> u.release.versionName
                    is UpdateState.Ready -> u.release.versionName; else -> BuildConfig.VERSION_NAME }
                T("Gahvoria $shown", 30.sp, FontWeight.ExtraBold)
                T(when (u) {
                    is UpdateState.Available -> "Tienes la ${BuildConfig.VERSION_NAME}. Se descarga, se comprueba que sea Gahvoria y luego Android te pide confirmar. Tus juegos y partidas no se tocan."
                    is UpdateState.Downloading -> "Descargando… No cierres Gahvoria."
                    is UpdateState.Ready -> "Lista para instalar. Android te pedirá confirmar."
                    is UpdateState.UpToDate -> "No hay una versión más nueva."
                    is UpdateState.Offline -> "No pude revisar. Comprueba tu conexión."
                    is UpdateState.NoStable -> "Todavía no hay una versión estable publicada. Elige «Todas las versiones» abajo para recibir las de prueba."
                    is UpdateState.Failed -> u.message
                    is UpdateState.Checking -> "Buscando…"
                    else -> "Gahvoria nunca instala nada sin que lo confirmes."
                }, 13.sp, FontWeight.Medium, c.muted)
                if (u is UpdateState.Downloading) ProgressBar(u.progress)
                when (u) {
                    is UpdateState.Available -> PrimaryButton("Descargar e instalar", Modifier.fillMaxWidth()) { s.downloadUpdate(u.release) }
                    is UpdateState.Ready -> PrimaryButton("Instalar", Modifier.fillMaxWidth()) { s.installReady(u) }
                    is UpdateState.Downloading, is UpdateState.Checking -> {}
                    else -> PrimaryButton("Buscar actualización", Modifier.fillMaxWidth()) { s.checkUpdate() }
                }
            }
        }
        Glass(Modifier.fillMaxWidth()) {
            RowItem("Todas las versiones", "Recibes cada mejora en cuanto está lista. Puede traer cambios a medio probar.", role = Role.RadioButton,
                onClick = { s.chooseChannel(io.github.nikalbazu24.gahvoria.core.UpdateChannel.DEV) }) {
                Radio(s.updateChannel == io.github.nikalbazu24.gahvoria.core.UpdateChannel.DEV)
            }
            Divider()
            RowItem("Solo versiones estables", "Recibes únicamente las que el equipo marcó como listas.", role = Role.RadioButton,
                onClick = { s.chooseChannel(io.github.nikalbazu24.gahvoria.core.UpdateChannel.STABLE) }) {
                Radio(s.updateChannel == io.github.nikalbazu24.gahvoria.core.UpdateChannel.STABLE)
            }
        }
        Glass(Modifier.fillMaxWidth()) {
            RowItem("Buscar sola", "Al abrir Gahvoria, como mucho cada 6 horas.", role = Role.Switch,
                onClick = { s.chooseAutoCheck(!s.autoCheck) }) { Toggle(s.autoCheck) }
            Divider()
            RowItem("Última comprobación", if (s.lastCheck == 0L) "Nunca" else DateUtils.getRelativeTimeSpanString(s.lastCheck,
                System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString())
        }
    }
}

// -------------------------------------------------------------------- settings

/** The settings sections, always visible at the side on wide screens (Díptico style). Each option lives in exactly one. */
private enum class Sec(val title: String, val hint: String, val words: String) {
    ESSENTIAL("Lo esencial", "Acceso a tus archivos", "acceso permiso archivos"),
    LOOK("Apariencia", "Claro, oscuro y colores", "apariencia colores paleta estilo tema modo oscuro claro sistema"),
    FILES("Archivos y accesos", "Launchers como Díptico", "accesos launcher diptico daijisho es-de carpeta atajos"),
    COVERS("Carátulas", "Imágenes de los juegos", "caratulas portadas imagenes internet catalogo"),
    REPORTS("Informes", "Cuando un juego falla", "informes errores enviar drive"),
    TRANSLATE("Traducción", "Juegos en otro idioma", "traduccion traductor idioma ingles"),
    CREATOR("Creadores", "Probar y enlazar tu juego", "creadores creador panel fangame juego propio errores informes chequeo tienda"),
    ABOUT("Acerca de", "Versión e introducción", "acerca version introduccion licencias motor");

    fun matches(q: String): Boolean {
        val n = q.trim().lowercase()
        if (n.isEmpty()) return true
        val flat = java.text.Normalizer.normalize(n, java.text.Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "")
        return title.lowercase().contains(n) || hint.lowercase().contains(n) || words.contains(flat)
    }
}

@Composable
private fun secColor(sec: Sec): androidx.compose.ui.graphics.Color {
    val c = LocalColors.current
    return when (sec) {
        Sec.ESSENTIAL -> c.accent
        Sec.LOOK -> c.accent2
        Sec.FILES -> c.accent3
        Sec.COVERS -> c.ok
        Sec.REPORTS -> c.warn
        Sec.TRANSLATE -> c.accent2
        Sec.CREATOR -> c.accent3
        Sec.ABOUT -> c.muted
    }
}

@Composable
fun SettingsScreen(s: AppState) {
    val c = LocalColors.current
    var query by remember { mutableStateOf("") }
    var open by remember { mutableStateOf<Sec?>(null) }
    var picked by remember { mutableStateOf(Sec.ESSENTIAL) }
    val shown = Sec.entries.filter { it.matches(query) }
    androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth >= 600.dp
        if (wide) {
            Row(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                Column(Modifier.width(220.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    T("Ajustes", 22.sp, FontWeight.ExtraBold, modifier = Modifier.height(44.dp))
                    SettingsSearch(query) { query = it }
                    for (sec in Sec.entries) {
                        val on = sec == picked && query.isBlank()
                        val dim = !sec.matches(query)
                        val col = secColor(sec)
                        val shape = RoundedCornerShape(16.dp)
                        Row(
                            Modifier.fillMaxWidth().press(shape, role = Role.Tab) { picked = sec; query = "" }
                                .background(if (on) col.copy(alpha = .22f) else c.glass, shape)
                                .border(if (on) 2.dp else 1.dp, if (on) col else c.line, shape)
                                .padding(horizontal = 14.dp, vertical = 12.dp).alpha(if (dim) .4f else 1f),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(Modifier.size(10.dp).clip(CircleShape).background(col))
                            Spacer(Modifier.size(10.dp))
                            Column {
                                T(sec.title, 14.sp, FontWeight.Bold)
                                T(sec.hint, 11.sp, FontWeight.Medium, c.muted)
                            }
                        }
                    }
                }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 56.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    val list = if (query.isBlank()) listOf(picked) else shown
                    if (list.isEmpty()) T("No encontré ese ajuste.", 13.sp, FontWeight.Medium, c.muted)
                    for (sec in list) { Label(sec.title, secColor(sec)); SecContent(s, sec) }
                    Spacer(Modifier.height(8.dp))
                }
            }
        } else {
            val cur = open
            if (cur != null && query.isBlank()) {
                BackHandler { open = null }
                Page(cur.title, onBack = { open = null }) { SecContent(s, cur) }
            } else Page("Ajustes") {
                SettingsSearch(query) { query = it }
                if (shown.isEmpty()) T("No encontré ese ajuste.", 13.sp, FontWeight.Medium, c.muted)
                Glass(Modifier.fillMaxWidth()) {
                    shown.forEachIndexed { i, sec ->
                        if (i > 0) Divider()
                        RowItem(sec.title, sec.hint, onClick = { open = sec; query = "" }) {
                            Box(Modifier.size(10.dp).clip(CircleShape).background(secColor(sec)))
                            Spacer(Modifier.size(10.dp))
                            T("›", 18.sp, FontWeight.Bold, c.faint)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun SettingsSearch(query: String, hint: String = "Buscar un ajuste", onChange: (String) -> Unit) {
    val c = LocalColors.current
    val shape = RoundedCornerShape(14.dp)
    androidx.compose.foundation.text.BasicTextField(
        query, onChange, singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(color = c.ink, fontSize = 14.sp, fontWeight = FontWeight.Medium, fontFamily = Montserrat),
        cursorBrush = androidx.compose.ui.graphics.SolidColor(c.accent),
        modifier = Modifier.fillMaxWidth().background(c.glass, shape).border(1.dp, c.line, shape).padding(horizontal = 14.dp, vertical = 12.dp),
        decorationBox = { inner -> Box { if (query.isEmpty()) T(hint, 14.sp, FontWeight.Medium, c.faint); inner() } },
    )
}

@Composable
private fun SecContent(s: AppState, sec: Sec) {
    val c = LocalColors.current
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        when (sec) {
            Sec.ESSENTIAL -> {
                Glass(Modifier.fillMaxWidth()) {
                    RowItem("Acceso a tus archivos", if (s.hasAccess) "Concedido. Gahvoria lee tus juegos y guarda las partidas junto a ellos."
                        else "Necesario para abrir juegos y guardar partidas junto a ellos.",
                        onClick = if (s.hasAccess) null else ({ s.askAccess() })) {
                        T(if (s.hasAccess) "Listo" else "Dar acceso", 12.sp, FontWeight.Bold, if (s.hasAccess) c.ok else c.accent)
                    }
                }
            }
            Sec.LOOK -> {
                Segmented(listOf("Sistema", "Oscuro", "Claro"), ThemeMode.entries.indexOf(s.mode)) { s.chooseMode(ThemeMode.entries[it]) }
                Glass(Modifier.fillMaxWidth()) {
                Palettes.all.forEachIndexed { i, p ->
                    if (i > 0) Divider()
                    RowItem(p.name, if (i == 0) "Principal" else null, role = Role.RadioButton, onClick = { s.choosePalette(p.id) }) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            for (v in listOf(p.dark.accent, p.dark.accent2, p.dark.accent3))
                                Box(Modifier.size(18.dp).clip(CircleShape).background(androidx.compose.ui.graphics.Color(v.toInt())).border(2.dp, c.bg2, CircleShape))
                        }
                        Spacer(Modifier.size(12.dp))
                        Radio(s.palette == p.id)
                    }
                }
                }
            }
            Sec.FILES -> {
                val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
                    if (uri != null) Storage.pathOf(uri)?.let { s.createShortcuts(it) }
                }
                Glass(Modifier.fillMaxWidth()) {
                    RowItem("Carpeta de accesos para launchers", (s.shortcutsPath ?: "Todavía no creada. Se propone ROMs/Gahvoria.") +
                        "\nDíptico, Daijishō, ES-DE y otros abren tus juegos desde ahí. Nunca se borran accesos.")
                    Divider()
                    RowItem("Elegir carpeta", null, onClick = { folderPicker.launch(null) }) { T("Elegir", 12.sp, FontWeight.Bold, c.accent) }
                    Divider()
                    RowItem(if (s.shortcutsOn) "Actualizar accesos" else "Crear accesos", null, onClick = { s.createShortcuts() }) {
                        T(if (s.shortcutsOn) "Actualizar" else "Crear", 12.sp, FontWeight.Bold, c.accent)
                    }
                }
            }
            Sec.COVERS -> Glass(Modifier.fillMaxWidth()) {
                RowItem("Buscar imágenes en Internet", "La imagen oficial cuando el juego está en el catálogo; si no, una de su carpeta.",
                    role = Role.Switch, onClick = { s.chooseCoversOnline(!s.coversOnline) }) { Toggle(s.coversOnline) }
            }
            Sec.REPORTS -> Glass(Modifier.fillMaxWidth()) {
                RowItem("Envío de informes", if (s.canSendReports) "Activado. Solo se envían cuando tú tocas Enviar informe."
                    else "Esta versión solo puede copiarlos.")
                Divider()
                RowItem("Último error de cada juego", "Está en las opciones de ese juego.")
            }
            Sec.TRANSLATE -> {
                Glass(Modifier.fillMaxWidth()) {
                    RowItem("Traducir mientras juegas", "Apagado hasta que lo actives. Después eliges en qué juegos, en sus opciones → Pantalla.",
                        role = Role.Switch, onClick = { s.chooseTranslateOn(!s.translateOn) }) { Toggle(s.translateOn) }
                }
                if (s.translateOn) {
                    Label("Con qué traducir")
                    Glass(Modifier.fillMaxWidth()) {
                        RowItem("En el teléfono (gratis)", "Traductor de Google sin conexión. Baja un paquete de idioma la primera vez (necesita Internet). Calidad modesta.",
                            role = Role.RadioButton, onClick = { s.chooseTranslateProvider("mlkit") }) { Radio(s.translateProvider == "mlkit") }
                        Divider()
                        RowItem("DeepL con tu propia clave", "Mejor calidad. La clave y el gasto son tuyos; Gahvoria no incluye ninguna.",
                            role = Role.RadioButton, onClick = { s.chooseTranslateProvider("deepl") }) { Radio(s.translateProvider == "deepl") }
                        if (s.translateProvider == "deepl") {
                            Divider()
                            var key by remember { mutableStateOf("") }
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                val shape = RoundedCornerShape(14.dp)
                                androidx.compose.foundation.text.BasicTextField(
                                    key, { key = it }, singleLine = true,
                                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                    textStyle = androidx.compose.ui.text.TextStyle(color = c.ink, fontSize = 14.sp, fontWeight = FontWeight.Medium, fontFamily = Montserrat),
                                    cursorBrush = androidx.compose.ui.graphics.SolidColor(c.accent),
                                    modifier = Modifier.fillMaxWidth().background(c.glass, shape).border(1.dp, c.line, shape).padding(14.dp),
                                    decorationBox = { inner -> Box { if (key.isEmpty()) T(if (s.hasDeeplKey) "Clave guardada en este teléfono" else "Pega tu clave de DeepL", 14.sp, FontWeight.Medium, c.faint); inner() } },
                                )
                                SecondaryButton("Guardar y comprobar clave", Modifier.fillMaxWidth(), enabled = key.isNotBlank()) { s.saveDeeplKey(key); key = "" }
                                s.deeplState?.let { T(it, 12.sp, FontWeight.SemiBold, c.muted) }
                            }
                        }
                    }
                    Label("Traducir a")
                    Glass(Modifier.fillMaxWidth()) {
                        listOf("es" to "Español", "en" to "Inglés", "pt" to "Portugués").forEachIndexed { i, (id, name) ->
                            if (i > 0) Divider()
                            RowItem(name, null, role = Role.RadioButton, onClick = { s.chooseTranslateTarget(id) }) { Radio(s.translateTarget == id) }
                        }
                    }
                    Label("Memoria")
                    val total = remember(s.translationsVersion, s.coverVersion) { s.translationsTotal() }
                    val status = remember(s.translationsVersion, s.coverVersion) { s.translateStatus() }
                    Glass(Modifier.fillMaxWidth()) {
                        RowItem("Traducciones guardadas", "${total.first} frases · ${io.github.nikalbazu24.gahvoria.data.Translations.humanSize(total.second)}. Se reutilizan al reabrir el juego, sin esperar.")
                        if (status != null) { Divider(); RowItem("Último aviso del traductor", status.second) }
                        Divider()
                        RowItem("Borrar traducciones guardadas", "Solo borra esta memoria. No toca juegos ni partidas.",
                            titleColor = c.accent, onClick = { s.clearTranslations() }) { T("Borrar", 12.sp, FontWeight.Bold, c.accent) }
                    }
                    T("Traduce el texto que el juego escribe; no el que viene dibujado dentro de imágenes. Con alfabetos como el japonés la calidad baja. Los códigos del juego (colores, nombres) se conservan; si uno no se puede conservar, esa frase queda sin traducir.", 12.sp, FontWeight.Medium, c.muted)
                }
            }
            Sec.CREATOR -> {
                Glass(Modifier.fillMaxWidth()) {
                    RowItem("Modo creador", "Para quien hace un fan game: chequeo del juego, historial de errores y pedidos de ajuste.",
                        role = Role.Switch, onClick = { s.chooseCreatorMode(!s.creatorMode) }) { Toggle(s.creatorMode) }
                    if (s.creatorMode) {
                        Divider()
                        RowItem("Abrir el panel de creador", "Marca tus juegos y míralos uno por uno.", onClick = { s.openCreator() }) { T("→", 16.sp, FontWeight.Bold, c.faint) }
                    }
                }
                T("Es opcional. Con el modo apagado, Gahvoria se ve igual que para cualquier jugador.", 12.sp, FontWeight.Medium, c.muted)
            }
            Sec.ABOUT -> Glass(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Cartridge(52.dp)
                    Spacer(Modifier.size(14.dp))
                    Column {
                        T("Gahvoria", 16.sp, FontWeight.Bold)
                        T("Versión ${BuildConfig.VERSION_NAME}", 12.sp, FontWeight.Medium, c.muted)
                    }
                }
                Divider()
                RowItem("Motor de juego", "mkxp-z (licencia GPL 2.0), moderno y clásico.")
                Divider()
                RowItem("Tipografía", "Montserrat (SIL Open Font License).")
                Divider()
                RowItem("Código", "github.com/nikalbazu24/Gahvoria")
                Divider()
                RowItem("Ver introducción", null, onClick = { s.showIntro() }) { T("→", 16.sp, FontWeight.Bold, c.faint) }
            }
        }
    }
}

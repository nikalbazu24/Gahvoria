package io.github.nikalbazu24.gahvoria.ui

import android.text.format.DateUtils
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.nikalbazu24.gahvoria.core.CreatorText
import io.github.nikalbazu24.gahvoria.core.Finding
import io.github.nikalbazu24.gahvoria.core.Game
import io.github.nikalbazu24.gahvoria.core.GameCheck
import io.github.nikalbazu24.gahvoria.core.Level
import io.github.nikalbazu24.gahvoria.core.LogEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The creator panel: for someone who makes a fan game and wants to test it here and link it to Gahvoria. Everything
 * it keeps (error history) stays on the device; sending a report or a request is always the creator's choice.
 */
@Composable
fun CreatorScreen(s: AppState) {
    val g = s.creatorFor
    if (g == null) {
        BackHandler { s.creatorOpen = false }
        CreatorList(s)
    } else {
        BackHandler { s.creatorFor = null }
        CreatorGame(s, g)
    }
}

@Composable
private fun CreatorList(s: AppState) {
    val c = LocalColors.current
    s.mineVersion
    val mine = s.myGames()
    val others = s.games.filter { !s.isMine(it) }
    Page("Panel de creador", onBack = { s.creatorOpen = false }) {
        T("Prueba tu juego en el teléfono y en la Thor, mira sus errores y pide ajustes sin salir de Gahvoria.", 13.sp, FontWeight.Medium, c.muted)
        Label("Tus juegos")
        Glass(Modifier.fillMaxWidth()) {
            if (mine.isEmpty()) RowItem("Todavía no marcaste ninguno", "Marca abajo el juego que hiciste tú. Solo en esos Gahvoria guarda el historial de errores.")
            mine.forEachIndexed { i, g ->
                if (i > 0) Divider()
                val errors = s.creatorLog.count(s.logKey(g))
                val version = s.profile(g).gameVersion
                RowItem(g.title,
                    (if (version.isNotEmpty()) "Versión $version · " else "") + (if (errors == 0) "Sin errores guardados" else "$errors ${if (errors == 1) "error guardado" else "errores guardados"}"),
                    onClick = { s.creatorFor = g }) { T("›", 18.sp, FontWeight.Bold, c.faint) }
            }
        }
        if (others.isNotEmpty()) {
            Label("Marcar un juego como mío")
            Glass(Modifier.fillMaxWidth()) {
                others.forEachIndexed { i, g ->
                    if (i > 0) Divider()
                    RowItem(g.title, null, role = Role.Switch, onClick = { s.setMine(g, true) }) { Toggle(false) }
                }
            }
        }
        T("Marcarlo no cambia el juego ni sus partidas. Puedes quitar la marca cuando quieras.", 12.sp, FontWeight.Medium, c.muted)
    }
}

@Composable
private fun CreatorGame(s: AppState, g: Game) {
    var tab by remember(g.path) { mutableIntStateOf(0) }
    Page(g.title, onBack = { s.creatorFor = null }) {
        Segmented(listOf("Resumen", "Chequeo", "Informes", "Pedir ajuste"), tab) { tab = it }
        when (tab) {
            0 -> Summary(s, g)
            1 -> Check(s, g)
            2 -> Reports(s, g)
            else -> Request(s, g)
        }
    }
}

@Composable
private fun Summary(s: AppState, g: Game) {
    val c = LocalColors.current
    val p = remember(g.path) { s.profile(g) }
    val newer = s.storeUpdate(g)
    if (newer != null) Glass(Modifier.fillMaxWidth()) {
        RowItem("Versión nueva en la tienda", "La tienda lista la ${newer.version} y tu juego dice ${p.gameVersion}. Si ya la publicaste, juégala aquí para probarla.",
            titleColor = c.accent, onClick = { s.openPage(newer.page) }) { T("Abrir", 12.sp, FontWeight.Bold, c.accent) }
    }
    val text = remember(g.path, s.coverVersion) { s.summaryText(g) }
    Glass(Modifier.fillMaxWidth()) {
        val rows = text.lines().map { it.substringBefore(": ") to it.substringAfter(": ") }
        rows.forEachIndexed { i, (k, v) -> if (i > 0) Divider(); RowItem(k, v) }
    }
    PrimaryButton("Copiar estos datos", Modifier.fillMaxWidth()) { s.copyText(text, "Datos") }
    SecondaryButton("Proponer mi juego en la Tienda", Modifier.fillMaxWidth()) {
        s.proposalFor = g; s.creatorOpen = false; s.creatorFor = null; s.tab = Tab.STORE
    }
    Glass(Modifier.fillMaxWidth()) {
        RowItem("Quitar la marca «es mío»", "No cambia el juego. El historial de errores guardado se conserva.",
            onClick = { s.setMine(g, false); s.creatorFor = null }) { T("Quitar", 12.sp, FontWeight.Bold, c.accent) }
    }
}

@Composable
private fun levelColor(l: Level): Color {
    val c = LocalColors.current
    return when (l) {
        Level.PROBLEM -> if (c.dark) Color(0xFFFF8E8E) else Color(0xFFC62F3A)
        Level.WARN -> c.warn
        Level.INFO -> c.accent3
        Level.OK -> c.ok
    }
}

private fun levelName(l: Level) = when (l) { Level.PROBLEM -> "PROBLEMA"; Level.WARN -> "AVISO"; Level.INFO -> "DATO"; Level.OK -> "BIEN" }

@Composable
private fun Check(s: AppState, g: Game) {
    val c = LocalColors.current
    var rev by remember(g.path) { mutableIntStateOf(0) }
    val result by produceState<List<Finding>?>(null, g.path, rev) {
        value = null
        value = withContext(Dispatchers.Default) { runCatching { s.check(g) }.getOrDefault(emptyList()) }
    }
    val list = result
    if (list == null) { T("Revisando los archivos…", 13.sp, FontWeight.SemiBold, c.muted); return }
    T(GameCheck.summary(list), 15.sp, FontWeight.ExtraBold)
    Glass(Modifier.fillMaxWidth()) {
        list.sortedBy { it.level.ordinal }.forEachIndexed { i, f ->
            if (i > 0) Divider()
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Tag(levelName(f.level), levelColor(f.level))
                    T(f.title, 14.sp, FontWeight.Bold, modifier = Modifier.weight(1f))
                }
                T(f.detail, 12.sp, FontWeight.Medium, c.muted)
            }
        }
    }
    T("El chequeo solo mira los archivos del juego; no lo ejecuta. Probarlo en el teléfono y en la Thor sigue siendo necesario.", 12.sp, FontWeight.Medium, c.muted)
    PrimaryButton("Copiar el chequeo", Modifier.fillMaxWidth()) {
        s.copyText(g.title + "\n" + list.joinToString("\n") { "[" + levelName(it.level) + "] " + it.title + ": " + it.detail }, "Chequeo")
    }
    SecondaryButton("Volver a revisar", Modifier.fillMaxWidth()) { rev++ }
}

@Composable
private fun Reports(s: AppState, g: Game) {
    val c = LocalColors.current
    val key = s.logKey(g)
    var rev by remember(g.path) { mutableIntStateOf(0) }
    val entries = remember(g.path, rev, s.coverVersion) { s.creatorLog.list(key) }
    var open by remember(g.path) { mutableStateOf<LogEntry?>(null) }
    val cur = open
    if (cur != null) {
        BackHandler { open = null }
        val text = remember(cur) { cur.text() }
        val d = remember(cur) { io.github.nikalbazu24.gahvoria.core.ErrorDiagnosis.diagnose(text, g.engine != Game.ENGINE_CLASSIC, s.classicAvailable()) }
        T(DateUtils.getRelativeTimeSpanString(cur.at, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString(), 12.sp, FontWeight.Bold, c.muted)
        Glass(Modifier.fillMaxWidth()) {
            RowItem(d.title, d.detail)
            Divider()
            Column(Modifier.padding(16.dp)) { T(text, 11.sp, FontWeight.Medium, c.ink) }
        }
        PrimaryButton("Enviar este informe al equipo", Modifier.fillMaxWidth()) {
            s.sendCreator("creador-" + g.title, "Gahvoria ${io.github.nikalbazu24.gahvoria.BuildConfig.VERSION_NAME}\nJuego: ${g.title}\n\n$text")
        }
        SecondaryButton("Copiar", Modifier.fillMaxWidth()) { s.copyText(text, "Informe") }
        SecondaryButton("Volver a la lista", Modifier.fillMaxWidth()) { open = null }
        return
    }
    if (entries.isEmpty()) {
        Glass(Modifier.fillMaxWidth()) {
            RowItem("Sin errores guardados", "Cuando tu juego se cierre con un error, aparece aquí con su diagnóstico y las líneas del script. Se guardan los últimos 20.")
        }
        return
    }
    Glass(Modifier.fillMaxWidth()) {
        entries.forEachIndexed { i, e ->
            if (i > 0) Divider()
            RowItem(e.headline().ifEmpty { "Error sin descripción" },
                DateUtils.getRelativeTimeSpanString(e.at, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString(),
                onClick = { open = e }) { T("›", 18.sp, FontWeight.Bold, c.faint) }
        }
    }
    var step by remember(g.path) { mutableIntStateOf(0) }
    Glass(Modifier.fillMaxWidth()) {
        RowItem(if (step == 0) "Borrar este historial" else "Toca otra vez para borrarlo",
            "Solo borra estas copias de los informes. No toca el juego ni sus partidas.", titleColor = c.accent,
            onClick = { if (step == 0) step = 1 else { s.creatorLog.clear(key); step = 0; rev++ } }) { T("→", 16.sp, FontWeight.Bold, c.faint) }
    }
}

@Composable
private fun Request(s: AppState, g: Game) {
    val c = LocalColors.current
    var kind by remember(g.path) { mutableIntStateOf(0) }
    var text by remember(g.path) { mutableStateOf("") }
    var withReport by remember(g.path) { mutableStateOf(true) }
    var withSummary by remember(g.path) { mutableStateOf(true) }
    var sending by remember(g.path) { mutableStateOf(false) }
    val last = remember(g.path, s.coverVersion) { s.creatorLog.list(s.logKey(g)).firstOrNull() }
    Segmented(CreatorText.REQUEST_KINDS, kind) { kind = it }
    Field(text, "Cuenta qué pasa y en qué parte del juego", singleLine = false) { text = it }
    Glass(Modifier.fillMaxWidth()) {
        RowItem("Incluir el último informe", if (last == null) "Todavía no hay informes guardados." else "El error más reciente de este juego.",
            role = Role.Switch, onClick = { if (last != null) withReport = !withReport }) { Toggle(withReport && last != null) }
        Divider()
        RowItem("Incluir los datos del juego", "Versión, motor, pantalla y tiempos. Sin tus partidas.",
            role = Role.Switch, onClick = { withSummary = !withSummary }) { Toggle(withSummary) }
    }
    val ok = text.trim().length >= 10 && !sending
    PrimaryButton(if (sending) "Enviando…" else "Enviar el pedido", Modifier.fillMaxWidth(), enabled = ok) {
        sending = true
        val body = CreatorText.request(kind, text, if (withSummary) s.summaryText(g) else null, if (withReport) last?.text() else null)
        s.sendCreator("ajuste-" + g.title, body) { done -> sending = false; if (done) text = "" }
    }
    T(if (text.trim().length < 10) "Escribe al menos una frase corta para poder enviarlo." else "Va a la bandeja privada del equipo de Gahvoria. Nadie más lo ve.", 12.sp, FontWeight.Medium, c.muted)
}

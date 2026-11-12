package io.github.nikalbazu24.gahvoria.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.nikalbazu24.gahvoria.core.Game
import io.github.nikalbazu24.gahvoria.core.GameRemoval

@Composable
private fun Scrim(content: @Composable () -> Unit) {
    val c = LocalColors.current
    val shape = RoundedCornerShape(26.dp)
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = .35f)).pointerInput(Unit) { detectTapGestures { } }.padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.widthIn(max = 460.dp).heightIn(max = 560.dp).background(c.bg2, shape).border(1.dp, c.line, shape)
                .verticalScroll(rememberScrollState()).padding(22.dp),
        ) { content() }
    }
}

/**
 * Deleting a game for good, in two steps. Step 1 says exactly what goes and offers a backup of the saves first;
 * step 2 asks to type the game's name. Nothing is deleted before step 2 is confirmed.
 */
@Composable
fun DeleteDialog(s: AppState, g: Game) {
    val c = LocalColors.current
    val bad = if (c.dark) Color(0xFFFF8E8E) else Color(0xFFC62F3A)
    val plan = remember(g.path) { s.deletePlan(g) }
    var step by remember(g.path) { mutableIntStateOf(0) }
    var backup by remember(g.path) { mutableStateOf(true) }
    var typed by remember(g.path) { mutableStateOf("") }
    BackHandler { s.deleting = null }
    val saver = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) s.exportSaves(g, uri) { ok -> if (ok) step = 1 }
    }
    Scrim {
        if (step == 0) {
            T("Borrar juego", 11.sp, FontWeight.Bold, c.muted, spacing = 1.sp)
            Spacer(Modifier.height(6.dp))
            T("¿Borrar ${g.title} por completo?", 18.sp, FontWeight.ExtraBold, bad)
            Spacer(Modifier.height(12.dp))
            Glass(Modifier.fillMaxWidth(), radius = 16.dp) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    T("Se borrará para siempre:", 12.sp, FontWeight.Bold, c.muted)
                    if (plan.deletesFolder) T("• La carpeta del juego y todo lo que tiene dentro", 13.sp, FontWeight.Medium)
                    else T("• La carpeta del juego NO se borra: ${plan.folderRefusal}", 13.sp, FontWeight.Medium, c.warn)
                    T("• Sus partidas guardadas" + (plan.saveDir?.let { " (${it.absolutePath})" } ?: ""), 13.sp, FontWeight.Medium)
                    T("• ${plan.shortcuts.size} accesos para launchers", 13.sp, FontWeight.Medium)
                    T("• Su carátula y lo que Gahvoria recuerda de él", 13.sp, FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(10.dp))
            T("Esto no se puede deshacer. Si solo quieres sacarlo de la lista, usa «Quitar de la biblioteca»: eso no borra nada.", 12.sp, FontWeight.Medium, c.muted)
            Spacer(Modifier.height(10.dp))
            Glass(Modifier.fillMaxWidth(), radius = 16.dp) {
                RowItem("Guardar antes un respaldo", "Un .zip con tus partidas, para no perderlas.", role = Role.Switch,
                    onClick = { backup = !backup }) { Toggle(backup) }
            }
            Spacer(Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PrimaryButton(if (backup) "Elegir dónde guardar el respaldo" else "Continuar", Modifier.fillMaxWidth()) {
                    if (backup) saver.launch("Respaldo ${io.github.nikalbazu24.gahvoria.core.Saves.safeName(g.title)}.zip") else step = 1
                }
                SecondaryButton("Cancelar", Modifier.fillMaxWidth()) { s.deleting = null }
            }
        } else {
            T("Última confirmación", 11.sp, FontWeight.Bold, c.muted, spacing = 1.sp)
            Spacer(Modifier.height(6.dp))
            T("Escribe el nombre del juego", 18.sp, FontWeight.ExtraBold, bad)
            Spacer(Modifier.height(8.dp))
            T("Para confirmar, escribe exactamente:", 12.sp, FontWeight.Medium, c.muted)
            T(g.title, 15.sp, FontWeight.ExtraBold)
            Spacer(Modifier.height(12.dp))
            val shape = RoundedCornerShape(14.dp)
            BasicTextField(
                typed, { typed = it }, singleLine = true,
                textStyle = TextStyle(color = c.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, fontFamily = Montserrat),
                cursorBrush = SolidColor(c.accent),
                modifier = Modifier.fillMaxWidth().background(c.glass, shape).border(1.dp, c.line, shape).padding(14.dp),
            )
            Spacer(Modifier.height(16.dp))
            val ok = GameRemoval.nameMatches(typed, g.title)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PrimaryButton("Borrar para siempre", Modifier.fillMaxWidth(), enabled = ok) { if (ok) s.deleteGame(g) }
                SecondaryButton("Cancelar", Modifier.fillMaxWidth()) { s.deleting = null }
            }
        }
    }
}

/** Saves were found in a backup or another emulator's folder: replace the ones that exist, keep them, or cancel. */
@Composable
fun ImportDialog(s: AppState, o: AppState.ImportOffer) {
    val c = LocalColors.current
    BackHandler { s.importOffer = null }
    Scrim {
        T("Importar partidas", 11.sp, FontWeight.Bold, c.muted, spacing = 1.sp)
        Spacer(Modifier.height(6.dp))
        T("Encontré ${o.items.size} partidas en ${o.source}", 18.sp, FontWeight.ExtraBold)
        Spacer(Modifier.height(10.dp))
        if (o.conflicts.isEmpty()) {
            T("Ninguna choca con las que ya tiene ${o.game.title}.", 13.sp, FontWeight.Medium, c.muted)
        } else {
            T("${o.conflicts.size} ya existen en ${o.game.title}: ${o.conflicts.take(4).joinToString(", ")}" +
                (if (o.conflicts.size > 4) "…" else "") + ". Si las reemplazas, las actuales se guardan con el final .antes.", 13.sp, FontWeight.Medium, c.muted)
        }
        Spacer(Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (o.conflicts.isEmpty()) PrimaryButton("Importar", Modifier.fillMaxWidth()) { s.importSaves(o, false) }
            else {
                PrimaryButton("Reemplazar y guardar las actuales como .antes", Modifier.fillMaxWidth()) { s.importSaves(o, true) }
                SecondaryButton("Importar solo las que faltan", Modifier.fillMaxWidth()) { s.importSaves(o, false) }
            }
            SecondaryButton("Cancelar", Modifier.fillMaxWidth()) { s.importOffer = null }
        }
    }
}

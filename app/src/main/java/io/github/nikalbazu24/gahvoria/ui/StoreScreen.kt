package io.github.nikalbazu24.gahvoria.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.nikalbazu24.gahvoria.core.StoreCatalog
import io.github.nikalbazu24.gahvoria.core.StoreGame

/**
 * The store: a list of fan games that points to each creator's official page (Gahvoria hosts nothing), plus installing
 * a downloaded .zip and a form for creators. Views: 0 list, 1 game, 2 install a .zip, 3 creator form.
 */
@Composable
fun StoreScreen(s: AppState) {
    val c = LocalColors.current
    var view by remember { mutableIntStateOf(0) }
    var sel by remember { mutableStateOf<StoreGame?>(null) }
    LaunchedEffect(Unit) { if (s.storeGames.isEmpty()) s.loadStore() }
    BackHandler(enabled = view != 0) { view = if (view == 2 && sel != null) 1 else 0 }
    when (view) {
        1 -> sel?.let { GameSheet(s, it, onBack = { view = 0 }, onInstall = { view = 2 }) } ?: run { view = 0 }
        2 -> ZipView(s, onBack = { view = if (sel != null) 1 else 0 })
        3 -> CreatorView(s, onBack = { view = 0 })
        else -> Page("Tienda") {
            var query by remember { mutableStateOf("") }
            var filter by remember { mutableIntStateOf(0) }
            SettingsSearch(query, "Buscar un juego") { query = it }
            Segmented(listOf("Todos", "Inglés", "Español", "Probados"), filter) { filter = it }
            val list = StoreCatalog.filter(s.storeGames, when (filter) { 1 -> "en"; 2 -> "es"; else -> "all" }, filter == 3, query)
            Glass(Modifier.fillMaxWidth()) {
                if (list.isEmpty()) RowItem("No hay juegos con ese filtro", if (filter == 3) "Todavía no hay juegos probados en la Thor." else null)
                list.forEachIndexed { i, g ->
                    if (i > 0) Divider()
                    Row(Modifier.fillMaxWidth().press(RoundedCornerShape(0.dp)) { sel = g; view = 1 }.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Cartridge(46.dp)
                        Spacer(Modifier.size(14.dp))
                        Column(Modifier.weight(1f)) {
                            T(g.title, 15.sp, FontWeight.ExtraBold)
                            T(g.creator, 12.sp, FontWeight.Medium, c.muted)
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Tag(langName(g), if (g.lang == "en") c.accent2 else c.muted)
                                Tag(if (g.tested) "Probado en la Thor" else "Sin probar", if (g.tested) c.ok else c.warn)
                            }
                        }
                        T("›", 18.sp, FontWeight.Bold, c.faint)
                    }
                }
            }
            Label("Ya lo descargaste")
            Glass(Modifier.fillMaxWidth()) {
                RowItem("Instalar un juego desde un archivo", "Para lo que bajaste de una página oficial (.zip, .7z o .rar). Gahvoria lo descomprime, lo revisa y lo agrega a tu biblioteca.",
                    onClick = { sel = null; view = 2 }) { T("›", 18.sp, FontWeight.Bold, c.faint) }
                Divider()
                RowItem("¿Eres creador? Envía tu juego", "Propones el enlace a tu página oficial. Se revisa antes de aparecer.",
                    onClick = { view = 3 }) { T("›", 18.sp, FontWeight.Bold, c.faint) }
            }
            T("Gahvoria no aloja ni distribuye juegos. Cada ficha lleva a la página oficial de su creador, donde se descarga. Los fan games son proyectos de sus creadores y pueden retirarse cuando ellos lo decidan.", 12.sp, FontWeight.Medium, c.muted)
        }
    }
}

private fun langName(g: StoreGame) = when (g.lang) { "en" -> "Inglés"; "es" -> "Español"; else -> "Idioma por confirmar" }

@Composable
private fun Tag(text: String, color: androidx.compose.ui.graphics.Color) {
    val c = LocalColors.current
    Box(Modifier.background(c.glass, RoundedCornerShape(99.dp)).padding(horizontal = 8.dp, vertical = 3.dp)) { T(text, 10.sp, FontWeight.ExtraBold, color) }
}

@Composable
private fun GameSheet(s: AppState, g: StoreGame, onBack: () -> Unit, onInstall: () -> Unit) {
    val c = LocalColors.current
    Page(g.title, onBack = onBack) {
        Glass(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Cartridge(64.dp)
                Spacer(Modifier.size(16.dp))
                Column {
                    T(g.creator, 14.sp, FontWeight.Bold)
                    T(g.description, 12.sp, FontWeight.Medium, c.muted)
                }
            }
            Divider()
            RowItem("Compatibilidad", if (g.tested) "Probado en la Thor." else "Sin probar: nadie lo ha abierto todavía en Gahvoria. Pruébalo y cuéntame cómo va.")
            Divider()
            RowItem("Idioma del juego", langName(g) + if (g.lang == "en" || g.lang == "?") ". Si está en otro idioma, puedes activar el traductor en sus opciones." else "")
            Divider()
            RowItem("Se descarga desde", g.host.ifEmpty { "Su página oficial" })
        }
        if (g.note.isNotEmpty()) T(g.note, 12.sp, FontWeight.Medium, c.warn)
        Label("Cómo jugarlo en Gahvoria")
        Glass(Modifier.fillMaxWidth()) {
            RowItem("1 · Descárgalo en su página oficial", "Se abre en tu navegador. Busca la versión de Windows (.zip, .7z o .rar).")
            Divider()
            RowItem("2 · Instálalo aquí", "Vuelve a Gahvoria y toca «Ya lo descargué». Lo encuentras en Descargas.")
            Divider()
            RowItem("3 · Juega", "Gahvoria revisa el juego y elige el motor. Si falla, prueba el otro.")
        }
        PrimaryButton("Abrir la página oficial", Modifier.fillMaxWidth()) { s.openPage(g.page) }
        SecondaryButton("Ya lo descargué: instalar", Modifier.fillMaxWidth()) { onInstall() }
    }
}

@Composable
private fun ZipView(s: AppState, onBack: () -> Unit) {
    val c = LocalColors.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) s.pickZip(uri) }
    val pick = s.zipPick
    val busy = s.zipProgress != null
    Page("Instalar desde archivo", onBack = onBack) {
        SecondaryButton("Elegir el archivo (.zip, .7z o .rar)", Modifier.fillMaxWidth(), enabled = !busy) {
            picker.launch(arrayOf("*/*"))
        }
        if (pick == null) {
            T("Elige el archivo que descargaste. Gahvoria no instala nada hasta que tú lo confirmes.", 13.sp, FontWeight.Medium, c.muted)
        } else {
            Glass(Modifier.fillMaxWidth()) {
                RowItem("Archivo", pick.name)
                if (pick.plan != null) {
                    Divider()
                    RowItem("Detectado", "Un juego de RPG Maker con ${pick.plan.files} archivos.")
                    Divider()
                    RowItem("Se instala en", "Juegos/ (en una carpeta nueva; si ya existe una con ese nombre, no se reemplaza nada). Tus partidas no se tocan.")
                }
            }
            pick.problem?.let { T(it, 13.sp, FontWeight.Medium, c.warn) }
            s.zipProgress?.let { T(it, 13.sp, FontWeight.SemiBold, c.muted) }
            if (pick.plan != null) PrimaryButton(if (busy) "Instalando…" else "Instalar y agregar a la biblioteca", Modifier.fillMaxWidth(), enabled = !busy) { s.installZip() }
        }
    }
}

@Composable
private fun CreatorView(s: AppState, onBack: () -> Unit) {
    val c = LocalColors.current
    var name by remember { mutableStateOf("") }
    var page by remember { mutableStateOf("") }
    var lang by remember { mutableIntStateOf(0) }
    var contact by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }
    var sending by remember { mutableStateOf(false) }
    Page("Envía tu juego", onBack = onBack) {
        Glass(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Field(name, "Nombre del juego") { name = it }
                Field(page, "Enlace a tu página oficial (https://…)") { page = it }
                Segmented(listOf("Inglés", "Español", "Otro"), lang) { lang = it }
                Field(contact, "Cómo contactarte (correo o usuario)") { contact = it }
            }
        }
        T("Solo se publica el enlace a tu página, tu nombre y una descripción corta. Puedes pedir que lo quitemos cuando quieras. Esto no sube ningún archivo.", 12.sp, FontWeight.Medium, c.muted)
        val ok = name.isNotBlank() && page.trim().startsWith("https://") && contact.isNotBlank() && !sending && !sent
        PrimaryButton(if (sent) "Enviado" else "Enviar para revisión", Modifier.fillMaxWidth(), enabled = ok) {
            sending = true
            s.sendProposal(name.trim(), page.trim(), listOf("Inglés", "Español", "Otro")[lang], contact.trim()) { done -> sending = false; sent = done }
        }
        if (!page.trim().startsWith("https://") && page.isNotEmpty()) T("El enlace debe empezar con https://", 12.sp, FontWeight.Medium, c.warn)
    }
}

@Composable
private fun Field(value: String, hint: String, onChange: (String) -> Unit) {
    val c = LocalColors.current
    val shape = RoundedCornerShape(14.dp)
    BasicTextField(
        value, onChange, singleLine = true,
        textStyle = TextStyle(color = c.ink, fontSize = 14.sp, fontWeight = FontWeight.Medium, fontFamily = Montserrat),
        cursorBrush = SolidColor(c.accent),
        modifier = Modifier.fillMaxWidth().background(c.glass, shape).border(1.dp, c.line, shape).padding(14.dp),
        decorationBox = { inner -> Box { if (value.isEmpty()) T(hint, 14.sp, FontWeight.Medium, c.faint); inner() } },
    )
}

package io.github.nikalbazu24.gahvoria.ui

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.nikalbazu24.gahvoria.core.Fix

/** A game closed with an error: the library blurs behind this window, which says what failed and what can be done. */
@Composable
fun ErrorDialog(s: AppState, e: ErrorReport) {
    val c = LocalColors.current
    var detail by remember(e) { mutableStateOf(false) }
    val shape = RoundedCornerShape(26.dp)
    val bad = if (c.dark) Color(0xFFFF8E8E) else Color(0xFFC62F3A)
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = .35f)).pointerInput(Unit) { detectTapGestures { } }.padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.widthIn(max = 460.dp).heightIn(max = 520.dp).background(c.bg2, shape).border(1.dp, c.line, shape)
                .verticalScroll(rememberScrollState()).padding(22.dp),
        ) {
            T("Error en " + e.game.title, 11.sp, FontWeight.Bold, c.muted, spacing = 1.sp)
            Spacer(Modifier.height(6.dp))
            T(e.diagnosis.title, 18.sp, FontWeight.ExtraBold, bad)
            Spacer(Modifier.height(12.dp))
            Glass(Modifier.fillMaxWidth(), radius = 16.dp) {
                Column(Modifier.padding(14.dp)) { T(e.diagnosis.detail, 13.sp, FontWeight.Medium) }
            }
            Spacer(Modifier.height(14.dp))
            T("¿Se puede corregir aquí?", 12.sp, FontWeight.Bold, c.muted)
            T(e.diagnosis.canFixHere, 14.sp, FontWeight.ExtraBold, if (e.diagnosis.fix == Fix.NONE) c.warn else c.ok)
            Spacer(Modifier.height(4.dp))
            T(e.diagnosis.advice, 13.sp, FontWeight.Medium, c.muted)
            if (detail) {
                Spacer(Modifier.height(12.dp))
                Glass(Modifier.fillMaxWidth(), radius = 14.dp) {
                    Box(Modifier.padding(12.dp)) { T(e.text.take(1500), 11.sp, FontWeight.Medium, c.muted) }
                }
            }
            Spacer(Modifier.height(18.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (e.diagnosis.fix == Fix.CLASSIC_ENGINE) {
                    PrimaryButton("Probar motor clásico", Modifier.fillMaxWidth()) { s.retryClassic(e) }
                    SecondaryButton("Enviar informe", Modifier.fillMaxWidth()) { s.sendReport(e.game, e.text); s.errorReport = null }
                } else {
                    PrimaryButton(if (s.canSendReports) "Enviar informe" else "Copiar informe", Modifier.fillMaxWidth()) {
                        s.sendReport(e.game, e.text); s.errorReport = null
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryButton(if (detail) "Ocultar detalle" else "Ver detalle", Modifier.weight(1f)) { detail = !detail }
                    SecondaryButton("Cerrar", Modifier.weight(1f)) { s.errorReport = null }
                }
            }
        }
    }
}

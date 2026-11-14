package io.github.nikalbazu24.gahvoria.data

import java.io.File

data class GameplayTranslationReport(
    val handled: Int,
    val unchanged: Int,
    val english: Int,
    val examples: List<String>,
    val export: String
) {
    val summary: String
        get() = "$handled procesadas · $unchanged iguales al original · $english con indicios de inglés"

    companion object {
        private const val LIMIT = 2500
        private val surfaces = mapOf(
            "dialogue" to "Diálogo", "choice" to "Opción", "window" to "Ventana",
            "help" to "Ayuda", "draw" to "Interfaz"
        )

        fun read(file: File): GameplayTranslationReport {
            if (!file.isFile) return GameplayTranslationReport(0, 0, 0, emptyList(), "")
            var handled = 0
            var unchanged = 0
            var english = 0
            val examples = mutableListOf<String>()
            val export = file.bufferedReader().useLines { lines ->
                lines.take(LIMIT).mapNotNull { line ->
                    val columns = line.split('\t', limit = 5)
                    if (columns.size != 5) return@mapNotNull null
                    val surface = surfaces[columns[1]] ?: return@mapNotNull null
                    val map = columns[2].toIntOrNull() ?: return@mapNotNull null
                    val issue = when (columns[0]) {
                        "handled" -> { handled++; return@mapNotNull null }
                        "unchanged" -> { unchanged++; "Igual al original" }
                        "english" -> { english++; "Posible inglés" }
                        else -> return@mapNotNull null
                    }
                    val location = listOfNotNull(surface, "Mapa $map".takeIf { map > 0 },
                        columns[3].takeIf { it.isNotEmpty() }).joinToString(" · ")
                    val text = columns[4].replace("\\\\", "\\")
                    val entry = "$issue · $location — $text"
                    if (examples.size < 8) examples.add(entry)
                    entry
                }.joinToString("\n", limit = LIMIT)
            }
            return GameplayTranslationReport(handled, unchanged, english, examples, export)
        }
    }
}

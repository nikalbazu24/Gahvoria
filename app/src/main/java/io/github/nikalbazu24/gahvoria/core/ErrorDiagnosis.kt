package io.github.nikalbazu24.gahvoria.core

/** What a player can do about an error, in order of how much Gahvoria can do by itself. */
enum class Fix { CLASSIC_ENGINE, MODERN_ENGINE, TRY_AGAIN, NONE }

/** The error in plain words: what failed, whether the app can fix it, and what to do next. */
data class Diagnosis(val title: String, val detail: String, val fix: Fix, val canFixHere: String, val advice: String)

/**
 * Reads the text a game left when it closed (the engine's report or Essentials' errorlog.txt) and explains it.
 * It only recognizes causes it can tell apart from the text; anything else is shown as a script error with its first
 * line, never guessed.
 */
object ErrorDiagnosis {

    /**
     * True when the error is the classic clash between Ruby versions (a byte read as a number, text and numbers mixed, syntax or
     * constants of the other Ruby), which the other engine usually avoids. [modernEngine] is the engine that failed.
     */
    fun engineClash(text: String, modernEngine: Boolean): Boolean {
        val low = text.lowercase()
        return if (modernEngine)
            listOf("no implicit conversion of integer into string", "no implicit conversion of string into integer", "can't be coerced into",
                "comparison of integer with string", "comparison of string with integer", "undefined method `ord'").any { low.contains(it) }
        else
            listOf("syntaxerror", "compile error", "uninitialized constant encoding", "undefined method `force_encoding'", "undefined method `each_char'",
                "undefined method `unpack1'", "unknown type of %string").any { low.contains(it) }
    }

    fun diagnose(text: String, modernEngine: Boolean, classicAvailable: Boolean): Diagnosis {
        val where = scriptAndLine(text)
        val low = text.lowercase()
        val missing = Regex("(?:No such file or directory|Unable to find file|ENOENT)[^\\n]*?[-@ ]\\s*['\"]?([\\w./ \\-]+\\.(?:png|jpg|jpeg|gif|bmp|ogg|mp3|wav|mid|rxdata|rvdata2|rb|txt|ini|dll))", RegexOption.IGNORE_CASE)
            .find(text)?.groupValues?.get(1)?.trim()
        return when {
            low.contains("nomemoryerror") || low.contains("out of memory") || low.contains("cannot allocate memory") ->
                Diagnosis("El juego se quedó sin memoria", "El equipo se quedó sin memoria mientras el juego cargaba o corría.", Fix.TRY_AGAIN,
                    "Puede ayudar", "Cierra otras apps y vuelve a abrirlo. Si se repite, envía el informe.")
            missing != null || low.contains("no such file or directory") || low.contains("unable to find file") ->
                Diagnosis("Falta un archivo del juego", "No encontré " + (missing ?: "un archivo que el juego pide") + ". Puede faltar en la carpeta o tener otro nombre.",
                    Fix.NONE, "No desde la app", "Envía el informe y reviso el detalle. Tus partidas no se tocan.")
            !modernEngine && low.contains("odd number list for hash") && low.contains("syntax error") ->
                Diagnosis("Este juego necesita el motor moderno", "El motor clásico no puede leer la sintaxis de sus tablas.",
                    Fix.MODERN_ENGINE, "Sí, con el motor moderno", "Reabre el juego con el motor moderno.")
            modernEngine && classicAvailable && (low.contains("syntaxerror") || low.contains("marshal") || low.contains("dump format error") || low.contains("incompatible")) ->
                Diagnosis("Este juego parece necesitar el motor clásico", "El juego usa algo de Ruby antiguo que el motor moderno no entiende" + (where?.let { " (" + it + ")" } ?: "") + ".",
                    Fix.CLASSIC_ENGINE, "Sí, probando otro motor", "Puedo reintentar con el motor clásico. Tus partidas no se tocan.")
            low.contains("fallo nativo") || low.contains("fallo del código nativo del motor") ->
                Diagnosis("El juego sufrió un fallo nativo", text.substringAfter("inesperada.").trim(), Fix.NONE,
                    "Requiere diagnóstico", "Envía el informe si se repite. La traza de Android ayuda a localizar el componente que falló.")
            low.contains("terminó de forma inesperada") ->
                Diagnosis("El juego se cerró de golpe", text.substringAfter("inesperada.").trim().ifEmpty { "Android cerró el proceso del juego sin que el juego dejara un error." },
                    Fix.TRY_AGAIN, "Puede ayudar", "Cierra otras apps y vuelve a abrirlo. Si se repite, envía el informe: dice por qué lo cerró Android.")
            low.contains("systemstackerror") || low.contains("stack level too deep") ->
                Diagnosis("Error dentro del script del juego", "El juego se llamó a sí mismo sin parar" + (where?.let { " (" + it + ")" } ?: "") + ".",
                    Fix.NONE, "No desde la app", "Es un fallo del juego o de un mod. Envía el informe para revisarlo.")
            else ->
                Diagnosis("El juego se cerró con un error", firstLine(text).ifEmpty { "El juego dejó un error sin descripción." } + (where?.let { "\n($it)" } ?: ""),
                    Fix.NONE, "No sé si se puede arreglar aquí", "Envía el informe y lo reviso con el detalle completo.")
        }
    }

    /** "Script 'Main' línea 12" from the engine's first line, or null. */
    fun scriptAndLine(text: String): String? {
        val m = Regex("Script '([^']+)' line (\\d+)").find(text) ?: return null
        return "script " + m.groupValues[1] + ", línea " + m.groupValues[2]
    }

    private fun firstLine(text: String): String =
        text.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() && !it.startsWith("Registro del juego") }.orEmpty().take(160)
}

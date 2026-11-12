package io.github.nikalbazu24.gahvoria.core

import java.io.File
import java.util.Locale

/** One saved error of a game: when it happened and the report text. */
data class LogEntry(val at: Long, val file: File) {
    fun text(): String = runCatching { file.readText() }.getOrDefault("")
    /** The first non-empty line, for lists. */
    fun headline(): String = text().lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() }.orEmpty().take(120)
}

/**
 * The creator's history of errors, one folder per game, newest first. It lives on the device only; nothing leaves
 * unless the creator sends a report. Identical consecutive errors are stored once and the oldest are dropped.
 */
class CreatorLog(private val root: File, private val keep: Int = 20) {

    fun key(path: String, title: String): String {
        val slug = title.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]+"), "-").trim('-').take(32).ifEmpty { "juego" }
        return slug + "-" + Integer.toHexString(path.hashCode())
    }

    fun list(key: String): List<LogEntry> =
        File(root, key).listFiles { f -> f.isFile && f.name.endsWith(".txt") }.orEmpty()
            .mapNotNull { f -> f.nameWithoutExtension.toLongOrNull()?.let { LogEntry(it, f) } }
            .sortedByDescending { it.at }

    /** Saves [text] as an error that happened at [at]. Returns null when the newest entry is the same text. */
    fun add(key: String, at: Long, text: String): LogEntry? {
        val dir = File(root, key).apply { mkdirs() }
        val all = list(key)
        if (all.firstOrNull()?.text() == text) return null
        var stamp = at
        while (File(dir, "$stamp.txt").exists()) stamp++
        val f = File(dir, "$stamp.txt")
        val tmp = File(dir, "$stamp.part")
        tmp.writeText(text)
        check(tmp.renameTo(f)) { "No se pudo guardar el informe" }
        list(key).drop(keep).forEach { it.file.delete() }
        return LogEntry(stamp, f)
    }

    fun count(key: String) = list(key).size

    fun clear(key: String) { File(root, key).listFiles()?.forEach { it.delete() }; File(root, key).delete() }
}

/** The texts a creator copies or sends. Plain functions so they can be tested. */
object CreatorText {

    val REQUEST_KINDS = listOf("Un error", "Va lento", "Se ve mal", "Otra cosa")

    fun summary(
        title: String, appVersion: String, essentials: String, gameVersion: String, engine: String, screen: String,
        lastLoad: String?, playMinutes: Long, folder: String, saves: Int,
    ): String = buildString {
        appendLine("Juego: $title")
        appendLine("Gahvoria: $appVersion")
        appendLine("Base: " + essentials.ifEmpty { "RPG Maker" })
        appendLine("Versión del juego: " + gameVersion.ifEmpty { "no indicada" })
        appendLine("Motor: $engine")
        appendLine("Pantalla: $screen")
        appendLine("Última carga: " + (lastLoad ?: "sin datos"))
        appendLine("Tiempo jugado: $playMinutes min")
        appendLine("Carpeta: $folder")
        append("Partidas: $saves")
    }

    /** The message a creator sends asking for an adjustment. [report] and [summary] are added only if the creator chose to. */
    fun request(kind: Int, description: String, summary: String?, report: String?): String = buildString {
        appendLine("Pedido de ajuste (creador)")
        appendLine("Tipo: " + REQUEST_KINDS.getOrElse(kind) { REQUEST_KINDS.last() })
        appendLine()
        appendLine(description.trim().take(2000))
        if (summary != null) { appendLine(); appendLine("— Datos del juego —"); appendLine(summary) }
        if (report != null) { appendLine(); appendLine("— Último informe —"); appendLine(report.take(6000)) }
    }

    /** The store proposal text, prefilled with what Gahvoria detected. */
    fun proposal(name: String, page: String, lang: String, contact: String, detected: String): String =
        "Propuesta para la tienda\nJuego: $name\nPágina oficial: $page\nIdioma: $lang\nContacto: $contact\nDetectado por Gahvoria: $detected\n"
}

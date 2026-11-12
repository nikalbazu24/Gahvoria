package io.github.nikalbazu24.gahvoria.core

import java.util.Locale

/** How long one game took to load, step by step (milliseconds). The game screen keeps these on the device only. */
data class LoadRecord(
    val at: Long, val versionCode: Int, val title: String, val engine: String,
    val files: Long, val scripts: Long, val start: Long, val total: Long,
)

/**
 * Reads load_times.tsv: one line per load, tab separated (when, version, game, engine, files, scripts, start, total).
 * They exist to compare one Gahvoria version with another and improve loading with data, not guesses.
 */
object LoadTimes {

    fun parse(text: String): List<LoadRecord> = text.lineSequence().mapNotNull { line ->
        val f = line.split('\t')
        if (f.size < 8) return@mapNotNull null
        runCatching {
            LoadRecord(f[0].toLong(), f[1].toInt(), f[2], f[3], f[4].toLong(), f[5].toLong(), f[6].toLong(), f[7].toLong())
        }.getOrNull()
    }.toList()

    /** The most recent load of that game. */
    fun lastFor(records: List<LoadRecord>, title: String): LoadRecord? =
        records.filter { it.title == title }.maxByOrNull { it.at }

    fun seconds(ms: Long): String = String.format(Locale.ROOT, "%.1f", ms / 1000.0).replace('.', ',') + " s"

    /** "12,4 s · archivos 3,1 · scripts 6,2 · apertura 3,1" */
    fun summary(r: LoadRecord): String =
        seconds(r.total) + " · archivos " + seconds(r.files).removeSuffix(" s") +
            " · scripts " + seconds(r.scripts).removeSuffix(" s") + " · apertura " + seconds(r.start).removeSuffix(" s")
}

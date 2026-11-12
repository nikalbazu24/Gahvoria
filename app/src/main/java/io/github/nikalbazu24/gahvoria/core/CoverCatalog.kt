package io.github.nikalbazu24.gahvoria.core

import java.text.Normalizer
import java.util.Locale

/** One game of the public catalog that has an image its creators published. */
data class CoverEntry(val id: String, val title: String, val aliases: List<String>, val urls: List<String>, val credit: String)

/**
 * The catalog of official covers (the same list Essentia's store uses). Only games in it can get a cover from the
 * Internet; every other game uses an image from its own folder, or the drawn cover.
 */
object CoverCatalog {

    const val URL = "https://raw.githubusercontent.com/nikalbazu24/Essentia/main/catalog/catalog.json"

    /** "Pokémon Añil 4.0" → "anil": names written in different ways give the same key. */
    fun norm(s: String?): String {
        if (s == null) return ""
        var t = Normalizer.normalize(s, Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "").lowercase(Locale.ROOT).replace('_', ' ')
        t = t.replace(Regex("\\bpokemon\\b"), " ").replace(Regex("\\b(v|ver|version)?\\s*\\d+(\\.\\d+)*[a-z]?\\b"), " ")
        return t.replace(Regex("[^a-z0-9]+"), "")
    }

    fun parse(text: String): List<CoverEntry> {
        val root = runCatching { Json(text).value() }.getOrNull() as? Map<*, *> ?: return emptyList()
        val games = root["games"] as? List<*> ?: return emptyList()
        return games.mapNotNull { g ->
            val m = g as? Map<*, *> ?: return@mapNotNull null
            val title = m["title"] as? String ?: return@mapNotNull null
            val urls = listOf(m["cover"], m["logo"]).mapNotNull { it as? String }.filter { it.startsWith("https://") }
            if (urls.isEmpty()) return@mapNotNull null
            CoverEntry(
                id = m["id"] as? String ?: title,
                title = title,
                aliases = (m["aliases"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                urls = urls,
                credit = m["media_credit"] as? String ?: "",
            )
        }
    }

    /** The entry whose title or alias matches the game's title or folder name. */
    fun match(entries: List<CoverEntry>, title: String, folderName: String): CoverEntry? {
        val a = norm(title)
        val b = norm(folderName)
        fun same(name: String): Boolean { val n = norm(name); return n.isNotEmpty() && (n == a || n == b) }
        return entries.firstOrNull { e -> same(e.title) || e.aliases.any(::same) }
    }

    /** A small JSON reader (objects, arrays, strings, numbers, true/false/null): enough for the catalog, no libraries. */
    internal class Json(private val s: String) {
        private var i = 0

        fun value(): Any? {
            ws()
            return when (val ch = s[i]) {
                '{' -> obj()
                '[' -> arr()
                '"' -> str()
                't' -> { i += 4; true }
                'f' -> { i += 5; false }
                'n' -> { i += 4; null }
                else -> if (ch == '-' || ch.isDigit()) num() else error("JSON inesperado")
            }
        }

        private fun ws() { while (i < s.length && s[i].isWhitespace()) i++ }

        private fun obj(): Map<String, Any?> {
            val m = LinkedHashMap<String, Any?>()
            i++
            ws()
            if (s[i] == '}') { i++; return m }
            while (true) {
                ws()
                val k = str()
                ws(); check(s[i++] == ':')
                m[k] = value()
                ws()
                when (s[i++]) { ',' -> continue; '}' -> return m; else -> error("JSON inesperado") }
            }
        }

        private fun arr(): List<Any?> {
            val l = ArrayList<Any?>()
            i++
            ws()
            if (s[i] == ']') { i++; return l }
            while (true) {
                l.add(value())
                ws()
                when (s[i++]) { ',' -> continue; ']' -> return l; else -> error("JSON inesperado") }
            }
        }

        private fun str(): String {
            check(s[i++] == '"')
            val b = StringBuilder()
            while (true) {
                val ch = s[i++]
                when (ch) {
                    '"' -> return b.toString()
                    '\\' -> when (val e = s[i++]) {
                        'n' -> b.append('\n'); 't' -> b.append('\t'); 'r' -> b.append('\r')
                        'b' -> b.append('\b'); 'f' -> b.append('\u000C')
                        'u' -> { b.append(s.substring(i, i + 4).toInt(16).toChar()); i += 4 }
                        else -> b.append(e)
                    }
                    else -> b.append(ch)
                }
            }
        }

        private fun num(): Double {
            val st = i
            while (i < s.length && (s[i].isDigit() || s[i] in "+-.eE")) i++
            return s.substring(st, i).toDouble()
        }
    }
}

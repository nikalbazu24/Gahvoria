package io.github.nikalbazu24.gahvoria.core

/**
 * What a game's own scripts say about the Ruby they were written for, so the right engine is chosen before opening
 * the game instead of after it fails. The modern engine runs Ruby 3, the classic one Ruby 1.8.7.
 *
 *  - [modern]: syntax or methods that only exist since Ruby 1.9. The classic engine cannot even read that script.
 *  - [classic]: habits of Ruby 1.8 (a byte read as a number, `$KCODE`...) that Ruby 3 does not keep.
 *
 * Each clue is a short phrase naming what was found and where, to show the player why an engine was chosen.
 */
data class RubyClues(val modern: List<String>, val classic: List<String>, val soft: List<String> = emptyList()) {
    companion object {
        val NONE = RubyClues(emptyList(), emptyList())

        private val MODERN = listOf(
            "claves de hash con dos puntos" to Regex("""(?:\{|,)\s*[a-zA-Z_]\w*:\s*:[a-zA-Z_]\w*"""),
            "%i[ ]" to Regex("""(?<![\w%])%[iI][\[({<]"""),
            "-> (lambda)" to Regex("""(?:=|,|\()\s*->\s*[({|\w]"""),
            "&. (navegación segura)" to Regex("""\w&\.\w"""),
        )

        /** Only fail when that line runs, and games often guard them ("if defined?(Encoding)"): a hint, not proof. */
        private val SOFT = listOf(
            "Encoding::" to Regex("""\bEncoding::\w+|\bEncoding\.default_"""),
            "require_relative" to Regex("""\brequire_relative\b"""),
            "each_with_object" to Regex("""\.each_with_object\b"""),
            "define_singleton_method" to Regex("""\bdefine_singleton_method\b"""),
            "File.write" to Regex("""\bFile\.write\("""),
        )

        private val CLASSIC = listOf(
            "KCODE" to Regex("""[$]KCODE"""),
            "getc como número" to Regex("""\.getc\b"""),
            "each_byte propio" to Regex("""\bdef\s+each_byte\b"""),
            "lectura de bytes (fgetb / fgetw / fgetdw)" to Regex("""\bdef\s+fget(?:b|w|dw)\b"""),
            "Fixnum / Bignum" to Regex("""\b(?:Fixnum|Bignum)\b"""),
        )

        /** [sections] are (script name, code). Comment lines are ignored. */
        fun scan(sections: List<Pair<String, String?>>): RubyClues {
            val modern = LinkedHashMap<String, String>()
            val classic = LinkedHashMap<String, String>()
            val soft = LinkedHashMap<String, String>()
            for ((name, code) in sections) {
                if (code == null) continue
                val body = code.lineSequence().filter { !it.trimStart().startsWith("#") }.joinToString("\n")
                for ((label, re) in MODERN) if (label !in modern && re.containsMatchIn(body)) modern[label] = name
                for ((label, re) in SOFT) if (label !in soft && re.containsMatchIn(body)) soft[label] = name
                for ((label, re) in CLASSIC) if (label !in classic && re.containsMatchIn(body)) classic[label] = name
            }
            fun show(m: Map<String, String>) = m.map { (label, where) -> "$label (en «${where.ifBlank { "sin nombre" }}»)" }
            return RubyClues(show(modern), show(classic), show(soft))
        }
    }
}

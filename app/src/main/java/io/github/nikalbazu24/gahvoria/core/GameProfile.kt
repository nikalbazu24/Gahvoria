package io.github.nikalbazu24.gahvoria.core

import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Locale
import java.util.zip.Inflater

/**
 * What the game's own scripts say about it: whether it is Pokémon Essentials, which version, and which of
 * the two engines suits it. Read from Data/Scripts.rxdata|rvdata|rvdata2 (Ruby Marshal + zlib). Nothing is modified.
 */
data class GameProfile(
    val essentials: Boolean = false,
    /** "17.2", "21.1"... or "" when the scripts do not say. */
    val essentialsVersion: String = "",
    /** The game's own GAME_VERSION, if its Settings script has one. */
    val gameVersion: String = "",
    /** Old Essentials window resizer (Sprite_Resizer), typical of v16-17. */
    val oldResizer: Boolean = false,
) {
    /** The Essentials version as a number (17.2, 21.1...), or 0 if unknown. */
    val versionNumber: Float
        get() = Regex("""(\d+(?:\.\d+)?)""").find(essentialsVersion)?.value?.toFloatOrNull() ?: 0f

    /** Essentials 17 and 18 were written for RPG Maker XP's Ruby 1.8: the classic engine from the start. */
    val recommendedEngine: String
        get() = if (essentials && !oldResizer && versionNumber >= 17f && versionNumber < 19f)
            Game.ENGINE_CLASSIC else Game.ENGINE_MODERN

    val label: String
        get() = when {
            !essentials -> ""
            essentialsVersion.isEmpty() -> "Pokémon Essentials"
            else -> "Essentials $essentialsVersion"
        }

    companion object {
        private val ESS_VERSION = Regex("""ESSENTIALS_?VERSION\s*=\s*"([^"]+)"""")
        // v19+: module Essentials ... VERSION = "21.1"
        private val ESS_MODULE = Regex("""module\s+Essentials\b[\s\S]{0,400}?\bVERSION\s*=\s*"([^"]+)"""")
        private val GAME_VERSION = Regex("""(?m)^\s*GAME_VERSION\s*=\s*["']([^"'\n]{1,24})["']""")

        fun read(dir: File): GameProfile {
            val scripts = scriptsFile(dir) ?: return GameProfile()
            return runCatching { parse(readSections(scripts.readBytes(), 400)) }.getOrDefault(GameProfile())
        }

        internal fun parse(sections: List<Pair<String, String?>>): GameProfile {
            var essentials = false
            var version = ""
            var gameVersion = ""
            var oldResizer = false
            for ((name, code) in sections) {
                if (name.equals("Sprite_Resizer", ignoreCase = true)) { oldResizer = true; essentials = true }
                if (name.startsWith("PokeBattle_") || name.startsWith("PScreen_") || name == "Compiler") essentials = true
                if (code == null) continue
                if (gameVersion.isEmpty()) GAME_VERSION.find(code)?.let { gameVersion = it.groupValues[1].trim() }
                if (version.isEmpty()) {
                    val v = ESS_VERSION.find(code) ?: ESS_MODULE.find(code)
                    if (v != null) { version = v.groupValues[1]; essentials = true }
                }
            }
            return GameProfile(essentials, version, gameVersion, oldResizer)
        }

        private fun child(dir: File, name: String): File? {
            val f = File(dir, name)
            if (f.exists()) return f
            return dir.list()?.firstOrNull { it.equals(name, ignoreCase = true) }?.let { File(dir, it) }
        }

        private fun scriptsFile(dir: File): File? {
            val data = child(dir, "Data") ?: return null
            return listOf("Scripts.rxdata", "Scripts.rvdata", "Scripts.rvdata2")
                .firstNotNullOfOrNull { n -> child(data, n)?.takeIf { it.isFile } }
        }

        /** [name, code] of each script section; code is only inflated for the first few and the likely ones. */
        internal fun readSections(data: ByteArray, maxCode: Int): List<Pair<String, String?>> {
            val root = Marshal(data).value() as? List<*> ?: return emptyList()
            val out = ArrayList<Pair<String, String?>>()
            var i = 0
            for (o in root) {
                val e = o as? List<*> ?: continue
                if (e.size < 3) continue
                val name = (e[1] as? ByteArray)?.toString(Charsets.UTF_8).orEmpty()
                val wanted = i++ < 12 || Regex(".*(settings|essentials|version).*").matches(name.lowercase(Locale.ROOT))
                val code = if (wanted && i <= maxCode) (e[2] as? ByteArray)?.let { inflate(it) } else null
                out.add(name to code)
            }
            return out
        }

        private fun inflate(z: ByteArray): String? = runCatching {
            val inf = Inflater()
            inf.setInput(z)
            val out = ByteArrayOutputStream()
            val buf = ByteArray(65536)
            while (!inf.finished()) {
                val n = inf.inflate(buf)
                if (n == 0 && (inf.needsInput() || inf.needsDictionary())) break
                out.write(buf, 0, n)
            }
            inf.end()
            out.toString("UTF-8")
        }.getOrNull()
    }

    /** The little of Ruby's Marshal format a scripts file needs; strings come back as bytes, other values are skipped. */
    private class Marshal(private val d: ByteArray) {
        private var pos = 2

        private fun b() = d[pos++].toInt() and 0xFF

        private fun num(): Int {
            val c = d[pos++].toInt()
            if (c == 0) return 0
            if (c >= 5) return c - 5
            if (c <= -5) return c + 5
            if (c > 0) {
                var x = 0
                for (i in 0 until c) x = x or (b() shl (8 * i))
                return x
            }
            var x = -1
            for (i in 0 until -c) { x = x and (0xFF shl (8 * i)).inv(); x = x or (b() shl (8 * i)) }
            return x
        }

        private fun bytes(): ByteArray {
            val n = num()
            require(n >= 0 && pos + n <= d.size)
            return d.copyOfRange(pos, pos + n).also { pos += n }
        }

        fun value(): Any? = when (val t = b().toChar()) {
            '0', 'T', 'F' -> null
            'i' -> { num(); null }
            '"' -> bytes()
            ':' -> { bytes(); null }
            ';' -> { num(); null }
            'I' -> { val v = value(); repeat(num()) { value(); value() }; v }
            '[' -> { val n = num(); require(n >= 0); List(n) { value() } }
            else -> throw IllegalStateException("marshal type $t")
        }
    }
}

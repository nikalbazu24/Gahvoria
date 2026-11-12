package io.github.nikalbazu24.gahvoria.core

import java.io.File
import java.util.Locale

/** How much a finding matters to a creator: PROBLEM stops the game, WARN may break it, INFO is worth knowing, OK is fine. */
enum class Level { PROBLEM, WARN, INFO, OK }

data class Finding(val level: Level, val title: String, val detail: String)

/**
 * The creator's pre-flight: reads the game's folder and scripts and says what could go wrong on Android before a
 * player finds it. It only reports what the files show; it does not run the game, so a clean check is not a promise
 * that the game works.
 */
object GameCheck {

    fun check(dir: File): List<Finding> {
        val names = dir.list()?.toList().orEmpty()
        val lower = names.associateBy { it.lowercase(Locale.ROOT) }
        val out = ArrayList<Finding>()
        val ini = lower["game.ini"]?.let { File(dir, it) }
        if (ini == null) out.add(Finding(Level.PROBLEM, "Falta Game.ini", "Sin ese archivo no se reconoce el juego. Debe estar en la carpeta principal."))
        else out.addAll(checkIni(runCatching { ini.readText() }.getOrDefault("")))
        val data = lower["data"]?.let { File(dir, it) }
        if (data == null || !data.isDirectory) out.add(Finding(Level.PROBLEM, "Falta la carpeta Data", "Ahí viven los mapas y los scripts del juego."))
        val archive = names.firstOrNull { val n = it.lowercase(Locale.ROOT); n.endsWith(".rgssad") || n.endsWith(".rgss2a") || n.endsWith(".rgss3a") }
        if (archive != null) {
            out.add(Finding(Level.INFO, "Archivo comprimido del juego ($archive)", "El motor puede leerlo, pero Gahvoria no puede revisar sus scripts: no habrá chequeo de scripts, versión ni extracto de errores."))
            return out
        }
        val scripts = GameProfile.allScripts(dir)
        if (scripts.isEmpty()) {
            out.add(Finding(Level.WARN, "No pude leer los scripts", "Data/Scripts.rxdata (o .rvdata/.rvdata2) falta o está dañado."))
            return out
        }
        out.addAll(checkScripts(scripts))
        return out
    }

    internal fun checkIni(text: String): List<Finding> {
        val out = ArrayList<Finding>()
        val rtp = Regex("(?im)^[ \\t]*RTP\\d?[ \\t]*=[ \\t]*(\\S[^\\r\\n]*)$").findAll(text).map { it.groupValues[1].trim() }.filter { it.isNotEmpty() }.toList()
        if (rtp.isNotEmpty()) out.add(Finding(Level.WARN, "Pide el RTP (${rtp.joinToString(", ")})",
            "Gahvoria no trae los recursos de RPG Maker. Si el juego los usa, inclúyelos dentro de su carpeta o saldrán archivos faltantes."))
        return out
    }

    internal fun checkScripts(sections: List<Pair<String, String>>): List<Finding> {
        val out = ArrayList<Finding>()
        fun where(re: Regex): List<String> = sections.filter { re.containsMatchIn(it.second) }.map { it.first.ifBlank { "(sin nombre)" } }
        fun names(l: List<String>) = l.take(4).joinToString(", ") + if (l.size > 4) " y ${l.size - 4} más" else ""

        val win = where(Regex("Win32API"))
        if (win.isNotEmpty()) out.add(Finding(Level.INFO, "Usa llamadas de Windows (Win32API)",
            "En: ${names(win)}. El motor incluye una parte de ellas; las que no, pueden fallar. Pruébalo en el teléfono."))
        val dll = where(Regex("""["'][^"'\n]*\.dll["']""", RegexOption.IGNORE_CASE)).filterNot { it in win }
        if (dll.isNotEmpty()) out.add(Finding(Level.WARN, "Carga archivos .dll", "En: ${names(dll)}. Android no puede cargarlos."))
        val paths = where(Regex("""["'][A-Za-z]:\\{1,2}[^"'\n]*["']"""))
        if (paths.isNotEmpty()) out.add(Finding(Level.WARN, "Rutas de Windows escritas a mano (C:\\…)", "En: ${names(paths)}. En Android esas carpetas no existen."))
        val exec = where(Regex("""\b(?:system|exec)\s*\(\s*["']|\bShellExecute\b|\bcmd\.exe\b""", RegexOption.IGNORE_CASE)).filterNot { it in win }
        if (exec.isNotEmpty()) out.add(Finding(Level.WARN, "Ejecuta programas del sistema", "En: ${names(exec)}. Android no ejecuta programas de Windows."))

        val profile = GameProfile.parse(sections.map { it.first to it.second })
        if (profile.essentials) out.add(Finding(Level.OK, profile.label + " detectado",
            "Motor sugerido: " + (if (profile.recommendedEngine == Game.ENGINE_CLASSIC) "clásico" else "moderno") + "."))
        if (profile.gameVersion.isEmpty()) out.add(Finding(Level.INFO, "No hay GAME_VERSION",
            "Define GAME_VERSION = \"1.0\" en los ajustes de tu juego: Gahvoria lo muestra y puede avisar cuando haya una versión nueva en la tienda."))
        else out.add(Finding(Level.OK, "Versión del juego ${profile.gameVersion}", "Es la que verán los jugadores y la tienda."))
        if (out.none { it.level == Level.PROBLEM || it.level == Level.WARN })
            out.add(0, Finding(Level.OK, "No encontré problemas en los scripts", "Esto solo mira los archivos: probarlo en el teléfono sigue siendo necesario."))
        return out
    }

    /** "2 avisos, 1 dato" for a list header. */
    fun summary(f: List<Finding>): String {
        val p = f.count { it.level == Level.PROBLEM }; val w = f.count { it.level == Level.WARN }
        return when {
            p > 0 -> "$p ${if (p == 1) "problema" else "problemas"}" + if (w > 0) " y $w ${if (w == 1) "aviso" else "avisos"}" else ""
            w > 0 -> "$w ${if (w == 1) "aviso" else "avisos"}"
            else -> "Sin problemas a la vista"
        }
    }
}

package io.github.nikalbazu24.gahvoria.core

import java.io.File
import java.util.Locale

/** The kind of game a folder holds, read from its files. Nothing is modified. */
enum class EngineKind(val label: String) {
    RPG_MAKER_XP("RPG Maker XP"),
    RPG_MAKER_VX("RPG Maker VX"),
    RPG_MAKER_VXACE("RPG Maker VX Ace"),
    GODOT("Godot"),
    UNITY("Unity"),
    UNKNOWN("Desconocido");

    val isRpgMaker get() = this == RPG_MAKER_XP || this == RPG_MAKER_VX || this == RPG_MAKER_VXACE
}

/** Can Gahvoria open it today? */
enum class Support { PLAYABLE, NEXT_PHASE, RESEARCH, NOT_A_GAME }

data class Inspection(
    val kind: EngineKind,
    /** Pokémon Essentials on top of RPG Maker (a guess from its typical files). */
    val essentials: Boolean,
    /** Title from Game.ini (RPG Maker) or project.godot, or the folder name. */
    val title: String,
) {
    val support: Support get() = when {
        kind.isRpgMaker -> Support.PLAYABLE
        kind == EngineKind.GODOT -> Support.NEXT_PHASE
        kind == EngineKind.UNITY -> Support.RESEARCH
        else -> Support.NOT_A_GAME
    }

    /** "RPG Maker XP + Essentials", "Godot"... */
    val engineLabel: String get() = if (essentials) kind.label + " + Essentials" else kind.label
}

/**
 * Reads a game folder: RPG Maker (Game.ini, Game.rgss*a, .rxdata, .rvdata or .rvdata2 files in Data), Godot
 * (project.godot or a .pck), Unity (a folder ending in _Data with Unity's files). These signals must still
 * be checked with real games on the Thor before relying on them.
 */
object GameInspector {

    fun inspect(dir: File): Inspection {
        val names = dir.list()?.toList().orEmpty()
        val lower = names.associateBy { it.lowercase(Locale.ROOT) }
        val kind = rpgMakerKind(dir, lower) ?: when {
            "project.godot" in lower || names.any { it.lowercase(Locale.ROOT).endsWith(".pck") } -> EngineKind.GODOT
            isUnity(dir, names) -> EngineKind.UNITY
            else -> EngineKind.UNKNOWN
        }
        val essentials = kind.isRpgMaker && looksLikeEssentials(dir, lower)
        val title = when {
            kind.isRpgMaker -> lower["game.ini"]?.let { iniTitle(File(dir, it)) }
            kind == EngineKind.GODOT -> lower["project.godot"]?.let { godotTitle(File(dir, it)) }
            else -> null
        }?.takeIf { it.isNotBlank() } ?: dir.name
        return Inspection(kind, essentials, title.trim())
    }

    private fun rpgMakerKind(dir: File, lower: Map<String, String>): EngineKind? {
        if ("game.rgss3a" in lower) return EngineKind.RPG_MAKER_VXACE
        if ("game.rgss2a" in lower) return EngineKind.RPG_MAKER_VX
        if ("game.rgssad" in lower) return EngineKind.RPG_MAKER_XP
        val data = lower["data"]?.let { File(dir, it) }
        val files = data?.list()?.map { it.lowercase(Locale.ROOT) }.orEmpty()
        return when {
            files.any { it.endsWith(".rvdata2") } -> EngineKind.RPG_MAKER_VXACE
            files.any { it.endsWith(".rvdata") } -> EngineKind.RPG_MAKER_VX
            files.any { it.endsWith(".rxdata") } -> EngineKind.RPG_MAKER_XP
            "game.ini" in lower && "mkxp.json" in lower -> EngineKind.RPG_MAKER_XP
            else -> null
        }
    }

    private fun looksLikeEssentials(dir: File, lower: Map<String, String>): Boolean {
        if ("pbs" in lower) return true
        val data = lower["data"]?.let { File(dir, it) } ?: return false
        val files = data.list()?.map { it.lowercase(Locale.ROOT) }.orEmpty()
        return files.any { it == "species.dat" || it == "messages.dat" || it == "pluginscripts.rxdata" || it == "pkmnanimations.rxdata" }
    }

    private fun isUnity(dir: File, names: List<String>): Boolean = names.any { name ->
        val f = File(dir, name)
        name.endsWith("_Data") && f.isDirectory &&
            (File(f, "globalgamemanagers").exists() || File(f, "data.unity3d").exists() || File(f, "Managed").isDirectory)
    } || names.any { it.equals("UnityPlayer.dll", ignoreCase = true) }

    internal fun iniTitle(ini: File): String? = runCatching {
        ini.readLines(charsetOf(ini)).firstNotNullOfOrNull { line ->
            val t = line.trim()
            if (t.startsWith("Title=", ignoreCase = true)) t.substringAfter('=').trim() else null
        }
    }.getOrNull()

    internal fun godotTitle(project: File): String? = runCatching {
        project.readLines().firstNotNullOfOrNull { line ->
            val t = line.trim()
            if (t.startsWith("config/name")) t.substringAfter('=').trim().trim('"') else null
        }
    }.getOrNull()

    /** Game.ini is UTF-8 in most fan games; old Windows games may use Windows-1252. */
    private fun charsetOf(f: File) = runCatching {
        val bytes = f.readBytes()
        java.nio.charset.StandardCharsets.UTF_8.newDecoder().decode(java.nio.ByteBuffer.wrap(bytes))
        Charsets.UTF_8
    }.getOrDefault(charset("windows-1252"))
}

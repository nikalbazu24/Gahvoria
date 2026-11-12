package io.github.nikalbazu24.gahvoria.core

import java.io.File
import java.nio.file.Files

/**
 * Deleting a game for good, with every trace: its folder, its saves, its shortcuts and its cover. It is a separate,
 * deliberate action from "Quitar de la biblioteca" (which never deletes anything) and needs two confirmations:
 * an explanation and then typing the game's name.
 */
object GameRemoval {

    /** What would be deleted, so the first confirmation can say it in plain words. Nothing is touched. */
    data class Plan(
        val gameDir: File,
        /** Null when the saves live inside the game folder (they go with it) or there is nothing to delete. */
        val saveDir: File?,
        val shortcuts: List<File>,
        /** Null when the folder can be deleted; otherwise why it will be kept. */
        val folderRefusal: String?,
    ) {
        val deletesFolder get() = folderRefusal == null
    }

    data class Result(val folderDeleted: Boolean, val files: Int, val shortcuts: Int, val failed: List<String>)

    /** The second confirmation: the player typed the game's name (spaces and capitals don't matter). */
    fun nameMatches(typed: String, title: String): Boolean {
        val a = typed.trim().lowercase()
        return a.isNotEmpty() && a == title.trim().lowercase()
    }

    /**
     * Why this folder must not be deleted, or null. A game folder is only deleted when it is clearly the game's own:
     * at least two levels under the storage root (never "Download" or the root itself) and not holding another game.
     */
    fun folderRefusal(dir: File, storageRoot: File, otherGames: List<String>): String? {
        if (!dir.isDirectory) return "La carpeta ya no existe."
        val d = runCatching { dir.canonicalFile }.getOrNull() ?: return "No pude comprobar la carpeta."
        val root = runCatching { storageRoot.canonicalFile }.getOrNull() ?: return "No pude comprobar el almacenamiento."
        val rel = runCatching { d.relativeTo(root).path }.getOrNull()
        if (rel == null || rel.startsWith("..") || rel.isEmpty()) return "Por seguridad no borro carpetas fuera del almacenamiento ni su raíz."
        if (rel.split(File.separatorChar).size < 2) return "Por seguridad no borro carpetas sueltas en la raíz del almacenamiento."
        for (o in otherGames) {
            val od = runCatching { File(o).canonicalFile }.getOrNull() ?: continue
            if (od != d && od.path.startsWith(d.path + File.separator)) return "Dentro hay otro juego de tu biblioteca."
        }
        return null
    }

    /** Shortcut files (in the shortcuts folder) that point to this game. */
    fun shortcutsFor(game: Game, folder: File?): List<File> {
        if (folder == null || !folder.isDirectory) return emptyList()
        return folder.listFiles()?.filter { f ->
            f.isFile && f.name.endsWith(Shortcuts.EXT, ignoreCase = true) &&
                runCatching { Shortcuts.pathIn(f.readText(Charsets.UTF_8)) }.getOrNull() == game.path
        }.orEmpty()
    }

    fun plan(game: Game, saveDir: File, shortcutsFolder: File?, storageRoot: File, otherGames: List<String>): Plan {
        val dir = File(game.path)
        val inside = runCatching { saveDir.canonicalPath == dir.canonicalPath }.getOrDefault(false)
        return Plan(dir, if (inside || !saveDir.exists()) null else saveDir, shortcutsFor(game, shortcutsFolder),
            folderRefusal(dir, storageRoot, otherGames))
    }

    /**
     * Deletes what the plan says. The saves go too (the player was offered a backup before). Links are removed
     * without following them, so a link inside the folder can never lead the deletion somewhere else.
     */
    fun execute(plan: Plan): Result {
        val failed = ArrayList<String>()
        var files = 0
        fun wipe(f: File) {
            if (Files.isSymbolicLink(f.toPath())) { if (f.delete()) files++ else failed += f.path; return }
            if (f.isDirectory) f.listFiles()?.forEach { wipe(it) }
            if (f.delete()) files++ else if (f.exists()) failed += f.path
        }
        if (plan.deletesFolder) wipe(plan.gameDir)
        plan.saveDir?.let { wipe(it) }
        var sc = 0
        for (s in plan.shortcuts) if (s.delete()) sc++ else failed += s.path
        return Result(plan.deletesFolder && !plan.gameDir.exists(), files, sc, failed)
    }
}

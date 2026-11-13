package io.github.nikalbazu24.gahvoria

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.DocumentsContract
import io.github.nikalbazu24.gahvoria.core.Game
import io.github.nikalbazu24.gahvoria.core.GameInspector
import io.github.nikalbazu24.gahvoria.core.Library
import io.github.nikalbazu24.gahvoria.core.Shortcuts
import io.github.nikalbazu24.gahvoria.data.Launcher
import io.github.nikalbazu24.gahvoria.data.Settings
import io.github.nikalbazu24.gahvoria.data.Storage
import java.io.File

/**
 * The door for other launchers (Díptico, Daijishō, ES-DE, Cocoon, iiSU...). It has no screen: it works out which game
 * was asked for, starts it and closes, so going back from the game returns to the launcher.
 *
 * It accepts VIEW with a shortcut file (file:// or content://), a game folder, any file inside the game (Game.ini), or
 * a text extra "path". Nothing is added to the library and no file is changed.
 */
class LaunchActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val problem = runCatching { open() }.getOrElse { "No pude abrir el juego: ${it.message ?: "error desconocido"}" }
        if (problem != null) {
            // Gahvoria's own screen says what happened, in its own style.
            startActivity(Intent(this, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_NOTICE, problem)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }
        finish()
    }

    /** Starts the game and returns null, or the reason it could not. */
    private fun open(): String? {
        val target = requested() ?: return "No entendí qué juego abrir. Crea los accesos otra vez desde Ajustes."
        if (!Storage.hasAccess(this)) return "Gahvoria necesita acceso a tus archivos para abrir juegos."
        val dir = Shortcuts.resolve(target) ?: return "No encuentro la carpeta del juego. ¿La moviste o borraste?"
        val now = System.currentTimeMillis()
        val library = Library(File(filesDir, "library.tsv"))
        val known = library.load().firstOrNull { it.path == dir.absolutePath }
        val game = known ?: GameInspector.inspect(dir).let {
            Game(dir.absolutePath, it.title, it.kind, it.essentials, addedAt = now)
        }
        if (!game.kind.isRpgMaker) return "Este motor todavía no se puede jugar en Gahvoria."
        if (known != null) {
            library.update(game.path) { it.copy(lastPlayed = now) }
            Settings(this).startSession(game.path, now)
        }
        Launcher.play(this, game, launchAt = now)
        return null
    }

    private fun requested(): File? {
        intent.getStringExtra("path")?.takeIf { it.isNotBlank() }?.let { return File(it) }
        val uri = intent.data ?: return null
        if (uri.scheme == "file") return uri.path?.let(::File)
        // content://: a document id that holds the real path, or a shortcut file read through the sender
        runCatching { DocumentsContract.getDocumentId(uri) }.getOrNull()
            ?.let { Storage.pathOfDocumentId(it) }?.takeIf { it.exists() }?.let { return it }
        val text = runCatching {
            contentResolver.openInputStream(uri)?.use { input ->
                val buf = ByteArray(4096)
                String(buf, 0, input.read(buf).coerceAtLeast(0), Charsets.UTF_8)
            }
        }.getOrNull()
        return text?.let(Shortcuts::pathIn)?.let(::File)
    }
}

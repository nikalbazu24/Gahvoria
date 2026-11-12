package io.github.nikalbazu24.gahvoria.data

import android.content.Context
import android.net.Uri
import com.github.junrar.Archive
import io.github.nikalbazu24.gahvoria.core.ArchiveKind
import io.github.nikalbazu24.gahvoria.core.ArchiveSource
import io.github.nikalbazu24.gahvoria.core.ZipSource
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.InputStream

/** Opens the compressed file the player chose (.zip, .7z or .rar) as an [ArchiveSource]. */
object Archives {

    /** What kind of file it is, from its first bytes. */
    fun kindOf(c: Context, uri: Uri): ArchiveKind {
        val head = ByteArray(8)
        val n = c.contentResolver.openInputStream(uri)?.use { it.read(head) } ?: return ArchiveKind.UNKNOWN
        return ArchiveKind.detect(head.copyOf(maxOf(n, 0)))
    }

    fun open(c: Context, uri: Uri, kind: ArchiveKind): ArchiveSource = when (kind) {
        ArchiveKind.ZIP -> ZipSource { c.contentResolver.openInputStream(uri)!! }
        ArchiveKind.SEVEN_Z -> SevenZ(c, uri)
        ArchiveKind.RAR -> Rar(c, uri)
        ArchiveKind.UNKNOWN -> throw IllegalArgumentException("Formato no compatible")
    }

    private class SevenZ(val c: Context, val uri: Uri) : ArchiveSource {
        override fun walk(visit: (String, Boolean, InputStream) -> Unit) {
            val pfd = c.contentResolver.openFileDescriptor(uri, "r") ?: error("No pude abrir el archivo")
            pfd.use {
                FileInputStream(it.fileDescriptor).channel.use { ch ->
                    val f = try { SevenZFile(ch) } catch (e: Exception) { throw IllegalStateException(friendly(e), e) }
                    f.use { z ->
                        while (true) {
                            val e = z.nextEntry ?: break
                            visit(e.name, e.isDirectory, if (e.isDirectory) ByteArrayInputStream(ByteArray(0)) else z.getInputStream(e))
                        }
                    }
                }
            }
        }

        private fun friendly(e: Exception) =
            if (e.javaClass.simpleName.contains("Password", true) || (e.message ?: "").contains("password", true))
                "Este .7z tiene contraseña y no puedo abrirlo." else "No pude abrir este .7z: ${e.message ?: "archivo dañado"}"
    }

    private class Rar(val c: Context, val uri: Uri) : ArchiveSource {
        override fun walk(visit: (String, Boolean, InputStream) -> Unit) {
            val input = c.contentResolver.openInputStream(uri) ?: error("No pude abrir el archivo")
            input.use {
                val archive = try { Archive(it) } catch (e: Exception) { throw IllegalStateException(friendly(e), e) }
                archive.use { a ->
                    for (h in a.fileHeaders) {
                        val name = h.fileName ?: continue
                        visit(name, h.isDirectory, if (h.isDirectory) ByteArrayInputStream(ByteArray(0)) else a.getInputStream(h))
                    }
                }
            }
        }

        private fun friendly(e: Exception): String {
            val m = (e.message ?: "") + e.javaClass.simpleName
            return when {
                m.contains("V5", true) || m.contains("RAR5", true) ->
                    "Este .rar es de la versión 5, que todavía no puedo abrir. Descomprímelo en un computador o vuelve a comprimirlo como .zip o .7z."
                m.contains("password", true) || m.contains("encrypt", true) -> "Este .rar tiene contraseña y no puedo abrirlo."
                else -> "No pude abrir este .rar: ${e.message ?: "archivo dañado"}"
            }
        }
    }
}

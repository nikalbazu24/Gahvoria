package io.github.nikalbazu24.gahvoria.data

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import java.io.File
import java.util.UUID

/** Access to the folders where games live. */
object Storage {

    fun root(): File = Environment.getExternalStorageDirectory()

    /** Essentia's RTP folder, shared so RTPs already copied there keep working. */
    fun rtpRoot(): File = File(root(), "Essentia/RTP")

    fun hasAccess(a: Activity): Boolean = if (Build.VERSION.SDK_INT >= 30) Environment.isExternalStorageManager()
    else a.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
        a.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    fun askAccess(a: Activity) {
        if (Build.VERSION.SDK_INT >= 30) {
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + a.packageName))
            runCatching { a.startActivity(intent) }.onFailure {
                a.startActivity(Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
            }
        } else if (Build.VERSION.SDK_INT >= 23) {
            a.requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 7)
        }
    }

    /** A folder chosen with the system picker, as a path ("primary:Juegos/Ópalo" -> /storage/emulated/0/Juegos/Ópalo). */
    fun pathOf(tree: Uri): File? {
        val id = runCatching { DocumentsContract.getTreeDocumentId(tree) }.getOrNull() ?: return null
        val volume = id.substringBefore(':')
        val rel = id.substringAfter(':', "")
        val base = if (volume.equals("primary", ignoreCase = true)) root() else File("/storage/$volume")
        return if (rel.isEmpty()) base else File(base, rel)
    }

    fun canWrite(dir: File): Boolean {
        val probe = File(dir, ".gahvoria-probe-" + UUID.randomUUID())
        return try { probe.createNewFile() } catch (e: Exception) { false } finally { probe.delete() }
    }
}

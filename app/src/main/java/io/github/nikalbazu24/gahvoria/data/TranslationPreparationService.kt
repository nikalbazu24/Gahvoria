package io.github.nikalbazu24.gahvoria.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.util.AtomicFile
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.hatkid.mkxpz.translate.TranslationPreparation
import io.github.nikalbazu24.gahvoria.MainActivity
import java.io.File
import java.io.IOException

class TranslationPreparationService : Service() {
    data class Status(
        val catalog: String, val phase: String, val reviewed: Int = 0,
        val total: Int = 0, val translated: Int = 0, val active: Boolean = false,
    ) {
        val description: String get() = if (total == 0) phase
            else "$phase · $reviewed de $total revisadas · $translated guardadas"
    }

    private val handler = Handler(Looper.getMainLooper())
    private val manager get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == PAUSE) {
            intent.getStringExtra(CATALOG)?.takeIf { it == current }?.let { pause() }
            if (current == null) stopSelf()
            return START_NOT_STICKY
        }
        val base = current
        if (base == null || intent?.getStringExtra(CATALOG) != base) {
            if (base == null) stopSelf()
            return START_NOT_STICKY
        }
        val title = intent?.getStringExtra(TITLE).orEmpty()
        val initial = state.value ?: Status(base, "Preparando…", active = true)
        try {
            startForeground(ID, notification(title, initial))
        } catch (failure: RuntimeException) {
            finishWork(initial.copy(phase = "Android no pudo iniciar la preparación", active = false), title)
            return START_NOT_STICKY
        }
        Thread({
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
            var status = initial
            try {
                val catalog = File(base + ".ready")
                val end = SystemClock.elapsedRealtime() + 180_000
                while (!catalog.isFile && !cancelled && !File(base + ".skip").exists()) {
                    if (File(base + ".error").exists() || SystemClock.elapsedRealtime() >= end) {
                        throw IOException("No pude leer los archivos del juego")
                    }
                    Thread.sleep(150)
                }
                if (!cancelled && catalog.isFile) {
                    TranslationPreparation.run(this, catalog, { cancelled }) { reviewed, total, translated ->
                        status = Status(base, "Traduciendo", reviewed, total, translated, true)
                        val update = status
                        handler.post {
                            state.value = update
                            runCatching { manager.notify(ID, notification(title, update)) }
                        }
                    }
                }
                val phase = when {
                    cancelled || !catalog.isFile -> "En pausa"
                    status.total == 0 -> "Sin frases detectadas"
                    File(base + ".limited").exists() -> "Catálogo parcial"
                    status.translated < status.total -> "Preparación terminada con frases pendientes"
                    else -> "Preparación terminada"
                }
                status = status.copy(phase = phase, active = false)
            } catch (failure: Exception) {
                Log.w("TranslationPreparation", "Preparation interrupted", failure)
                status = status.copy(phase = failure.message ?: "Error al preparar", active = false)
            } catch (failure: LinkageError) {
                Log.w("TranslationPreparation", "Model unavailable", failure)
                status = status.copy(phase = "Modelo Neural no disponible", active = false)
            }
            val result = status
            handler.post { finishWork(result, title) }
        }, "GahvoriaTranslationPreparation").start()
        return START_NOT_STICKY
    }

    private fun finishWork(status: Status, title: String) {
        val file = AtomicFile(File(status.catalog + ".status"))
        runCatching {
            val output = file.startWrite()
            try {
                output.write(listOf(status.phase.replace('\n', ' '), status.reviewed, status.total, status.translated)
                    .joinToString("\n").toByteArray(Charsets.UTF_8))
                file.finishWrite(output)
            } catch (failure: IOException) {
                file.failWrite(output)
                throw failure
            }
        }
        state.value = status
        current = null
        stopForeground(true)
        runCatching { manager.notify(DONE_ID, notification(title, status)) }
        stopSelf()
    }

    private fun notification(title: String, status: Status): Notification {
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(NotificationChannel(CHANNEL, "Preparación de traducciones", NotificationManager.IMPORTANCE_LOW))
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val open = PendingIntent.getActivity(this, 4121, Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), flags)
        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(title)
            .setContentText(status.description)
            .setContentIntent(open)
            .setOnlyAlertOnce(true)
            .setOngoing(status.active)
            .setAutoCancel(!status.active)
            .apply {
                if (status.active) {
                    val pause = PendingIntent.getService(this@TranslationPreparationService, 4122,
                        Intent(this@TranslationPreparationService, TranslationPreparationService::class.java)
                            .setAction(PAUSE).putExtra(CATALOG, status.catalog), flags)
                    setProgress(status.total, status.reviewed, status.total == 0)
                    addAction(android.R.drawable.ic_media_pause, "Pausar", pause)
                }
            }.build()
    }

    companion object {
        private const val CHANNEL = "translation_preparation"
        private const val CATALOG = "catalog"
        private const val TITLE = "title"
        private const val PAUSE = "pause"
        private const val ID = 4119
        private const val DONE_ID = 4120
        @Volatile private var current: String? = null
        @Volatile private var cancelled = false
        private val state = mutableStateOf<Status?>(null)
        val active get() = current != null

        fun status(catalog: File): Status? {
            val base = catalog.absolutePath
            return state.value?.takeIf { it.catalog == base } ?: runCatching {
                AtomicFile(File(base + ".status")).openRead().bufferedReader().use {
                    Status(base, it.readLine(), it.readLine().toInt(), it.readLine().toInt(), it.readLine().toInt())
                }
            }.getOrNull()
        }

        fun start(context: Context, catalog: File, title: String): Boolean {
            if (active) return false
            val base = catalog.absolutePath
            val previous = status(catalog)
            current = base
            cancelled = false
            state.value = (previous ?: Status(base, "")).copy(
                phase = if (File(base + ".ready").isFile) "Preparando…" else "Leyendo el juego…", active = true)
            return runCatching {
                ContextCompat.startForegroundService(context, Intent(context, TranslationPreparationService::class.java)
                    .putExtra(CATALOG, base).putExtra(TITLE, title))
            }.onFailure {
                current = null
                state.value = previous
            }.isSuccess
        }

        fun pause() {
            val base = current ?: return
            cancelled = true
            runCatching { File(base + ".skip").createNewFile() }
        }

        /** Called off the main thread so the service can release the model and memory before gameplay. */
        fun pauseAndWait(): Boolean {
            pause()
            val end = SystemClock.elapsedRealtime() + 5_000
            while (active && SystemClock.elapsedRealtime() < end) Thread.sleep(50)
            return !active
        }

        fun forgetStatus() { if (!active) state.value = null }
    }
}

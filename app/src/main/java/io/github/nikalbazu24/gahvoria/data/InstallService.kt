package io.github.nikalbazu24.gahvoria.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import io.github.nikalbazu24.gahvoria.MainActivity

/**
 * Keeps Gahvoria alive while a game is being unpacked, so the player can use the phone meanwhile (or leave the app)
 * without Android closing the work halfway. It does no unpacking itself: the install runs in its own thread
 * (AppState.installZip) and this only holds the process and shows the progress notification.
 */
class InstallService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val n = build(this, intent?.getStringExtra(TEXT) ?: "Instalando un juego…", 0, 0)
        runCatching {
            if (Build.VERSION.SDK_INT >= 29) startForeground(ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            else startForeground(ID, n)
        }
        return START_NOT_STICKY
    }

    companion object {
        private const val ID = 4107
        private const val DONE_ID = 4108
        private const val CHANNEL = "install"
        /** The "it's ready" notice pops up over whatever is on screen, a game included. */
        private const val DONE_CHANNEL = "install_done"
        private const val TEXT = "text"
        @Volatile private var lastUpdate = 0L

        private fun manager(c: Context) = c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        private fun channel(c: Context) {
            if (Build.VERSION.SDK_INT >= 26) {
                manager(c).createNotificationChannel(NotificationChannel(CHANNEL, "Instalación en curso", NotificationManager.IMPORTANCE_LOW))
                manager(c).createNotificationChannel(NotificationChannel(DONE_CHANNEL, "Juego listo", NotificationManager.IMPORTANCE_HIGH))
            }
        }

        private fun build(c: Context, text: String, done: Int, total: Int, ongoing: Boolean = true): Notification {
            channel(c)
            val open = PendingIntent.getActivity(c, 0, Intent(c, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            return NotificationCompat.Builder(c, if (ongoing) CHANNEL else DONE_CHANNEL)
                .setPriority(if (ongoing) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(if (ongoing) android.R.drawable.stat_sys_download else android.R.drawable.stat_sys_download_done)
                .setContentTitle("Gahvoria")
                .setContentText(text)
                .setContentIntent(open)
                .setOngoing(ongoing)
                .setOnlyAlertOnce(true)
                .setAutoCancel(!ongoing)
                .apply { if (ongoing) setProgress(total, done, total == 0) }
                .build()
        }

        /** Starts the foreground service. Never throws: without it the install still runs, it just may stop if the app is closed. */
        fun start(c: Context, text: String) {
            runCatching { ContextCompat.startForegroundService(c, Intent(c, InstallService::class.java).putExtra(TEXT, text)) }
        }

        /** Updates the progress at most twice a second. */
        fun progress(c: Context, text: String, done: Int, total: Int) {
            val now = System.currentTimeMillis()
            if (now - lastUpdate < 500 && done < total) return
            lastUpdate = now
            runCatching { manager(c).notify(ID, build(c, text, done, total)) }
        }

        /** Stops the service and leaves a normal notification with the result. */
        fun finish(c: Context, text: String) {
            runCatching { c.stopService(Intent(c, InstallService::class.java)) }
            runCatching { manager(c).notify(DONE_ID, build(c, text, 0, 0, ongoing = false)) }
        }
    }
}

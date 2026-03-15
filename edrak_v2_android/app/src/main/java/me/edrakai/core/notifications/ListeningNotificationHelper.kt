package me.edrakai.core.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Intent
import androidx.core.app.NotificationCompat
import me.edrakai.MainActivity
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Creates and manages the foreground listening notification.
 *
 * Channel: "edrak_listening" (importance HIGH so it's visible in status bar)
 * The notification shows a "Stop" action so the user can stop listening
 * without opening the app.
 */
@Singleton
class ListeningNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID         = "edrak_listening"
        const val NOTIFICATION_ID    = 1001
        const val ACTION_STOP        = "me.edrakai.action.STOP_LISTENING"
    }

    init {
        createChannel()
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Edrak Listening",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows while Edrak is recording and analyzing conversations"
            setShowBadge(false)
        }
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
        Timber.d("ListeningNotificationHelper: channel created")
    }

    fun buildListeningNotification(isListening: Boolean): Notification {
        val openIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stopIntent = PendingIntent.getBroadcast(
            context, 1,
            Intent(ACTION_STOP).setPackage(context.packageName),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle(if (isListening) "Edrak is listening…" else "Edrak — paused")
            .setContentText("Tap to open · conversations are being analyzed")
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setSilent(true)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Stop",
                stopIntent
            )
            .build()
    }
}

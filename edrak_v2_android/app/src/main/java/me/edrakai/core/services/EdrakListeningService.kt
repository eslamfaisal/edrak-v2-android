package me.edrakai.core.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * The backbone of Edrak V2.
 * Runs in the foreground even when the app UI is killed.
 * Full implementation in Prompt 3A.
 */
@AndroidEntryPoint
class EdrakListeningService : Service() {

    override fun onBind(intent: IBinder?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("EdrakListeningService: onStartCommand — stub (Phase 3A)")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("EdrakListeningService: onDestroy")
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, EdrakListeningService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, EdrakListeningService::class.java))
        }
    }
}

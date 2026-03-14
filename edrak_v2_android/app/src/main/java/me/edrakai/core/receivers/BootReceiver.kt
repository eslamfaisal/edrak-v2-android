package me.edrakai.core.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

/**
 * Triggered after device reboot.
 * Re-schedules any pending WorkManager sync jobs.
 * Full implementation in Prompt 4A.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Timber.d("BootReceiver: Device rebooted — stub (Phase 4A)")
            // TODO: Re-enqueue WorkManager fallback sync job in Phase 4A
        }
    }
}

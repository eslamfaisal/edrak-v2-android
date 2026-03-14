package me.edrakai.core.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * Receives FCM pushes from the backend.
 * Full implementation in Prompt 4B.
 */
@AndroidEntryPoint
class EdrakFcmService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("EdrakFcmService: New FCM token — stub (Phase 4B)")
        // TODO: Upload token to backend in Phase 4B
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d("EdrakFcmService: Message received type=${message.data["type"]} — stub (Phase 4B)")
        // TODO: Handle ACTION_DETECTED and DAILY_DIGEST_READY in Phase 4B
    }
}

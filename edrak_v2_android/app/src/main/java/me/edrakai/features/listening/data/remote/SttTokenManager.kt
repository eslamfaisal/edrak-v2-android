package me.edrakai.features.listening.data.remote

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the short-lived Google STT V2 OAuth token.
 *
 * Token is cached in DataStore and re-fetched 60 seconds before expiry.
 * Thread-safe: Hilt @Singleton + suspend functions.
 */
@Singleton
class SttTokenManager @Inject constructor(
    private val sttApiService: SttApiService,
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val KEY_TOKEN      = stringPreferencesKey("stt_token")
        private val KEY_EXPIRES_AT = longPreferencesKey("stt_token_expires_at")
        private const val REFRESH_BUFFER_MS = 60_000L  // refresh 60s before expiry
    }

    /**
     * Returns a valid STT token, refreshing if needed.
     * @throws Exception if the backend call fails
     */
    suspend fun getValidToken(): String {
        val prefs = dataStore.data.first()
        val cachedToken = prefs[KEY_TOKEN]
        val expiresAt   = prefs[KEY_EXPIRES_AT] ?: 0L

        if (cachedToken != null && System.currentTimeMillis() < expiresAt - REFRESH_BUFFER_MS) {
            Timber.d("SttTokenManager: using cached token (expires in ${(expiresAt - System.currentTimeMillis()) / 1000}s)")
            return cachedToken
        }

        Timber.d("SttTokenManager: refreshing STT token from backend")
        val response = sttApiService.getToken()
        val newExpiresAt = System.currentTimeMillis() + response.expiresInSeconds * 1000L

        dataStore.edit { prefs ->
            prefs[KEY_TOKEN]      = response.token
            prefs[KEY_EXPIRES_AT] = newExpiresAt
        }

        Timber.i("SttTokenManager: token refreshed, expires at $newExpiresAt")
        return response.token
    }

    /** Clears cached token (e.g. on logout). */
    suspend fun clearToken() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_TOKEN)
            prefs.remove(KEY_EXPIRES_AT)
        }
    }
}

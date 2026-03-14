package me.edrakai.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores and retrieves JWT tokens using EncryptedSharedPreferences.
 * This is the ONLY place tokens are stored. Never use plain SharedPreferences.
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "edrak_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun clearTokens() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_VOICE_SETUP_COMPLETE)
            .apply()
    }

    fun isLoggedIn(): Boolean = getAccessToken() != null

    fun setVoiceSetupComplete(complete: Boolean) {
        prefs.edit().putBoolean(KEY_VOICE_SETUP_COMPLETE, complete).apply()
    }

    fun isVoiceSetupComplete(): Boolean = prefs.getBoolean(KEY_VOICE_SETUP_COMPLETE, false)

    /**
     * Attempts to refresh the access token using the stored refresh token.
     * Returns true if refresh was successful.
     * NOTE: The actual refresh network call will be injected in the auth feature.
     * This is a placeholder that other features can expand.
     */
    fun refreshToken(): Boolean {
        // Will be implemented in auth feature (Prompt 1B)
        // For now: clear tokens to force re-login
        Timber.w("TokenManager: refreshToken called — not yet implemented, clearing tokens")
        clearTokens()
        return false
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_VOICE_SETUP_COMPLETE = "voice_setup_complete"
    }
}

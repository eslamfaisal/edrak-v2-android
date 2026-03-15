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
 * Stores and retrieves JWT tokens and user profile using EncryptedSharedPreferences.
 * This is the ONLY place tokens and user data are stored. Never use plain SharedPreferences.
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

    // ─── Token Storage ────────────────────────────────────────────────────────

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    // ─── User Profile Storage ─────────────────────────────────────────────────

    fun saveUserProfile(
        userId: String,
        email: String,
        displayName: String,
        timezone: String? = null,
        firebaseCustomToken: String? = null,
    ) {
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_DISPLAY_NAME, displayName)
            .putString(KEY_USER_TIMEZONE, timezone)
            .putString(KEY_FIREBASE_CUSTOM_TOKEN, firebaseCustomToken)
            .apply()
    }

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
    fun getUserDisplayName(): String? = prefs.getString(KEY_USER_DISPLAY_NAME, null)
    fun getUserTimezone(): String? = prefs.getString(KEY_USER_TIMEZONE, null)
    fun getFirebaseCustomToken(): String? = prefs.getString(KEY_FIREBASE_CUSTOM_TOKEN, null)

    // ─── Session Control ──────────────────────────────────────────────────────

    fun clearTokens() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_DISPLAY_NAME)
            .remove(KEY_USER_TIMEZONE)
            .remove(KEY_FIREBASE_CUSTOM_TOKEN)
            .remove(KEY_VOICE_SETUP_COMPLETE)
            .apply()
    }

    fun isLoggedIn(): Boolean = getAccessToken() != null

    fun setVoiceSetupComplete(complete: Boolean) {
        prefs.edit().putBoolean(KEY_VOICE_SETUP_COMPLETE, complete).apply()
    }

    fun isVoiceSetupComplete(): Boolean = prefs.getBoolean(KEY_VOICE_SETUP_COMPLETE, false)

    fun refreshToken(): Boolean {
        Timber.w("TokenManager: refreshToken called — not yet implemented, clearing tokens")
        clearTokens()
        return false
    }

    companion object {
        private const val KEY_ACCESS_TOKEN         = "access_token"
        private const val KEY_REFRESH_TOKEN        = "refresh_token"
        private const val KEY_USER_ID              = "user_id"
        private const val KEY_USER_EMAIL           = "user_email"
        private const val KEY_USER_DISPLAY_NAME    = "user_display_name"
        private const val KEY_USER_TIMEZONE        = "user_timezone"
        private const val KEY_FIREBASE_CUSTOM_TOKEN = "firebase_custom_token"
        private const val KEY_VOICE_SETUP_COMPLETE = "voice_setup_complete"
    }
}

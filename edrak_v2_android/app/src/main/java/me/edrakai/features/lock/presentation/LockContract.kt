package me.edrakai.features.lock.presentation

// ─── State ────────────────────────────────────────────────────────────────────

data class LockState(
    val displayName: String       = "",
    val passphrase: String        = "",         // shown on screen so user knows what to say
    val status: LockStatus        = LockStatus.IDLE,
    val liveConfidence: Float     = 0f,
    val statusMessage: String     = "Say your passphrase to unlock",
    val attemptCount: Int         = 0,
    val isRecording: Boolean      = false,
)

enum class LockStatus {
    IDLE,        // waiting to start
    RECORDING,   // AudioRecord capturing
    ANALYZING,   // chunk sent to backend
    MATCHED,     // success — navigating
    FAILED,      // this attempt failed — can retry
    LOCKED_OUT,  // 3 failures — must re-login
}

// ─── Events ───────────────────────────────────────────────────────────────────

sealed class LockEvent {
    data object StartRecording   : LockEvent()
    data object StopAndVerify    : LockEvent()
    data object Retry            : LockEvent()
    data object ForgotVoice      : LockEvent()  // → Login (clears session)
}

// ─── Effects (one-time) ───────────────────────────────────────────────────────

sealed class LockEffect {
    data object NavigateToHome   : LockEffect()
    data object NavigateToLogin  : LockEffect()
    data class ShowError(val message: String) : LockEffect()
}

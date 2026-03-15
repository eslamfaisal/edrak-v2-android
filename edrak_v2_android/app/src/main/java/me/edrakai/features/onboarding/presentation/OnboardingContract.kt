package me.edrakai.features.onboarding.presentation

/** MVI Contract for the Voice Fingerprint Onboarding flow. */

// ─── State ────────────────────────────────────────────────────────────────────

data class OnboardingState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    // Passphrase setup
    val passphraseInput: String = "",
    val passphraseError: String? = null,
    // Enrollment
    val currentPhraseIndex: Int = 0,
    val recordedCount: Int = 0,
    val isRecording: Boolean = false,
    val isUploading: Boolean = false,
    val uploadError: String? = null,
    // Live Verification
    val verificationStatus: VerificationStatus = VerificationStatus.IDLE,
    val liveConfidence: Float = 0f,
    val consecutiveMatches: Int = 0,
    val verificationMessage: String = "Tap the mic and speak naturally",
)

enum class OnboardingStep {
    WELCOME,
    PASSPHRASE_SETUP,  // user types their voice PIN phrase
    RECORDING,         // 3 enrollment phrases (phrase 0 = passphrase)
    VERIFICATION,      // real-time biometric confirm
    SUCCESS,
}

enum class VerificationStatus { IDLE, LISTENING, ANALYZING, MATCHED, NO_MATCH }

// ─── Events ───────────────────────────────────────────────────────────────────

sealed class OnboardingEvent {
    data object StartSetup                             : OnboardingEvent()
    data class  PassphraseChanged(val text: String)   : OnboardingEvent()
    data object ConfirmPassphrase                      : OnboardingEvent()
    data object StartRecording                         : OnboardingEvent()
    data object StopRecording                          : OnboardingEvent()
    data object RetryUpload                            : OnboardingEvent()
    data object StartLiveVerification                  : OnboardingEvent()
    data object StopLiveVerification                   : OnboardingEvent()
    data object Continue                               : OnboardingEvent()
}

// ─── Effects (one-time) ───────────────────────────────────────────────────────

sealed class OnboardingEffect {
    data object NavigateToHome : OnboardingEffect()
    data class ShowError(val message: String) : OnboardingEffect()
}

package me.edrakai.features.onboarding.presentation

/** MVI Contract for the Voice Fingerprint Onboarding flow. */

// ─── State ────────────────────────────────────────────────────────────────────

data class OnboardingState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val currentPhraseIndex: Int = 0,
    val recordedCount: Int = 0,          // how many phrases recorded so far
    val isRecording: Boolean = false,
    val isUploading: Boolean = false,
    val uploadError: String? = null,
)

enum class OnboardingStep { WELCOME, RECORDING, SUCCESS }

// ─── Events ───────────────────────────────────────────────────────────────────

sealed class OnboardingEvent {
    data object StartSetup       : OnboardingEvent()
    data object StartRecording   : OnboardingEvent()
    data object StopRecording    : OnboardingEvent()
    data object RetryUpload      : OnboardingEvent()
    data object Continue         : OnboardingEvent()   // final → Home
}

// ─── Effects (one-time) ───────────────────────────────────────────────────────

sealed class OnboardingEffect {
    data object NavigateToHome : OnboardingEffect()
    data class ShowError(val message: String) : OnboardingEffect()
}

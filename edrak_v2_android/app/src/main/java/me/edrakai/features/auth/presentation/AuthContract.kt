package me.edrakai.features.auth.presentation

/** Represents everything the UI needs to render. */
data class AuthState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val nameError: String? = null,
)

/** User intent → ViewModel. */
sealed interface AuthEvent {
    data class EmailChanged(val value: String) : AuthEvent
    data class PasswordChanged(val value: String) : AuthEvent
    data class NameChanged(val value: String) : AuthEvent
    data class ConfirmPasswordChanged(val value: String) : AuthEvent
    data object LoginClicked : AuthEvent
    data object RegisterClicked : AuthEvent
}

/** One-time side-effects that the UI reacts to (navigation, snackbar). */
sealed interface AuthEffect {
    data object NavigateToHome : AuthEffect
    data object NavigateToVoiceSetup : AuthEffect
    data class ShowError(val message: String) : AuthEffect
}

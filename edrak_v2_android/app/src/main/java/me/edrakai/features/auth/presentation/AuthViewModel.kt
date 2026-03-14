package me.edrakai.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.edrakai.core.security.TokenManager
import me.edrakai.features.auth.domain.usecase.LoginUseCase
import me.edrakai.features.auth.domain.usecase.RegisterUseCase
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    // Channel is used for one-time effects (navigation, toasts) — never replay
    private val _effects = Channel<AuthEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.EmailChanged           -> _state.update { it.copy(email = event.value, emailError = null) }
            is AuthEvent.PasswordChanged        -> _state.update { it.copy(password = event.value, passwordError = null) }
            is AuthEvent.NameChanged            -> _state.update { it.copy(name = event.value, nameError = null) }
            is AuthEvent.ConfirmPasswordChanged -> _state.update { it.copy(confirmPassword = event.value) }
            is AuthEvent.LoginClicked           -> login()
            is AuthEvent.RegisterClicked        -> register()
        }
    }

    private fun login() {
        val s = _state.value
        if (!validate(email = s.email, password = s.password)) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            loginUseCase(s.email, s.password)
                .onSuccess {
                    val effect = if (tokenManager.isVoiceSetupComplete())
                        AuthEffect.NavigateToHome
                    else
                        AuthEffect.NavigateToVoiceSetup
                    _effects.send(effect)
                }
                .onFailure { err ->
                    _effects.send(AuthEffect.ShowError(err.message ?: "Login failed"))
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun register() {
        val s = _state.value
        if (!validateRegister(s.name, s.email, s.password, s.confirmPassword)) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            registerUseCase(s.name, s.email, s.password)
                .onSuccess {
                    // Fresh registration → always go to voice setup
                    _effects.send(AuthEffect.NavigateToVoiceSetup)
                }
                .onFailure { err ->
                    _effects.send(AuthEffect.ShowError(err.message ?: "Registration failed"))
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    // ─── Validation ──────────────────────────────────────────────────────────

    private fun validate(email: String, password: String): Boolean {
        var valid = true
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.update { it.copy(emailError = "Enter a valid email") }
            valid = false
        }
        if (password.length < 8) {
            _state.update { it.copy(passwordError = "Password must be at least 8 characters") }
            valid = false
        }
        return valid
    }

    private fun validateRegister(name: String, email: String, password: String, confirm: String): Boolean {
        var valid = validate(email, password)
        if (name.isBlank()) {
            _state.update { it.copy(nameError = "Name is required") }
            valid = false
        }
        if (password != confirm) {
            _state.update { it.copy(passwordError = "Passwords do not match") }
            valid = false
        }
        return valid
    }
}

package me.edrakai.features.auth.domain.usecase

import me.edrakai.features.auth.domain.model.AuthResult
import me.edrakai.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthResult> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password must not be empty"))
        }
        return repository.login(email.trim(), password)
    }
}

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String
    ): Result<AuthResult> {
        if (name.isBlank() || email.isBlank() || password.length < 8) {
            return Result.failure(IllegalArgumentException("Invalid registration data"))
        }
        return repository.register(name.trim(), email.trim(), password)
    }
}

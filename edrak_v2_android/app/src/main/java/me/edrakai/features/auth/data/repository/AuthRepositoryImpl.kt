package me.edrakai.features.auth.data.repository

import me.edrakai.core.security.TokenManager
import me.edrakai.features.auth.data.remote.AuthApiService
import me.edrakai.features.auth.data.remote.LoginRequest
import me.edrakai.features.auth.data.remote.RegisterRequest
import me.edrakai.features.auth.domain.model.AuthResult
import me.edrakai.features.auth.domain.model.AuthTokens
import me.edrakai.features.auth.domain.model.LoggedInUser
import me.edrakai.features.auth.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApiService,
    private val tokenManager: TokenManager,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthResult> = runCatching {
        val response = api.login(LoginRequest(email, password))
        if (!response.success) error(response.message)

        val data = response.data
        val tokens = AuthTokens(
            accessToken        = data.accessToken,
            refreshToken       = data.refreshToken,
            firebaseCustomToken = data.firebaseCustomToken,
        )
        val user = LoggedInUser(
            id          = data.user.id,
            email       = data.user.email,
            displayName = data.user.displayName,
            timezone    = data.user.timezone,
        )

        // Persist tokens + user profile atomically
        tokenManager.saveTokens(data.accessToken, data.refreshToken)
        tokenManager.saveUserProfile(
            userId              = user.id,
            email               = user.email,
            displayName         = user.displayName,
            timezone            = user.timezone,
            firebaseCustomToken = data.firebaseCustomToken,
        )

        AuthResult(tokens = tokens, user = user)
    }

    override suspend fun register(name: String, email: String, password: String): Result<AuthResult> = runCatching {
        val response = api.register(RegisterRequest(name, email, password))
        if (!response.success) error(response.message)

        val data = response.data
        val tokens = AuthTokens(
            accessToken        = data.accessToken,
            refreshToken       = data.refreshToken,
            firebaseCustomToken = data.firebaseCustomToken,
        )
        val user = LoggedInUser(
            id          = data.user.id,
            email       = data.user.email,
            displayName = data.user.displayName,
            timezone    = data.user.timezone,
        )

        tokenManager.saveTokens(data.accessToken, data.refreshToken)
        tokenManager.saveUserProfile(
            userId              = user.id,
            email               = user.email,
            displayName         = user.displayName,
            timezone            = user.timezone,
            firebaseCustomToken = data.firebaseCustomToken,
        )

        AuthResult(tokens = tokens, user = user)
    }

    override suspend fun logout() {
        tokenManager.clearTokens()
    }
}

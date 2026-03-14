package me.edrakai.features.auth.data.repository

import me.edrakai.core.security.TokenManager
import me.edrakai.features.auth.data.remote.AuthApiService
import me.edrakai.features.auth.data.remote.LoginRequest
import me.edrakai.features.auth.data.remote.RegisterRequest
import me.edrakai.features.auth.domain.model.AuthTokens
import me.edrakai.features.auth.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthTokens> = runCatching {
        val response = api.login(LoginRequest(email, password))
        val tokens = AuthTokens(response.accessToken, response.refreshToken)
        tokenManager.saveTokens(tokens.accessToken, tokens.refreshToken)
        tokens
    }

    override suspend fun register(name: String, email: String, password: String): Result<AuthTokens> = runCatching {
        val response = api.register(RegisterRequest(name, email, password))
        val tokens = AuthTokens(response.accessToken, response.refreshToken)
        tokenManager.saveTokens(tokens.accessToken, tokens.refreshToken)
        tokens
    }

    override suspend fun logout() {
        tokenManager.clearTokens()
    }
}

package me.edrakai.features.auth.domain.repository

import kotlinx.coroutines.flow.Flow
import me.edrakai.features.auth.domain.model.AuthTokens

/** Pure interface — no Android/Retrofit imports. */
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthTokens>
    suspend fun register(name: String, email: String, password: String): Result<AuthTokens>
    suspend fun logout()
}

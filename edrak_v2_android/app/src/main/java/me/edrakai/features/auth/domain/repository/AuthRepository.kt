package me.edrakai.features.auth.domain.repository

import me.edrakai.features.auth.domain.model.AuthResult

/** Pure interface — no Android/Retrofit imports. */
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthResult>
    suspend fun register(name: String, email: String, password: String): Result<AuthResult>
    suspend fun logout()
}

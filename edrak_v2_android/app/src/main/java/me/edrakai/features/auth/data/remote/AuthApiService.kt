package me.edrakai.features.auth.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): LoginResponse
}

// ─── Request DTOs ────────────────────────────────────────────────────────────

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

// ─── Response DTOs ───────────────────────────────────────────────────────────

@Serializable
data class LoginResponse(
    @SerialName("accessToken")  val accessToken: String,
    @SerialName("refreshToken") val refreshToken: String,
    @SerialName("userId")       val userId: Long,
    @SerialName("name")         val name: String,
    @SerialName("email")        val email: String
)

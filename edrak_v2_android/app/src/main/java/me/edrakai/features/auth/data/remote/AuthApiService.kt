package me.edrakai.features.auth.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthApiResponse

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthApiResponse
}

// ─── Request DTOs ────────────────────────────────────────────────────────────

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    @SerialName("displayName") val name: String,
    val email: String,
    val password: String
)

// ─── Response DTOs (matches actual backend envelope) ─────────────────────────

/**
 * Top-level envelope: {"success": true, "message": "...", "data": {...}, "timestamp": "..."}
 */
@Serializable
data class AuthApiResponse(
    @SerialName("success")   val success: Boolean,
    @SerialName("message")   val message: String,
    @SerialName("data")      val data: AuthResponseData,
    @SerialName("timestamp") val timestamp: String? = null,
)

@Serializable
data class AuthResponseData(
    @SerialName("accessToken")        val accessToken: String,
    @SerialName("refreshToken")       val refreshToken: String,
    @SerialName("expiresIn")          val expiresIn: Long,
    @SerialName("tokenType")          val tokenType: String,
    @SerialName("firebaseCustomToken") val firebaseCustomToken: String? = null,
    @SerialName("user")               val user: RemoteUser,
)

@Serializable
data class RemoteUser(
    @SerialName("id")           val id: String,
    @SerialName("email")        val email: String,
    @SerialName("displayName")  val displayName: String,
    @SerialName("timezone")     val timezone: String? = null,
    @SerialName("authProvider") val authProvider: String? = null,
)

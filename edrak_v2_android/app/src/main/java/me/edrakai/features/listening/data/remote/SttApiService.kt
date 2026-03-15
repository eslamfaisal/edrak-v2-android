package me.edrakai.features.listening.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.GET

interface SttApiService {

    @GET("api/v1/stt/token")
    suspend fun getToken(): SttTokenResponse
}

@Serializable
data class SttTokenResponse(
    val token: String,
    val expiresInSeconds: Long = 3600L
)

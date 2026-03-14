package me.edrakai.features.onboarding.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface VoiceApiService {
    @Multipart
    @POST("api/v2/voice/enroll")
    suspend fun enroll(@Part audio: MultipartBody.Part): EnrollResponse

    @GET("api/v2/voice/status")
    suspend fun getStatus(): EnrollStatusResponse
}

@Serializable
data class EnrollResponse(
    @SerialName("enrolled")   val enrolled: Boolean,
    @SerialName("enrolledAt") val enrolledAt: String
)

@Serializable
data class EnrollStatusResponse(
    @SerialName("enrolled")   val enrolled: Boolean,
    @SerialName("enrolledAt") val enrolledAt: String?
)

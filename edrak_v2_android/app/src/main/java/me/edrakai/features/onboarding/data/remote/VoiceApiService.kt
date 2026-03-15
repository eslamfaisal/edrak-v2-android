package me.edrakai.features.onboarding.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import okhttp3.MultipartBody

interface VoiceApiService {
    @Multipart
    @POST("api/v1/voice/enroll")
    suspend fun enroll(@Part audio: MultipartBody.Part): VoiceApiEnvelope<EnrollmentStatusDto>

    @GET("api/v1/voice/status")
    suspend fun getStatus(): VoiceApiEnvelope<EnrollmentStatusDto>

    @Multipart
    @POST("api/v1/voice/verify")
    suspend fun verify(@Part audio: MultipartBody.Part): VoiceApiEnvelope<VerifyResultDto>
}

// ─── Backend envelope ─────────────────────────────────────────────────────────
// {"success":true,"message":"...","data":{...},"timestamp":"..."}

@Serializable
data class VoiceApiEnvelope<T>(
    @SerialName("success")   val success: Boolean,
    @SerialName("message")   val message: String? = null,
    @SerialName("data")      val data: T? = null,
    @SerialName("timestamp") val timestamp: String? = null,
)

// ─── DTOs ─────────────────────────────────────────────────────────────────────

@Serializable
data class EnrollmentStatusDto(
    @SerialName("enrolled")       val enrolled: Boolean,
    @SerialName("fullyEnrolled")  val fullyEnrolled: Boolean = false,
    @SerialName("phraseCount")    val phraseCount: Int = 0,
    @SerialName("enrolledAt")     val enrolledAt: String? = null,
)

@Serializable
data class VerifyResultDto(
    @SerialName("match")       val match: Boolean,
    @SerialName("confidence")  val confidence: Double,
    @SerialName("errorReason") val errorReason: String? = null,
)

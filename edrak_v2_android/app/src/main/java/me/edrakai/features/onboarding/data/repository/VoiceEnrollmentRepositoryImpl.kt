package me.edrakai.features.onboarding.data.repository

import me.edrakai.features.onboarding.data.remote.VoiceApiService
import me.edrakai.features.onboarding.domain.repository.VoiceEnrollmentRepository
import me.edrakai.features.onboarding.domain.repository.VerifyResult
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceEnrollmentRepositoryImpl @Inject constructor(
    private val api: VoiceApiService,
) : VoiceEnrollmentRepository {

    override suspend fun enrollVoice(audioFile: File): Result<Unit> = runCatching {
        val part = audioFile.toPart()
        val envelope = api.enroll(part)
        if (!envelope.success) error(envelope.message ?: "Enrollment failed")
        audioFile.delete() // never persist audio on device
    }

    override suspend fun isEnrolled(): Boolean = runCatching {
        val envelope = api.getStatus()
        envelope.success && (envelope.data?.fullyEnrolled == true)
    }.getOrDefault(false)

    override suspend fun verifyVoice(audioFile: File): Result<VerifyResult> = runCatching {
        val part = audioFile.toPart()
        val envelope = api.verify(part)
        if (!envelope.success) error(envelope.message ?: "Verification failed")
        val dto = envelope.data ?: error("Empty verification response")
        audioFile.delete()
        VerifyResult(match = dto.match, confidence = dto.confidence.toFloat())
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun File.toPart(): MultipartBody.Part {
        val requestFile = asRequestBody("audio/m4a".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("audio", name, requestFile)
    }
}

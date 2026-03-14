package me.edrakai.features.onboarding.data.repository

import me.edrakai.core.security.TokenManager
import me.edrakai.features.onboarding.data.remote.VoiceApiService
import me.edrakai.features.onboarding.domain.repository.VoiceEnrollmentRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceEnrollmentRepositoryImpl @Inject constructor(
    private val api: VoiceApiService,
    private val tokenManager: TokenManager
) : VoiceEnrollmentRepository {

    override suspend fun enrollVoice(audioFile: File): Result<Unit> = runCatching {
        val requestFile = audioFile.asRequestBody("audio/m4a".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("audio", audioFile.name, requestFile)
        api.enroll(part)
        audioFile.delete() // never persist audio on device
    }

    override suspend fun isEnrolled(): Boolean = runCatching {
        api.getStatus().enrolled
    }.getOrDefault(false)
}

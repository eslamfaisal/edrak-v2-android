package me.edrakai.features.onboarding.domain.repository

import java.io.File

interface VoiceEnrollmentRepository {
    suspend fun enrollVoice(audioFile: File): Result<Unit>
    suspend fun isEnrolled(): Boolean
    suspend fun verifyVoice(audioFile: File): Result<VerifyResult>
}

data class VerifyResult(
    val match: Boolean,
    val confidence: Float,
)

package me.edrakai.features.onboarding.domain.repository

import java.io.File

interface VoiceEnrollmentRepository {
    suspend fun enrollVoice(audioFile: File): Result<Unit>
    suspend fun isEnrolled(): Boolean
}

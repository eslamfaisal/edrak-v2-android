package me.edrakai.features.onboarding.domain.usecase

import me.edrakai.features.onboarding.domain.repository.VoiceEnrollmentRepository
import java.io.File
import javax.inject.Inject

class EnrollVoiceUseCase @Inject constructor(
    private val repository: VoiceEnrollmentRepository
) {
    suspend operator fun invoke(audioFile: File): Result<Unit> {
        if (!audioFile.exists() || audioFile.length() == 0L) {
            return Result.failure(IllegalArgumentException("Audio file is empty or missing"))
        }
        return repository.enrollVoice(audioFile)
    }
}

class CheckEnrollmentUseCase @Inject constructor(
    private val repository: VoiceEnrollmentRepository
) {
    suspend operator fun invoke(): Boolean = repository.isEnrolled()
}

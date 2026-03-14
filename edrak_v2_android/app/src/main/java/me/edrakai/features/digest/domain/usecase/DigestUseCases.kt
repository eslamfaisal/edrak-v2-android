package me.edrakai.features.digest.domain.usecase

import me.edrakai.features.digest.domain.model.DailyDigest
import me.edrakai.features.digest.domain.repository.DigestRepository
import javax.inject.Inject

class GetTodayDigestUseCase @Inject constructor(
    private val repository: DigestRepository
) {
    suspend operator fun invoke(): Result<DailyDigest> = repository.getTodayDigest()
}

class GetDigestByDateUseCase @Inject constructor(
    private val repository: DigestRepository
) {
    suspend operator fun invoke(date: String): Result<DailyDigest> = repository.getDigestByDate(date)
}

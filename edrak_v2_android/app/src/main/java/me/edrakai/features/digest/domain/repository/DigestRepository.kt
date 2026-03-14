package me.edrakai.features.digest.domain.repository

import me.edrakai.features.digest.domain.model.DailyDigest

interface DigestRepository {
    suspend fun getTodayDigest(): Result<DailyDigest>
    suspend fun getDigestByDate(date: String): Result<DailyDigest>
}

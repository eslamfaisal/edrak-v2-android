package me.edrakai.features.digest.data.repository

import me.edrakai.features.digest.data.remote.DigestApiService
import me.edrakai.features.digest.domain.model.DailyDigest
import me.edrakai.features.digest.domain.model.DigestConversation
import me.edrakai.features.digest.domain.repository.DigestRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DigestRepositoryImpl @Inject constructor(
    private val api: DigestApiService
) : DigestRepository {

    override suspend fun getTodayDigest(): Result<DailyDigest> = runCatching {
        api.getTodayDigest().toDomain()
    }

    override suspend fun getDigestByDate(date: String): Result<DailyDigest> = runCatching {
        api.getDigestByDate(date).toDomain()
    }

    private fun me.edrakai.features.digest.data.remote.DigestResponse.toDomain() = DailyDigest(
        date = date, totalWords = totalWords, totalTopics = totalTopics,
        conversations = conversations.map { dto ->
            DigestConversation(
                id = dto.id, title = dto.title, timeRange = dto.timeRange,
                category = dto.category, summary = dto.summary, actions = dto.actions
            )
        }
    )
}

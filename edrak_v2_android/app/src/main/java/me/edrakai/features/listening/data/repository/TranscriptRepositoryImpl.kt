package me.edrakai.features.listening.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.edrakai.core.database.dao.ConversationDao
import me.edrakai.core.database.dao.TranscriptChunkDao
import me.edrakai.features.listening.domain.model.LiveTranscriptEntry
import me.edrakai.features.listening.domain.repository.TranscriptRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptRepositoryImpl @Inject constructor(
    private val chunkDao: TranscriptChunkDao,
    private val conversationDao: ConversationDao
) : TranscriptRepository {

    override fun observeLiveTranscript(conversationId: String): Flow<List<LiveTranscriptEntry>> =
        chunkDao.getByConversation(conversationId).map { chunks ->
            chunks.map { chunk ->
                LiveTranscriptEntry(
                    id = chunk.id,
                    speakerTag = chunk.speakerTag,
                    text = chunk.text,
                    timestampMs = chunk.timestampMs
                )
            }
        }

    override suspend fun getActiveConversationId(): String? =
        conversationDao.getActiveConversation()?.id
}

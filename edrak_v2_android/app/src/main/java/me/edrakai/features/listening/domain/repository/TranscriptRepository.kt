package me.edrakai.features.listening.domain.repository

import kotlinx.coroutines.flow.Flow
import me.edrakai.features.listening.domain.model.LiveTranscriptEntry

interface TranscriptRepository {
    fun observeLiveTranscript(conversationId: String): Flow<List<LiveTranscriptEntry>>
    suspend fun getActiveConversationId(): String?
}

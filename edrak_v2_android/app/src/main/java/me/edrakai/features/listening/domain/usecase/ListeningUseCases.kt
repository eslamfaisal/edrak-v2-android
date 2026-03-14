package me.edrakai.features.listening.domain.usecase

import kotlinx.coroutines.flow.Flow
import me.edrakai.features.listening.domain.model.LiveTranscriptEntry
import me.edrakai.features.listening.domain.repository.TranscriptRepository
import javax.inject.Inject

class ObserveLiveTranscriptUseCase @Inject constructor(
    private val repository: TranscriptRepository
) {
    operator fun invoke(conversationId: String): Flow<List<LiveTranscriptEntry>> =
        repository.observeLiveTranscript(conversationId)
}

class GetActiveConversationUseCase @Inject constructor(
    private val repository: TranscriptRepository
) {
    suspend operator fun invoke(): String? = repository.getActiveConversationId()
}

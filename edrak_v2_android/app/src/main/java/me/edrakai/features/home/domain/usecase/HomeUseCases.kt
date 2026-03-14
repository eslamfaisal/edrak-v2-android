package me.edrakai.features.home.domain.usecase

import kotlinx.coroutines.flow.Flow
import me.edrakai.features.home.domain.model.ConversationSummary
import me.edrakai.features.home.domain.model.DetectedAction
import me.edrakai.features.home.domain.repository.HomeRepository
import javax.inject.Inject

class ObservePendingActionsUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    operator fun invoke(): Flow<List<DetectedAction>> = repository.observePendingActions()
}

class ObserveTodayConversationsUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    operator fun invoke(): Flow<List<ConversationSummary>> = repository.observeTodayConversations()
}

class MarkActionExecutedUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    suspend operator fun invoke(actionId: String) = repository.markActionExecuted(actionId)
}

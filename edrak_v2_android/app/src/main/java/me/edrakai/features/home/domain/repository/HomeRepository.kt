package me.edrakai.features.home.domain.repository

import kotlinx.coroutines.flow.Flow
import me.edrakai.features.home.domain.model.DetectedAction
import me.edrakai.features.home.domain.model.ConversationSummary

interface HomeRepository {
    fun observePendingActions(): Flow<List<DetectedAction>>
    fun observeTodayConversations(): Flow<List<ConversationSummary>>
    suspend fun markActionExecuted(actionId: String)
}

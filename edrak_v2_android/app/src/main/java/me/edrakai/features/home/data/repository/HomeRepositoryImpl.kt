package me.edrakai.features.home.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.edrakai.core.database.dao.ConversationDao
import me.edrakai.core.database.dao.DetectedActionDao
import me.edrakai.core.database.entity.SyncStatus
import me.edrakai.features.home.data.remote.ActionsApiService
import me.edrakai.features.home.domain.model.ConversationSummary
import me.edrakai.features.home.domain.model.DetectedAction
import me.edrakai.features.home.domain.repository.HomeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepositoryImpl @Inject constructor(
    private val actionDao: DetectedActionDao,
    private val conversationDao: ConversationDao,
    private val actionsApi: ActionsApiService
) : HomeRepository {

    override fun observePendingActions(): Flow<List<DetectedAction>> =
        actionDao.getPendingActionsFlow().map { entities ->
            entities.map { e ->
                DetectedAction(
                    id = e.id, conversationId = e.conversationId, type = e.type,
                    title = e.title, payload = e.payload, detectedAt = e.detectedAt,
                    executed = e.executed
                )
            }
        }

    override fun observeTodayConversations(): Flow<List<ConversationSummary>> =
        conversationDao.getAllFlow().map { entities ->
            val todayStart = todayStartMs()
            entities
                .filter { it.startTime >= todayStart }
                .map { e ->
                    ConversationSummary(
                        id = e.id, startTime = e.startTime, endTime = e.endTime,
                        speakerCount = 0,
                        previewText = "",
                        syncStatus = e.syncStatus
                    )
                }
        }

    override suspend fun markActionExecuted(actionId: String) {
        actionDao.markExecuted(actionId)
        runCatching { actionsApi.markExecuted(actionId) } // best-effort API call
    }

    private fun todayStartMs(): Long {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}

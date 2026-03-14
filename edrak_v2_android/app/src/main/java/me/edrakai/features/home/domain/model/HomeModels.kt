package me.edrakai.features.home.domain.model

import me.edrakai.core.database.entity.ActionType
import me.edrakai.core.database.entity.SyncStatus

// Domain representation — independent of Room entities
data class DetectedAction(
    val id: String,
    val conversationId: String,
    val type: ActionType,
    val title: String,
    val payload: String,
    val detectedAt: Long,
    val executed: Boolean
)

data class ConversationSummary(
    val id: String,
    val startTime: Long,
    val endTime: Long?,
    val speakerCount: Int,
    val previewText: String,
    val syncStatus: SyncStatus
)

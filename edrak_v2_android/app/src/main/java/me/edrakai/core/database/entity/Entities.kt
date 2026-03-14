package me.edrakai.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SyncStatus { LOCAL_ONLY, SYNCING, SYNCED, SYNC_FAILED }
enum class ActionType  { MEETING, ALARM, TASK, NOTE }

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val startTime: Long,
    val endTime: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
)

@Entity(tableName = "transcript_chunks")
data class TranscriptChunkEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val speakerTag: String,      // "SPEAKER_1", "SPEAKER_2", etc.
    val text: String,
    val timestampMs: Long,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
)

@Entity(tableName = "detected_actions")
data class DetectedActionEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val type: ActionType,
    val title: String,
    val payload: String,         // JSON blob
    val detectedAt: Long,
    val executed: Boolean = false,
)

package me.edrakai.core.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import me.edrakai.core.database.entity.ConversationEntity
import me.edrakai.core.database.entity.DetectedActionEntity
import me.edrakai.core.database.entity.SyncStatus
import me.edrakai.core.database.entity.TranscriptChunkEntity

@Dao
interface ConversationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conversation: ConversationEntity)

    @Update
    suspend fun update(conversation: ConversationEntity)

    @Query("SELECT * FROM conversations WHERE endTime IS NULL LIMIT 1")
    suspend fun getActiveConversation(): ConversationEntity?

    @Query("SELECT * FROM conversations ORDER BY startTime DESC")
    fun getAllFlow(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getById(id: String): ConversationEntity?
}

@Dao
interface TranscriptChunkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chunk: TranscriptChunkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chunks: List<TranscriptChunkEntity>)

    @Query("UPDATE transcript_chunks SET syncStatus = :status WHERE id IN (:ids)")
    suspend fun updateSyncStatus(ids: List<String>, status: SyncStatus)

    @Query("SELECT * FROM transcript_chunks WHERE syncStatus IN ('LOCAL_ONLY', 'SYNC_FAILED')")
    suspend fun getUnsyncedChunks(): List<TranscriptChunkEntity>

    @Query("SELECT * FROM transcript_chunks WHERE conversationId = :conversationId ORDER BY timestampMs ASC")
    fun getByConversationFlow(conversationId: String): Flow<List<TranscriptChunkEntity>>

    @Query("SELECT * FROM transcript_chunks WHERE conversationId = :conversationId ORDER BY timestampMs ASC")
    suspend fun getByConversation(conversationId: String): List<TranscriptChunkEntity>
}

@Dao
interface DetectedActionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(action: DetectedActionEntity)

    @Query("UPDATE detected_actions SET executed = 1 WHERE id = :id")
    suspend fun markExecuted(id: String)

    @Query("SELECT * FROM detected_actions WHERE executed = 0 ORDER BY detectedAt DESC")
    fun getPendingActionsFlow(): Flow<List<DetectedActionEntity>>

    @Query("SELECT * FROM detected_actions WHERE conversationId = :conversationId ORDER BY detectedAt ASC")
    fun getByConversationFlow(conversationId: String): Flow<List<DetectedActionEntity>>
}

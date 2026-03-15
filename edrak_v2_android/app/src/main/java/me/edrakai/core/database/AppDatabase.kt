package me.edrakai.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import me.edrakai.core.database.dao.ConversationDao
import me.edrakai.core.database.dao.DetectedActionDao
import me.edrakai.core.database.dao.TranscriptChunkDao
import me.edrakai.core.database.entity.ConversationEntity
import me.edrakai.core.database.entity.DetectedActionEntity
import me.edrakai.core.database.entity.TranscriptChunkEntity

@Database(
    entities = [
        ConversationEntity::class,
        TranscriptChunkEntity::class,
        DetectedActionEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(EdrakTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun transcriptChunkDao(): TranscriptChunkDao
    abstract fun detectedActionDao(): DetectedActionDao
}

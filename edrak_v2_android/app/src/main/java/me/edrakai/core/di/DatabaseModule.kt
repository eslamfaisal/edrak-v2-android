package me.edrakai.core.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.edrakai.core.database.AppDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "edrak_v2.db",
        )
            .fallbackToDestructiveMigration() // Replace with proper migrations in production
            .build()
    }

    @Provides
    @Singleton
    fun provideConversationDao(db: AppDatabase) = db.conversationDao()

    @Provides
    @Singleton
    fun provideTranscriptChunkDao(db: AppDatabase) = db.transcriptChunkDao()

    @Provides
    @Singleton
    fun provideDetectedActionDao(db: AppDatabase) = db.detectedActionDao()
}

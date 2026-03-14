package me.edrakai.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.edrakai.features.auth.data.repository.AuthRepositoryImpl
import me.edrakai.features.auth.domain.repository.AuthRepository
import me.edrakai.features.digest.data.repository.DigestRepositoryImpl
import me.edrakai.features.digest.domain.repository.DigestRepository
import me.edrakai.features.home.data.repository.HomeRepositoryImpl
import me.edrakai.features.home.domain.repository.HomeRepository
import me.edrakai.features.listening.data.repository.TranscriptRepositoryImpl
import me.edrakai.features.listening.domain.repository.TranscriptRepository
import me.edrakai.features.onboarding.data.repository.VoiceEnrollmentRepositoryImpl
import me.edrakai.features.onboarding.domain.repository.VoiceEnrollmentRepository
import javax.inject.Singleton

/**
 * Binds every domain Repository interface to its concrete implementation.
 *
 * Convention: @Binds is preferred over @Provides for interface-to-impl wiring
 * because it generates zero extra code at compile time (no method body).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindVoiceEnrollmentRepository(impl: VoiceEnrollmentRepositoryImpl): VoiceEnrollmentRepository

    @Binds
    @Singleton
    abstract fun bindHomeRepository(impl: HomeRepositoryImpl): HomeRepository

    @Binds
    @Singleton
    abstract fun bindTranscriptRepository(impl: TranscriptRepositoryImpl): TranscriptRepository

    @Binds
    @Singleton
    abstract fun bindDigestRepository(impl: DigestRepositoryImpl): DigestRepository
}

package me.edrakai.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.edrakai.features.auth.data.remote.AuthApiService
import me.edrakai.features.digest.data.remote.DigestApiService
import me.edrakai.features.home.data.remote.ActionsApiService
import me.edrakai.features.listening.data.remote.SttApiService
import me.edrakai.features.onboarding.data.remote.VoiceApiService
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Provides Retrofit API service instances for each feature.
 * All services share the same Retrofit instance provided by NetworkModule.
 */
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides @Singleton
    fun provideVoiceApiService(retrofit: Retrofit): VoiceApiService =
        retrofit.create(VoiceApiService::class.java)

    @Provides @Singleton
    fun provideActionsApiService(retrofit: Retrofit): ActionsApiService =
        retrofit.create(ActionsApiService::class.java)

    @Provides @Singleton
    fun provideDigestApiService(retrofit: Retrofit): DigestApiService =
        retrofit.create(DigestApiService::class.java)

    @Provides @Singleton
    fun provideSttApiService(retrofit: Retrofit): SttApiService =
        retrofit.create(SttApiService::class.java)
}

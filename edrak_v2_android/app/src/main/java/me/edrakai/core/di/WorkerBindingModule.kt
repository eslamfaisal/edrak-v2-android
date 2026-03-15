package me.edrakai.core.di

import androidx.work.ListenableWorker
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds

/**
 * Provides an empty multibinding map for WorkManager workers.
 *
 * WHY: EdrakWorkerFactory requires Map<Class<Worker>, Provider<ChildWorkerFactory>>.
 * Dagger infers the Provider wrapper itself — @Multibinds declares the bare value type.
 * When Workers are added (Phase 3), use @WorkerKey to bind into this map.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class WorkerBindingModule {

    @Multibinds
    abstract fun workerFactories(): Map<Class<out ListenableWorker>, @JvmSuppressWildcards ChildWorkerFactory>
}

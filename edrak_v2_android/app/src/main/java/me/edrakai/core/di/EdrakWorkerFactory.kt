package me.edrakai.core.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import javax.inject.Inject
import javax.inject.Provider

/**
 * Custom Hilt-compatible WorkerFactory that allows injecting dependencies into Workers.
 * Registered in EdrakApplication.workManagerConfiguration.
 */
class EdrakWorkerFactory @Inject constructor(
    private val workerProviders: Map<Class<out ListenableWorker>, @JvmSuppressWildcards Provider<ChildWorkerFactory>>,
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? {
        val workerClass = Class.forName(workerClassName).asSubclass(ListenableWorker::class.java)
        val provider = workerProviders[workerClass] ?: return null
        return provider.get().create(appContext, workerParameters)
    }
}

interface ChildWorkerFactory {
    fun create(appContext: Context, params: WorkerParameters): ListenableWorker
}

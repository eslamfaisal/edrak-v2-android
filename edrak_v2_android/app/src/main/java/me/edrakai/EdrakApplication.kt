package me.edrakai

import android.app.Application
import android.os.StrictMode
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import me.edrakai.core.di.EdrakWorkerFactory
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class EdrakApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: EdrakWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            enableStrictMode()
        }
    }

    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build(),
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectActivityLeaks()
                .penaltyLog()
                .build(),
        )
    }
}

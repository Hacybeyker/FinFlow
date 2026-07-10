package com.hacybeyker.finflow

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.hacybeyker.finflow.core.database.DatabaseKey
import com.hacybeyker.finflow.feature.widget.ui.WidgetRefresher
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// Configuration.Provider: WorkManager initializes on demand (its default initializer is removed in
// the manifest) with a HiltWorkerFactory, so @HiltWorker workers (ReminderWorker) can inject
// dependencies. Workers without the annotation (WidgetRefreshWorker, Glance's own session workers)
// still work — HiltWorkerFactory returns null for them and WorkManager falls back to reflection.
@HiltAndroidApp
class FinFlowApplication :
    Application(),
    Configuration.Provider {

    @Inject
    lateinit var databaseKey: DatabaseKey

    @Inject
    lateinit var widgetRefresher: WidgetRefresher

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Warm the crypto path off the main thread: native SQLCipher lib + Keystore-backed passphrase.
        // The first DAO injection (main thread, after unlock) then finds everything already loaded.
        applicationScope.launch {
            System.loadLibrary("sqlcipher")
            databaseKey.warmUp()
        }
        widgetRefresher.start(applicationScope)
    }
}

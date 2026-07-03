package com.hacybeyker.finflow

import android.app.Application
import com.hacybeyker.finflow.core.database.DatabaseKey
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class FinFlowApplication : Application() {

    @Inject
    lateinit var databaseKey: DatabaseKey

    override fun onCreate() {
        super.onCreate()
        // Warm the crypto path off the main thread: native SQLCipher lib + Keystore-backed passphrase.
        // The first DAO injection (main thread, after unlock) then finds everything already loaded.
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            System.loadLibrary("sqlcipher")
            databaseKey.warmUp()
        }
    }
}

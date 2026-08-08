package com.onideity.rommcompanion

import android.app.Application
import com.onideity.rommcompanion.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RommCompanionApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Keeps RommSession's API client in sync whenever the configured
        // server URL changes; runs for the process lifetime.
        applicationScope.launch {
            container.session.watchServerUrl()
        }
    }
}

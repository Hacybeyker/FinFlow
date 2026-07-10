package com.hacybeyker.finflow.feature.widget.ui

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.hacybeyker.finflow.core.domain.PreferencesRepository
import com.hacybeyker.finflow.feature.widget.domain.usecase.GetWidgetSummaryUseCase
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

/**
 * Pushes a widget re-render whenever the data it shows changes: any transaction write (Room's Flow
 * re-emits) or a currency change (the amounts re-format). Glance can't observe flows itself — its
 * composition is a short-lived session — so this app-scoped collector is the bridge from the SSOT
 * to the launcher. With no widgets on screen `updateAll` is a cheap no-op.
 *
 * Dependencies are [Lazy] so injecting this into the Application doesn't build the (SQLCipher)
 * database graph on the main thread at startup — first access happens inside the collector
 * coroutine, off the critical path.
 */
@Singleton
class WidgetRefresher @Inject constructor(
    private val getWidgetSummary: Lazy<GetWidgetSummaryUseCase>,
    private val preferences: Lazy<PreferencesRepository>,
    @param:ApplicationContext private val context: Context
) {

    fun start(scope: CoroutineScope) {
        scope.launch {
            combine(getWidgetSummary.get()(), preferences.get().observePreferences()) { _, _ -> }
                .conflate()
                // A transient Room/DataStore read failure must never crash the whole app: this
                // collector runs for the entire process lifetime with no lifecycle to bound it.
                // WidgetRefreshWorker's daily run is the eventual-consistency fallback if it stops.
                .catch { }
                .collect { runCatching { FinFlowWidget().updateAll(context) } }
        }
    }
}

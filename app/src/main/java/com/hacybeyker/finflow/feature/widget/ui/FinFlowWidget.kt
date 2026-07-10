package com.hacybeyker.finflow.feature.widget.ui

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.material3.ColorProviders
import com.hacybeyker.finflow.core.domain.PreferencesRepository
import com.hacybeyker.finflow.core.ui.format.MoneyFormatter
import com.hacybeyker.finflow.core.ui.format.moneyFormatterFor
import com.hacybeyker.finflow.core.ui.theme.DarkColorScheme
import com.hacybeyker.finflow.core.ui.theme.LightColorScheme
import com.hacybeyker.finflow.feature.widget.domain.WidgetSummary
import com.hacybeyker.finflow.feature.widget.domain.usecase.GetWidgetSummaryUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Home-screen widget: all-time balance, current-month income/expense and, when the actual size
 * reports enough room, the latest movements (see [WidgetContent]'s height check). Amounts are
 * masked by default and unmask with a tap on the widget's own
 * "Mostrar" control ([ToggleRevealAction]) — **not** gated behind the app's biometric lock: the OS
 * lock screen is already the real access boundary for a home-screen surface (anyone who can see it
 * already unlocked the device), so this is casual privacy, not a second access-control layer. See
 * [WidgetContent] for the reveal/mask rendering.
 *
 * Glance runs the composition inside a short-lived session worker, so the data must be **collected
 * as state inside `provideContent`** (`collectAsState`) — a value captured once before composing
 * would freeze for the whole session, and `updateAll` on a live session only recomposes, it does not
 * re-run [provideGlance] (that bug shipped first: writes only showed up after the session died).
 * The flow is subscribed to exactly once, here — there's no separate up-front `first()` snapshot,
 * which would pay for the same combined query twice per session start.
 */
class FinFlowWidget : GlanceAppWidget() {

    // Exact (not Responsive over a couple of declared sizes): launchers report very different real
    // dp values for "one grid row" — a Responsive bucket that doesn't match the device's actual
    // minimum left dead space below the content. Exact composes for whatever size the host reports,
    // so LocalSize.current always reflects reality and content can size itself accordingly.
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val states: Flow<WidgetState?> = combine(
            entryPoint.getWidgetSummary()(),
            entryPoint.preferencesRepository().observePreferences()
        ) { summary, preferences ->
            WidgetState(summary = summary, formatter = moneyFormatterFor(preferences.currencyCode))
        }

        provideContent {
            // Renders nothing until the first (single) subscription emits, rather than flashing a
            // fake placeholder value while briefly holding two live subscriptions to the same data.
            val state by states.collectAsState(initial = null)
            // Per-widget-instance state (Glance's own store, independent of app preferences) — the
            // reveal choice is local to this widget, not tied to any account-wide setting.
            val revealed = currentState(AMOUNTS_REVEALED) ?: false
            GlanceTheme(colors = WidgetColors) {
                state?.let { WidgetContent(it, revealed = revealed) }
            }
        }
    }
}

internal data class WidgetState(val summary: WidgetSummary, val formatter: MoneyFormatter)

/**
 * The widget is instantiated by the system (not by Hilt), so its dependencies enter through an
 * `@EntryPoint` on the singleton component instead of constructor injection.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface WidgetEntryPoint {
    fun getWidgetSummary(): GetWidgetSummaryUseCase
    fun preferencesRepository(): PreferencesRepository
}

/**
 * Same brand schemes as [com.hacybeyker.finflow.core.ui.theme.FinFlowTheme], mapped once to Glance
 * day/night providers. The widget follows the **system** dark mode (RemoteViews resolve day/night
 * at the launcher, where the in-app theme preference does not exist).
 */
private val WidgetColors = ColorProviders(light = LightColorScheme, dark = DarkColorScheme)

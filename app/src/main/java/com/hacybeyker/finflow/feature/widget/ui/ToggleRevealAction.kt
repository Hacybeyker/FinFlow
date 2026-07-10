package com.hacybeyker.finflow.feature.widget.ui

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState

/** Whether amounts are unmasked, stored per widget instance (Glance's own state, not app data). */
internal val AMOUNTS_REVEALED = booleanPreferencesKey("amounts_revealed")

/**
 * Flips this widget instance's reveal flag and re-renders immediately. Deliberately **not** gated
 * behind biometrics: the OS lock screen is already the real access boundary for a home-screen
 * surface (anyone who can see it already unlocked the device), so masking here is casual privacy
 * (don't broadcast the balance to whoever glances at the screen), not access control.
 */
class ToggleRevealAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[AMOUNTS_REVEALED] = !(prefs[AMOUNTS_REVEALED] ?: false)
        }
        FinFlowWidget().update(context, glanceId)
    }
}

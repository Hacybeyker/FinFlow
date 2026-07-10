package com.hacybeyker.finflow.feature.settings.ui

import com.hacybeyker.finflow.core.domain.UserPreferences

/**
 * [isLoading] distinguishes "DataStore hasn't emitted yet" from real values: the app root must not
 * compose the lock gate (nor trust [preferences]) until the first emission, or a user who disabled
 * the lock would still get prompted on a cold start.
 */
data class SettingsUiState(val preferences: UserPreferences = UserPreferences(), val isLoading: Boolean = true)

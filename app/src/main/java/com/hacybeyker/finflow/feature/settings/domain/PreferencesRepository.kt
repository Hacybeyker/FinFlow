package com.hacybeyker.finflow.feature.settings.domain

import kotlinx.coroutines.flow.Flow

/**
 * Contract for reading/writing [UserPreferences]. Reads are a reactive [Flow] (the theme and the
 * money formatter re-apply instantly when a value changes); writes are suspend and atomic per key.
 * Implementations must always emit a valid snapshot — unknown or corrupt stored values fall back to
 * the [UserPreferences] defaults instead of failing.
 */
interface PreferencesRepository {
    fun observePreferences(): Flow<UserPreferences>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setCurrencyCode(code: String?)
    suspend fun setAppLockEnabled(enabled: Boolean)
}

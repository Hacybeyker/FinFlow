package com.hacybeyker.finflow.core.domain

import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [PreferencesRepository] honoring the contract: always emits a valid snapshot. */
class FakePreferencesRepository(initial: UserPreferences = UserPreferences()) : PreferencesRepository {

    private val state = MutableStateFlow(initial)

    override fun observePreferences(): Flow<UserPreferences> = state

    override suspend fun setThemeMode(mode: ThemeMode) {
        state.value = state.value.copy(themeMode = mode)
    }

    override suspend fun setCurrencyCode(code: String?) {
        state.value = state.value.copy(currencyCode = code)
    }

    override suspend fun setAppLockEnabled(enabled: Boolean) {
        state.value = state.value.copy(appLockEnabled = enabled)
    }

    override suspend fun setReminderEnabled(enabled: Boolean) {
        state.value = state.value.copy(reminderEnabled = enabled)
    }

    override suspend fun setReminderTime(time: LocalTime) {
        state.value = state.value.copy(reminderTime = time)
    }
}

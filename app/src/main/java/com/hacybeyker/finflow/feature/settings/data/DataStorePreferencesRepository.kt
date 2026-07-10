package com.hacybeyker.finflow.feature.settings.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hacybeyker.finflow.core.domain.PreferencesRepository
import com.hacybeyker.finflow.core.domain.ThemeMode
import com.hacybeyker.finflow.core.domain.UserPreferences
import java.time.LocalTime
import java.util.Currency
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * [PreferencesRepository] over Preferences DataStore. Every read is defensive: an unknown theme
 * name or an invalid/unsupported currency code degrades to the [UserPreferences] default instead of
 * crashing — a settings file must never be able to brick startup (the theme is read before the
 * first frame).
 */
class DataStorePreferencesRepository @Inject constructor(private val dataStore: DataStore<Preferences>) :
    PreferencesRepository {

    override fun observePreferences(): Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            themeMode = prefs[THEME_MODE]?.toThemeModeOrNull() ?: ThemeMode.SYSTEM,
            currencyCode = prefs[CURRENCY_CODE]?.takeIf { it.isValidCurrencyCode() },
            appLockEnabled = prefs[APP_LOCK_ENABLED] ?: true,
            reminderEnabled = prefs[REMINDER_ENABLED] ?: false,
            reminderTime = prefs[REMINDER_MINUTE_OF_DAY]?.toLocalTimeOrNull()
                ?: UserPreferences.DefaultReminderTime
        )
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs -> prefs[THEME_MODE] = mode.name }
    }

    override suspend fun setCurrencyCode(code: String?) {
        dataStore.edit { prefs ->
            if (code == null) prefs.remove(CURRENCY_CODE) else prefs[CURRENCY_CODE] = code
        }
    }

    override suspend fun setAppLockEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[APP_LOCK_ENABLED] = enabled }
    }

    override suspend fun setReminderEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[REMINDER_ENABLED] = enabled }
    }

    override suspend fun setReminderTime(time: LocalTime) {
        dataStore.edit { prefs -> prefs[REMINDER_MINUTE_OF_DAY] = time.hour * MINUTES_PER_HOUR + time.minute }
    }

    private companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")

        // Minute of day (0..1439): survives locale/format changes, unlike a formatted string.
        val REMINDER_MINUTE_OF_DAY = intPreferencesKey("reminder_minute_of_day")
        const val MINUTES_PER_HOUR = 60
    }
}

private fun String.toThemeModeOrNull(): ThemeMode? = ThemeMode.entries.firstOrNull { it.name == this }

private fun Int.toLocalTimeOrNull(): LocalTime? = runCatching { LocalTime.ofSecondOfDay(this * 60L) }.getOrNull()

private fun String.isValidCurrencyCode(): Boolean = runCatching { Currency.getInstance(this) }.isSuccess

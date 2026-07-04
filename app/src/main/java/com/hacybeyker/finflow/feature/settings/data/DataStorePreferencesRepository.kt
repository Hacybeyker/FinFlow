package com.hacybeyker.finflow.feature.settings.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hacybeyker.finflow.feature.settings.domain.PreferencesRepository
import com.hacybeyker.finflow.feature.settings.domain.ThemeMode
import com.hacybeyker.finflow.feature.settings.domain.UserPreferences
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
            appLockEnabled = prefs[APP_LOCK_ENABLED] ?: true
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

    private companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
    }
}

private fun String.toThemeModeOrNull(): ThemeMode? = ThemeMode.entries.firstOrNull { it.name == this }

private fun String.isValidCurrencyCode(): Boolean = runCatching { Currency.getInstance(this) }.isSuccess

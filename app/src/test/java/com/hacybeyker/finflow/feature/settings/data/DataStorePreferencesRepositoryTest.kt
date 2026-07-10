package com.hacybeyker.finflow.feature.settings.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hacybeyker.finflow.core.domain.ThemeMode
import com.hacybeyker.finflow.core.domain.UserPreferences
import java.time.LocalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Exercises the real Preferences DataStore on the JVM (it is not Android-only) against a temp file,
 * so the mapping and its defensive fallbacks are tested without Robolectric.
 */
class DataStorePreferencesRepositoryTest {

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private fun TestScope.dataStore() = PreferenceDataStoreFactory.create(
        scope = CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler))
    ) { tmpFolder.newFile("settings.preferences_pb") }

    @Test
    fun `fresh store emits the defaults`() = runTest {
        val repository = DataStorePreferencesRepository(dataStore())

        assertEquals(UserPreferences(), repository.observePreferences().first())
    }

    @Test
    fun `writes round-trip through the store`() = runTest {
        val repository = DataStorePreferencesRepository(dataStore())

        repository.setThemeMode(ThemeMode.DARK)
        repository.setCurrencyCode("PEN")
        repository.setAppLockEnabled(false)
        repository.setReminderEnabled(true)
        repository.setReminderTime(LocalTime.of(7, 45))

        assertEquals(
            UserPreferences(
                themeMode = ThemeMode.DARK,
                currencyCode = "PEN",
                appLockEnabled = false,
                reminderEnabled = true,
                reminderTime = LocalTime.of(7, 45)
            ),
            repository.observePreferences().first()
        )
    }

    @Test
    fun `clearing the currency returns to device default`() = runTest {
        val repository = DataStorePreferencesRepository(dataStore())

        repository.setCurrencyCode("USD")
        repository.setCurrencyCode(null)

        assertEquals(null, repository.observePreferences().first().currencyCode)
    }

    @Test
    fun `corrupt stored values fall back to defaults instead of crashing`() = runTest {
        val dataStore = dataStore()
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey("theme_mode")] = "NEON" // not a ThemeMode
            prefs[stringPreferencesKey("currency_code")] = "NOPE" // not ISO 4217
            prefs[intPreferencesKey("reminder_minute_of_day")] = 5_000 // > 1439, not a valid time
        }
        val repository = DataStorePreferencesRepository(dataStore)

        assertEquals(UserPreferences(), repository.observePreferences().first())
    }
}

package com.hacybeyker.finflow.feature.settings.ui

import com.hacybeyker.finflow.core.domain.FakePreferencesRepository
import com.hacybeyker.finflow.core.domain.FakeTransactionRepository
import com.hacybeyker.finflow.core.domain.ThemeMode
import com.hacybeyker.finflow.core.domain.UserPreferences
import com.hacybeyker.finflow.core.test.MainDispatcherRule
import com.hacybeyker.finflow.feature.reminders.domain.FakeReminderScheduler
import com.hacybeyker.finflow.feature.reminders.domain.usecase.SetReminderEnabledUseCase
import com.hacybeyker.finflow.feature.reminders.domain.usecase.SetReminderTimeUseCase
import com.hacybeyker.finflow.feature.settings.domain.FakeCsvSaver
import com.hacybeyker.finflow.feature.settings.domain.usecase.ObservePreferencesUseCase
import com.hacybeyker.finflow.feature.settings.domain.usecase.SetAppLockEnabledUseCase
import com.hacybeyker.finflow.feature.settings.domain.usecase.SetCurrencyUseCase
import com.hacybeyker.finflow.feature.settings.domain.usecase.SetThemeModeUseCase
import com.hacybeyker.finflow.feature.transactions.domain.usecase.ExportTransactionsCsvUseCase
import java.time.LocalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val scheduler = FakeReminderScheduler()

    private fun viewModel(repository: FakePreferencesRepository, csvSaver: FakeCsvSaver = FakeCsvSaver()) =
        SettingsViewModel(
            observePreferences = ObservePreferencesUseCase(repository),
            setThemeModeUseCase = SetThemeModeUseCase(repository),
            setCurrencyUseCase = SetCurrencyUseCase(repository),
            setAppLockEnabledUseCase = SetAppLockEnabledUseCase(repository),
            setReminderEnabledUseCase = SetReminderEnabledUseCase(repository, scheduler),
            setReminderTimeUseCase = SetReminderTimeUseCase(repository, scheduler),
            exportTransactionsCsv = ExportTransactionsCsvUseCase(FakeTransactionRepository()),
            csvSaver = csvSaver
        )

    @Test
    fun `starts loading and emits stored preferences`() = runTest(mainDispatcherRule.testDispatcher.scheduler) {
        val stored = UserPreferences(themeMode = ThemeMode.DARK, currencyCode = "PEN", appLockEnabled = false)
        val viewModel = viewModel(FakePreferencesRepository(stored))

        assertTrue(viewModel.uiState.value.isLoading)

        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(stored, state.preferences)
    }

    @Test
    fun `theme change persists and re-emits`() = runTest(mainDispatcherRule.testDispatcher.scheduler) {
        val viewModel = viewModel(FakePreferencesRepository())
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.setThemeMode(ThemeMode.DARK)
        advanceUntilIdle()

        assertEquals(ThemeMode.DARK, viewModel.uiState.value.preferences.themeMode)
    }

    @Test
    fun `currency can be set and cleared back to device default`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val viewModel = viewModel(FakePreferencesRepository())
            backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.setCurrency("USD")
            advanceUntilIdle()
            assertEquals("USD", viewModel.uiState.value.preferences.currencyCode)

            viewModel.setCurrency(null)
            advanceUntilIdle()
            assertEquals(null, viewModel.uiState.value.preferences.currencyCode)
        }

    @Test
    fun `app lock toggle persists`() = runTest(mainDispatcherRule.testDispatcher.scheduler) {
        val viewModel = viewModel(FakePreferencesRepository())
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.setAppLockEnabled(false)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.preferences.appLockEnabled)
    }

    @Test
    fun `reminder toggle persists and schedules the work`() = runTest(mainDispatcherRule.testDispatcher.scheduler) {
        val viewModel = viewModel(FakePreferencesRepository())
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.setReminderEnabled(true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.preferences.reminderEnabled)
        assertEquals(UserPreferences.DefaultReminderTime, scheduler.scheduledTime)
    }

    @Test
    fun `reminder time change persists and reschedules`() = runTest(mainDispatcherRule.testDispatcher.scheduler) {
        val viewModel = viewModel(FakePreferencesRepository(UserPreferences(reminderEnabled = true)))
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.setReminderTime(LocalTime.of(8, 0))
        advanceUntilIdle()

        assertEquals(LocalTime.of(8, 0), viewModel.uiState.value.preferences.reminderTime)
        assertEquals(LocalTime.of(8, 0), scheduler.scheduledTime)
    }

    @Test
    fun `export writes the csv to the destination and reports success`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val csvSaver = FakeCsvSaver(result = true)
            val viewModel = viewModel(FakePreferencesRepository(), csvSaver)

            viewModel.exportTransactions("content://docs/mov.csv")
            advanceUntilIdle()

            assertEquals("content://docs/mov.csv", csvSaver.savedDestination)
            assertEquals("date,category,type,amount,note\n", csvSaver.savedContent)
            assertEquals(ExportResult.SUCCESS, viewModel.exportResult.value)
        }

    @Test
    fun `a failed write reports error and the result can be consumed`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val viewModel = viewModel(FakePreferencesRepository(), FakeCsvSaver(result = false))

            viewModel.exportTransactions("content://docs/mov.csv")
            advanceUntilIdle()
            assertEquals(ExportResult.ERROR, viewModel.exportResult.value)

            viewModel.onExportResultShown()
            assertEquals(null, viewModel.exportResult.value)
        }
}

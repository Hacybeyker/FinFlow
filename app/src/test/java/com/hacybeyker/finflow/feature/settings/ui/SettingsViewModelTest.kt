package com.hacybeyker.finflow.feature.settings.ui

import com.hacybeyker.finflow.core.test.MainDispatcherRule
import com.hacybeyker.finflow.feature.settings.domain.FakePreferencesRepository
import com.hacybeyker.finflow.feature.settings.domain.ThemeMode
import com.hacybeyker.finflow.feature.settings.domain.UserPreferences
import com.hacybeyker.finflow.feature.settings.domain.usecase.ObservePreferencesUseCase
import com.hacybeyker.finflow.feature.settings.domain.usecase.SetAppLockEnabledUseCase
import com.hacybeyker.finflow.feature.settings.domain.usecase.SetCurrencyUseCase
import com.hacybeyker.finflow.feature.settings.domain.usecase.SetThemeModeUseCase
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

    private fun viewModel(repository: FakePreferencesRepository) = SettingsViewModel(
        observePreferences = ObservePreferencesUseCase(repository),
        setThemeModeUseCase = SetThemeModeUseCase(repository),
        setCurrencyUseCase = SetCurrencyUseCase(repository),
        setAppLockEnabledUseCase = SetAppLockEnabledUseCase(repository)
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
}

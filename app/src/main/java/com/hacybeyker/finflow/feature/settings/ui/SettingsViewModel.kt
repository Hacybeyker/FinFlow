package com.hacybeyker.finflow.feature.settings.ui

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hacybeyker.finflow.feature.settings.domain.ThemeMode
import com.hacybeyker.finflow.feature.settings.domain.usecase.ObservePreferencesUseCase
import com.hacybeyker.finflow.feature.settings.domain.usecase.SetAppLockEnabledUseCase
import com.hacybeyker.finflow.feature.settings.domain.usecase.SetCurrencyUseCase
import com.hacybeyker.finflow.feature.settings.domain.usecase.SetThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Serves two consumers: the Settings screen (reads + writes) and the app root, which observes the
 * same state to apply theme/currency and decide whether to compose the lock gate.
 */
@Stable
@HiltViewModel
class SettingsViewModel @Inject constructor(
    observePreferences: ObservePreferencesUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
    private val setCurrencyUseCase: SetCurrencyUseCase,
    private val setAppLockEnabledUseCase: SetAppLockEnabledUseCase
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = observePreferences()
        .map { SettingsUiState(preferences = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = SettingsUiState()
        )

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { setThemeModeUseCase(mode) }

    fun setCurrency(code: String?) = viewModelScope.launch { setCurrencyUseCase(code) }

    fun setAppLockEnabled(enabled: Boolean) = viewModelScope.launch { setAppLockEnabledUseCase(enabled) }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

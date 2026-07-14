package com.hacybeyker.finflow.feature.settings.ui

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hacybeyker.finflow.core.domain.ThemeMode
import com.hacybeyker.finflow.feature.reminders.domain.usecase.SetReminderEnabledUseCase
import com.hacybeyker.finflow.feature.reminders.domain.usecase.SetReminderTimeUseCase
import com.hacybeyker.finflow.feature.settings.domain.CsvSaver
import com.hacybeyker.finflow.feature.settings.domain.usecase.ObservePreferencesUseCase
import com.hacybeyker.finflow.feature.settings.domain.usecase.SetAppLockEnabledUseCase
import com.hacybeyker.finflow.feature.settings.domain.usecase.SetCurrencyUseCase
import com.hacybeyker.finflow.feature.settings.domain.usecase.SetThemeModeUseCase
import com.hacybeyker.finflow.feature.transactions.domain.usecase.ExportTransactionsCsvUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val setAppLockEnabledUseCase: SetAppLockEnabledUseCase,
    private val setReminderEnabledUseCase: SetReminderEnabledUseCase,
    private val setReminderTimeUseCase: SetReminderTimeUseCase,
    private val exportTransactionsCsv: ExportTransactionsCsvUseCase,
    private val csvSaver: CsvSaver
) : ViewModel() {

    // One-shot result: the screen shows a snackbar and acknowledges with onExportResultShown().
    private val _exportResult = MutableStateFlow<ExportResult?>(null)
    val exportResult: StateFlow<ExportResult?> = _exportResult.asStateFlow()

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

    fun setReminderEnabled(enabled: Boolean) = viewModelScope.launch { setReminderEnabledUseCase(enabled) }

    fun setReminderTime(time: LocalTime) = viewModelScope.launch { setReminderTimeUseCase(time) }

    fun exportTransactions(destinationUri: String) = viewModelScope.launch {
        val saved = csvSaver.save(destinationUri, exportTransactionsCsv())
        _exportResult.value = if (saved) ExportResult.SUCCESS else ExportResult.ERROR
    }

    fun onExportResultShown() {
        _exportResult.value = null
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

enum class ExportResult { SUCCESS, ERROR }

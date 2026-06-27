package com.hacybeyker.finflow.feature.charts.ui

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hacybeyker.finflow.feature.charts.domain.usecase.GetMonthlyTotalsUseCase
import com.hacybeyker.finflow.feature.charts.domain.usecase.GetSpendingByCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@Stable
@HiltViewModel
class ChartsViewModel @Inject constructor(
    getSpendingByCategory: GetSpendingByCategoryUseCase,
    getMonthlyTotals: GetMonthlyTotalsUseCase,
    clock: Clock
) : ViewModel() {

    val uiState: StateFlow<ChartsUiState> = combine(
        getSpendingByCategory(YearMonth.now(clock)),
        getMonthlyTotals(monthCount = TRAILING_MONTHS, until = YearMonth.now(clock))
    ) { spending, monthlyTotals ->
        ChartsUiState.Content(spending = spending, monthlyTotals = monthlyTotals)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = ChartsUiState.Loading
    )

    private companion object {
        const val TRAILING_MONTHS = 6
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

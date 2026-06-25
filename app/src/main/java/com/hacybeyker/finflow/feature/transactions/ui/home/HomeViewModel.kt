package com.hacybeyker.finflow.feature.transactions.ui.home

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hacybeyker.finflow.feature.transactions.domain.usecase.GetBalanceUseCase
import com.hacybeyker.finflow.feature.transactions.domain.usecase.GetTransactionsByMonthUseCase
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
class HomeViewModel @Inject constructor(
    getBalance: GetBalanceUseCase,
    getTransactionsByMonth: GetTransactionsByMonthUseCase,
    clock: Clock
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        getBalance(),
        getTransactionsByMonth(YearMonth.now(clock))
    ) { balance, transactions ->
        HomeUiState.Content(balance = balance, transactions = transactions)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = HomeUiState.Loading
    )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

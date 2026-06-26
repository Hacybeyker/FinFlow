package com.hacybeyker.finflow.feature.transactions.ui.home

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hacybeyker.finflow.feature.transactions.domain.Transaction
import com.hacybeyker.finflow.feature.transactions.domain.usecase.AddTransactionUseCase
import com.hacybeyker.finflow.feature.transactions.domain.usecase.DeleteTransactionUseCase
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
import kotlinx.coroutines.launch

@Stable
@HiltViewModel
class HomeViewModel @Inject constructor(
    getBalance: GetBalanceUseCase,
    getTransactionsByMonth: GetTransactionsByMonthUseCase,
    private val deleteTransaction: DeleteTransactionUseCase,
    private val addTransaction: AddTransactionUseCase,
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

    fun delete(transaction: Transaction) {
        viewModelScope.launch { deleteTransaction(transaction) }
    }

    /** Restores a swipe-deleted transaction (re-inserts it with its original id). */
    fun undoDelete(transaction: Transaction) {
        viewModelScope.launch { addTransaction(transaction) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

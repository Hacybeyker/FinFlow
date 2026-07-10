package com.hacybeyker.finflow.feature.transactions.ui.home

import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.domain.TransactionMonth

sealed interface HomeUiState {
    data object Loading : HomeUiState

    /**
     * [months] is the full history, newest first, grouped for section headers.
     * [monthIncome]/[monthExpense] are the current calendar month's gross totals for the hero card
     * (zero when the month has no movements — the balance itself is always all-time).
     */
    data class Content(
        val balance: Money,
        val monthIncome: Money,
        val monthExpense: Money,
        val months: List<TransactionMonth>
    ) : HomeUiState
}

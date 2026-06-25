package com.hacybeyker.finflow.feature.transactions.ui.home

import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.feature.transactions.domain.Transaction

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Content(val balance: Money, val transactions: List<Transaction>) : HomeUiState
}

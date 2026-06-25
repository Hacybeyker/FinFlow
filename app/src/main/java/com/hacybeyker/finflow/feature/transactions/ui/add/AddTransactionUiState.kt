package com.hacybeyker.finflow.feature.transactions.ui.add

import com.hacybeyker.finflow.feature.transactions.domain.TransactionType
import java.time.LocalDate

data class AddTransactionUiState(
    val amountInput: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "",
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
    val error: AddTransactionError? = null,
    val isSaved: Boolean = false
)

enum class AddTransactionError {
    INVALID_AMOUNT,
    INVALID_CATEGORY
}

sealed interface AddTransactionIntent {
    data class AmountChanged(val value: String) : AddTransactionIntent
    data class TypeChanged(val type: TransactionType) : AddTransactionIntent
    data class CategoryChanged(val value: String) : AddTransactionIntent
    data class DateChanged(val date: LocalDate) : AddTransactionIntent
    data class NoteChanged(val value: String) : AddTransactionIntent
    data object Save : AddTransactionIntent
}

package com.hacybeyker.finflow.feature.transactions.ui.add

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hacybeyker.finflow.core.ui.format.parseMoneyOrNull
import com.hacybeyker.finflow.feature.transactions.domain.Category
import com.hacybeyker.finflow.feature.transactions.domain.Transaction
import com.hacybeyker.finflow.feature.transactions.domain.usecase.AddTransactionUseCase
import com.hacybeyker.finflow.feature.transactions.domain.usecase.TransactionWriteResult
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
@HiltViewModel
class AddTransactionViewModel @Inject constructor(private val addTransaction: AddTransactionUseCase, clock: Clock) :
    ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState(date = LocalDate.now(clock)))
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    fun onIntent(intent: AddTransactionIntent) {
        when (intent) {
            is AddTransactionIntent.AmountChanged -> _uiState.update {
                it.copy(amountInput = intent.value, error = null)
            }

            is AddTransactionIntent.TypeChanged -> _uiState.update { it.copy(type = intent.type) }
            is AddTransactionIntent.CategoryChanged -> _uiState.update {
                it.copy(category = intent.value, error = null)
            }

            is AddTransactionIntent.DateChanged -> _uiState.update { it.copy(date = intent.date) }
            is AddTransactionIntent.NoteChanged -> _uiState.update { it.copy(note = intent.value) }
            AddTransactionIntent.Save -> save()
        }
    }

    private fun save() {
        val state = _uiState.value
        val amount = parseMoneyOrNull(state.amountInput)
        if (amount == null || !amount.isPositive) {
            _uiState.update { it.copy(error = AddTransactionError.INVALID_AMOUNT) }
            return
        }
        if (state.category.isBlank()) {
            _uiState.update { it.copy(error = AddTransactionError.INVALID_CATEGORY) }
            return
        }
        viewModelScope.launch {
            val result = addTransaction(
                Transaction(
                    amount = amount,
                    type = state.type,
                    category = Category(name = state.category.trim()),
                    date = state.date,
                    note = state.note.trim()
                )
            )
            when (result) {
                TransactionWriteResult.Success -> _uiState.update { it.copy(isSaved = true) }
                TransactionWriteResult.InvalidAmount ->
                    _uiState.update { it.copy(error = AddTransactionError.INVALID_AMOUNT) }
            }
        }
    }
}

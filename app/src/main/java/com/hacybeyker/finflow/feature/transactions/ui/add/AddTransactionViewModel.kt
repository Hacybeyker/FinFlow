package com.hacybeyker.finflow.feature.transactions.ui.add

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hacybeyker.finflow.core.domain.Category
import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.domain.Transaction
import com.hacybeyker.finflow.core.domain.TransactionType
import com.hacybeyker.finflow.core.ui.format.parseMoneyOrNull
import com.hacybeyker.finflow.feature.transactions.domain.usecase.AddCategoryUseCase
import com.hacybeyker.finflow.feature.transactions.domain.usecase.AddTransactionUseCase
import com.hacybeyker.finflow.feature.transactions.domain.usecase.CategorySaveResult
import com.hacybeyker.finflow.feature.transactions.domain.usecase.GetCategoriesUseCase
import com.hacybeyker.finflow.feature.transactions.domain.usecase.GetTransactionByIdUseCase
import com.hacybeyker.finflow.feature.transactions.domain.usecase.TransactionWriteResult
import com.hacybeyker.finflow.feature.transactions.domain.usecase.UpdateTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val addTransaction: AddTransactionUseCase,
    private val updateTransaction: UpdateTransactionUseCase,
    private val addCategory: AddCategoryUseCase,
    private val getTransactionById: GetTransactionByIdUseCase,
    getCategories: GetCategoriesUseCase,
    clock: Clock
) : ViewModel() {

    private val form = MutableStateFlow(FormState(date = LocalDate.now(clock)))

    val uiState: StateFlow<AddTransactionUiState> =
        combine(form, getCategories()) { form, categories ->
            AddTransactionUiState(
                amountInput = form.amountInput,
                type = form.type,
                categories = categories,
                selectedCategory = form.selectedCategory,
                date = form.date,
                note = form.note,
                error = form.error,
                isEditing = form.id != 0L,
                isSaved = form.isSaved
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = AddTransactionUiState(date = LocalDate.now(clock))
        )

    fun onIntent(intent: AddTransactionIntent) {
        when (intent) {
            is AddTransactionIntent.Load -> load(intent.transactionId)
            is AddTransactionIntent.AmountChanged -> form.update { it.copy(amountInput = intent.value, error = null) }
            is AddTransactionIntent.TypeChanged -> form.update { it.copy(type = intent.type) }
            is AddTransactionIntent.CategorySelected ->
                form.update { it.copy(selectedCategory = intent.category, error = null) }

            is AddTransactionIntent.CreateCategory -> createCategory(intent.name)
            is AddTransactionIntent.DateChanged -> form.update { it.copy(date = intent.date) }
            is AddTransactionIntent.NoteChanged -> form.update { it.copy(note = intent.value) }
            AddTransactionIntent.Save -> save()
        }
    }

    private fun load(transactionId: Long) {
        if (form.value.id == transactionId) return
        viewModelScope.launch {
            val transaction = getTransactionById(transactionId) ?: return@launch
            form.update {
                it.copy(
                    id = transaction.id,
                    amountInput = transaction.amount.toInput(),
                    type = transaction.type,
                    selectedCategory = transaction.category,
                    date = transaction.date,
                    note = transaction.note,
                    error = null
                )
            }
        }
    }

    private fun createCategory(name: String) = viewModelScope.launch {
        when (val result = addCategory(name)) {
            is CategorySaveResult.Success ->
                form.update { it.copy(selectedCategory = result.category, error = null) }

            CategorySaveResult.Duplicate -> form.update { state ->
                val existing = uiState.value.categories.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }
                state.copy(selectedCategory = existing ?: state.selectedCategory, error = null)
            }

            CategorySaveResult.InvalidName -> Unit
        }
    }

    private fun save() {
        val state = form.value
        val amount = parseMoneyOrNull(state.amountInput)
        if (amount == null || !amount.isPositive) {
            form.update { it.copy(error = AddTransactionError.INVALID_AMOUNT) }
            return
        }
        val category = state.selectedCategory
        if (category == null) {
            form.update { it.copy(error = AddTransactionError.INVALID_CATEGORY) }
            return
        }
        val transaction = Transaction(
            id = state.id,
            amount = amount,
            type = state.type,
            category = category,
            date = state.date,
            note = state.note.trim()
        )
        viewModelScope.launch {
            val result = if (state.id == 0L) addTransaction(transaction) else updateTransaction(transaction)
            when (result) {
                TransactionWriteResult.Success -> form.update { it.copy(isSaved = true) }
                TransactionWriteResult.InvalidAmount ->
                    form.update { it.copy(error = AddTransactionError.INVALID_AMOUNT) }
            }
        }
    }

    private data class FormState(
        val id: Long = 0,
        val amountInput: String = "",
        val type: TransactionType = TransactionType.EXPENSE,
        val selectedCategory: Category? = null,
        val date: LocalDate,
        val note: String = "",
        val error: AddTransactionError? = null,
        val isSaved: Boolean = false
    )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

private const val FRACTION_DIGITS = 2

/** Renders an exact [Money] back into an editable plain-decimal string (e.g. `4550` → "45.50"). */
private fun Money.toInput(): String = BigDecimal.valueOf(minorUnits).movePointLeft(FRACTION_DIGITS).toPlainString()

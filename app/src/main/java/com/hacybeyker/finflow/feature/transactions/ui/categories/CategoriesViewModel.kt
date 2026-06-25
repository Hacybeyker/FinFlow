package com.hacybeyker.finflow.feature.transactions.ui.categories

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hacybeyker.finflow.feature.transactions.domain.Category
import com.hacybeyker.finflow.feature.transactions.domain.usecase.AddCategoryUseCase
import com.hacybeyker.finflow.feature.transactions.domain.usecase.CategorySaveResult
import com.hacybeyker.finflow.feature.transactions.domain.usecase.DeleteCategoryUseCase
import com.hacybeyker.finflow.feature.transactions.domain.usecase.GetCategoriesUseCase
import com.hacybeyker.finflow.feature.transactions.domain.usecase.RenameCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Stable
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    getCategories: GetCategoriesUseCase,
    private val addCategory: AddCategoryUseCase,
    private val renameCategory: RenameCategoryUseCase,
    private val deleteCategory: DeleteCategoryUseCase
) : ViewModel() {

    private val dialog = MutableStateFlow<CategoryDialog>(CategoryDialog.Hidden)
    private val nameError = MutableStateFlow<CategoryNameError?>(null)

    val uiState: StateFlow<CategoriesUiState> =
        combine(getCategories(), dialog, nameError) { categories, dialog, error ->
            CategoriesUiState(categories = categories, isLoading = false, dialog = dialog, nameError = error)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = CategoriesUiState()
        )

    fun openAdd() {
        nameError.value = null
        dialog.value = CategoryDialog.Add
    }

    fun openRename(category: Category) {
        nameError.value = null
        dialog.value = CategoryDialog.Rename(category)
    }

    fun openDelete(category: Category) {
        dialog.value = CategoryDialog.ConfirmDelete(category)
    }

    fun dismissDialog() {
        dialog.value = CategoryDialog.Hidden
        nameError.value = null
    }

    fun submitAdd(name: String) = viewModelScope.launch { handle(addCategory(name)) }

    fun submitRename(category: Category, name: String) =
        viewModelScope.launch { handle(renameCategory(category, name)) }

    fun confirmDelete(category: Category) = viewModelScope.launch {
        deleteCategory(category)
        dismissDialog()
    }

    private fun handle(result: CategorySaveResult) {
        when (result) {
            is CategorySaveResult.Success -> dismissDialog()
            CategorySaveResult.InvalidName -> nameError.value = CategoryNameError.INVALID
            CategorySaveResult.Duplicate -> nameError.value = CategoryNameError.DUPLICATE
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

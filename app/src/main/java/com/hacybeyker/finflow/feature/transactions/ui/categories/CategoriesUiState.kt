package com.hacybeyker.finflow.feature.transactions.ui.categories

import com.hacybeyker.finflow.core.domain.Category

data class CategoriesUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
    val dialog: CategoryDialog = CategoryDialog.Hidden,
    val nameError: CategoryNameError? = null
)

sealed interface CategoryDialog {
    data object Hidden : CategoryDialog
    data object Add : CategoryDialog
    data class Rename(val category: Category) : CategoryDialog
    data class ConfirmDelete(val category: Category) : CategoryDialog
}

enum class CategoryNameError { INVALID, DUPLICATE }

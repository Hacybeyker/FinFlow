package com.hacybeyker.finflow.feature.transactions.domain.usecase

import com.hacybeyker.finflow.feature.transactions.domain.Category

/** Outcome of creating or renaming a category (shared by add and rename). */
sealed interface CategorySaveResult {
    data class Success(val category: Category) : CategorySaveResult
    data object InvalidName : CategorySaveResult
    data object Duplicate : CategorySaveResult
}

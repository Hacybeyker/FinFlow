package com.hacybeyker.finflow.feature.transactions.domain.usecase

import com.hacybeyker.finflow.feature.transactions.domain.Category
import com.hacybeyker.finflow.feature.transactions.domain.CategoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class RenameCategoryUseCase @Inject constructor(private val repository: CategoryRepository) {

    suspend operator fun invoke(category: Category, newName: String): CategorySaveResult {
        val trimmed = newName.trim()
        return when {
            trimmed.isBlank() -> CategorySaveResult.InvalidName
            clashesWithAnother(category.id, trimmed) -> CategorySaveResult.Duplicate
            else -> {
                val renamed = category.copy(name = trimmed)
                repository.update(renamed)
                CategorySaveResult.Success(renamed)
            }
        }
    }

    private suspend fun clashesWithAnother(id: Long, name: String): Boolean =
        repository.observeAll().first().any { it.id != id && it.name.equals(name, ignoreCase = true) }
}

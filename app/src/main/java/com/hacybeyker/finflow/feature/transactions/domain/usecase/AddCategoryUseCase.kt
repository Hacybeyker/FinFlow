package com.hacybeyker.finflow.feature.transactions.domain.usecase

import com.hacybeyker.finflow.core.domain.Category
import com.hacybeyker.finflow.feature.transactions.domain.CategoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class AddCategoryUseCase @Inject constructor(private val repository: CategoryRepository) {

    suspend operator fun invoke(name: String): CategorySaveResult {
        val trimmed = name.trim()
        return when {
            trimmed.isBlank() -> CategorySaveResult.InvalidName
            isDuplicate(trimmed) -> CategorySaveResult.Duplicate
            else -> {
                val id = repository.add(Category(name = trimmed))
                CategorySaveResult.Success(Category(id = id, name = trimmed))
            }
        }
    }

    private suspend fun isDuplicate(name: String): Boolean =
        repository.observeAll().first().any { it.name.equals(name, ignoreCase = true) }
}

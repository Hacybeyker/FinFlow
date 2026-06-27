package com.hacybeyker.finflow.feature.transactions.domain.usecase

import com.hacybeyker.finflow.core.domain.Category
import com.hacybeyker.finflow.feature.transactions.domain.CategoryRepository
import javax.inject.Inject

class DeleteCategoryUseCase @Inject constructor(private val repository: CategoryRepository) {

    suspend operator fun invoke(category: Category) = repository.delete(category)
}

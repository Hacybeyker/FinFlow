package com.hacybeyker.finflow.feature.transactions.domain.usecase

import com.hacybeyker.finflow.core.domain.Category
import com.hacybeyker.finflow.feature.transactions.domain.CategoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetCategoriesUseCase @Inject constructor(private val repository: CategoryRepository) {

    operator fun invoke(): Flow<List<Category>> = repository.observeAll()
}

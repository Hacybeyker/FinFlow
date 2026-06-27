package com.hacybeyker.finflow.feature.transactions.data

import com.hacybeyker.finflow.core.domain.Category
import com.hacybeyker.finflow.feature.transactions.data.local.CategoryDao
import com.hacybeyker.finflow.feature.transactions.data.local.CategoryEntity
import com.hacybeyker.finflow.feature.transactions.domain.CategoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCategoryRepository @Inject constructor(private val dao: CategoryDao) : CategoryRepository {

    override fun observeAll(): Flow<List<Category>> =
        dao.observeAll().map { entities -> entities.map(CategoryEntity::toDomain) }

    override suspend fun add(category: Category): Long = dao.insert(category.toEntity())

    override suspend fun update(category: Category) = dao.update(category.toEntity())

    override suspend fun delete(category: Category) = dao.delete(category.toEntity())
}

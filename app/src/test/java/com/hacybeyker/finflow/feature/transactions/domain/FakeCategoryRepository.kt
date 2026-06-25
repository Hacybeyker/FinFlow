package com.hacybeyker.finflow.feature.transactions.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [CategoryRepository] for fast, deterministic domain tests (no emulator, no Room). */
class FakeCategoryRepository(initial: List<Category> = emptyList()) : CategoryRepository {

    private val categories = MutableStateFlow(initial)
    private var nextId = initial.maxOfOrNull { it.id }?.plus(1) ?: 1L

    override fun observeAll(): Flow<List<Category>> = categories

    override suspend fun add(category: Category): Long {
        val id = nextId++
        categories.value = categories.value + category.copy(id = id)
        return id
    }

    override suspend fun update(category: Category) {
        categories.value = categories.value.map { if (it.id == category.id) category else it }
    }

    override suspend fun delete(category: Category) {
        categories.value = categories.value.filterNot { it.id == category.id }
    }
}

/** Builds a [Category] with sensible defaults so each test only sets what it cares about. */
fun category(id: Long = 1, name: String = "Test"): Category = Category(id = id, name = name)

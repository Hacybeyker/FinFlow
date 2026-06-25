package com.hacybeyker.finflow.feature.transactions.domain

import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun observeAll(): Flow<List<Category>>

    /** Inserts [category] and returns the id assigned by the data layer. */
    suspend fun add(category: Category): Long

    suspend fun update(category: Category)

    /** Deletes [category]; its transactions are removed too (ON DELETE CASCADE). */
    suspend fun delete(category: Category)
}

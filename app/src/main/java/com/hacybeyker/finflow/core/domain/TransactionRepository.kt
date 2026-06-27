package com.hacybeyker.finflow.core.domain

import java.time.YearMonth
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    fun observeAll(): Flow<List<Transaction>>

    fun observeByMonth(month: YearMonth): Flow<List<Transaction>>

    suspend fun getById(id: Long): Transaction?

    suspend fun add(transaction: Transaction)

    suspend fun update(transaction: Transaction)

    suspend fun delete(transaction: Transaction)
}

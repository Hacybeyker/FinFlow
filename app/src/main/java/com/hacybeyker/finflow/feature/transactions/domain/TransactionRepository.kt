package com.hacybeyker.finflow.feature.transactions.domain

import java.time.YearMonth
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    fun observeAll(): Flow<List<Transaction>>

    fun observeByMonth(month: YearMonth): Flow<List<Transaction>>

    suspend fun add(transaction: Transaction)
}

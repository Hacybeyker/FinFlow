package com.hacybeyker.finflow.feature.transactions.domain

import java.time.YearMonth
import kotlinx.coroutines.flow.Flow

/**
 * Contract for reading and writing transactions. Defined in the domain and implemented in `data`
 * (dependency inversion). Reads are reactive ([Flow]) so the UI updates itself when data changes.
 */
interface TransactionRepository {

    fun observeAll(): Flow<List<Transaction>>

    fun observeByMonth(month: YearMonth): Flow<List<Transaction>>

    suspend fun add(transaction: Transaction)
}

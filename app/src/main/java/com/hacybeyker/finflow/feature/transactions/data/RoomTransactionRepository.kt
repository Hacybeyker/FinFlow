package com.hacybeyker.finflow.feature.transactions.data

import com.hacybeyker.finflow.feature.transactions.data.local.TransactionDao
import com.hacybeyker.finflow.feature.transactions.data.local.TransactionEntity
import com.hacybeyker.finflow.feature.transactions.domain.Transaction
import com.hacybeyker.finflow.feature.transactions.domain.TransactionRepository
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed [TransactionRepository]. Room already runs queries and suspend inserts off the main
 * thread on its own executors, so no extra dispatcher is injected here.
 */
class RoomTransactionRepository @Inject constructor(private val dao: TransactionDao) : TransactionRepository {

    override fun observeAll(): Flow<List<Transaction>> =
        dao.observeAll().map { entities -> entities.map(TransactionEntity::toDomain) }

    override fun observeByMonth(month: YearMonth): Flow<List<Transaction>> {
        val startEpochDay = month.atDay(1).toEpochDay()
        val endEpochDay = month.atEndOfMonth().toEpochDay()
        return dao.observeByEpochDayRange(startEpochDay, endEpochDay)
            .map { entities -> entities.map(TransactionEntity::toDomain) }
    }

    override suspend fun add(transaction: Transaction) = dao.insert(transaction.toEntity())
}

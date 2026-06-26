package com.hacybeyker.finflow.feature.transactions.data

import com.hacybeyker.finflow.feature.transactions.data.local.TransactionDao
import com.hacybeyker.finflow.feature.transactions.data.local.TransactionWithCategory
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
        dao.observeAll().map { rows -> rows.map(TransactionWithCategory::toDomain) }

    override fun observeByMonth(month: YearMonth): Flow<List<Transaction>> {
        val startEpochDay = month.atDay(1).toEpochDay()
        val endEpochDay = month.atEndOfMonth().toEpochDay()
        return dao.observeByEpochDayRange(startEpochDay, endEpochDay)
            .map { rows -> rows.map(TransactionWithCategory::toDomain) }
    }

    override suspend fun getById(id: Long): Transaction? = dao.getById(id)?.toDomain()

    override suspend fun add(transaction: Transaction) = dao.insert(transaction.toEntity())

    override suspend fun update(transaction: Transaction) = dao.update(transaction.toEntity())

    override suspend fun delete(transaction: Transaction) = dao.delete(transaction.toEntity())
}

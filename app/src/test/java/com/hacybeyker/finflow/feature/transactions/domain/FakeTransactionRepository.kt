package com.hacybeyker.finflow.feature.transactions.domain

import com.hacybeyker.finflow.core.domain.Category
import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.domain.Transaction
import com.hacybeyker.finflow.core.domain.TransactionRepository
import com.hacybeyker.finflow.core.domain.TransactionType
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory [TransactionRepository] for fast, deterministic domain tests (no emulator, no Room). */
class FakeTransactionRepository(initial: List<Transaction> = emptyList()) : TransactionRepository {

    private val transactions = MutableStateFlow(initial)

    override fun observeAll(): Flow<List<Transaction>> = transactions

    override fun observeByMonth(month: YearMonth): Flow<List<Transaction>> =
        transactions.map { list -> list.filter { YearMonth.from(it.date) == month } }

    override suspend fun getById(id: Long): Transaction? = transactions.value.firstOrNull { it.id == id }

    override suspend fun add(transaction: Transaction) {
        transactions.value = transactions.value + transaction
    }

    override suspend fun update(transaction: Transaction) {
        transactions.value = transactions.value.map { if (it.id == transaction.id) transaction else it }
    }

    override suspend fun delete(transaction: Transaction) {
        transactions.value = transactions.value.filterNot { it.id == transaction.id }
    }
}

/** Builds a [Transaction] with sensible defaults so each test only sets what it cares about. */
fun transaction(
    id: Long = 0,
    amount: Long = 1000,
    type: TransactionType = TransactionType.EXPENSE,
    date: LocalDate = LocalDate.of(2026, 6, 15),
    category: Category = Category(id = 1, name = "Test"),
    note: String = ""
): Transaction = Transaction(
    id = id,
    amount = Money(amount),
    type = type,
    category = category,
    date = date,
    note = note
)

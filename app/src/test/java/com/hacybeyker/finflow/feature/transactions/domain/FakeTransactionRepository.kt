package com.hacybeyker.finflow.feature.transactions.domain

import com.hacybeyker.finflow.core.domain.Money
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

    override suspend fun add(transaction: Transaction) {
        transactions.value = transactions.value + transaction
    }
}

/** Builds a [Transaction] with sensible defaults so each test only sets what it cares about. */
fun transaction(
    amount: Long = 1000,
    type: TransactionType = TransactionType.EXPENSE,
    date: LocalDate = LocalDate.of(2026, 6, 15),
    category: Category = Category(id = 1, name = "Test"),
    note: String = ""
): Transaction = Transaction(
    amount = Money(amount),
    type = type,
    category = category,
    date = date,
    note = note
)

package com.hacybeyker.finflow.feature.transactions.domain.usecase

import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.domain.TransactionType
import com.hacybeyker.finflow.feature.transactions.domain.FakeTransactionRepository
import com.hacybeyker.finflow.feature.transactions.domain.transaction
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetTransactionHistoryUseCaseTest {

    @Test
    fun `groups by month, newest month and newest transaction first`() = runTest {
        val repository = FakeTransactionRepository(
            listOf(
                transaction(id = 1, amount = 100, date = LocalDate.of(2026, 5, 20)),
                transaction(id = 2, amount = 200, date = LocalDate.of(2026, 6, 1)),
                transaction(id = 3, amount = 300, date = LocalDate.of(2026, 6, 15)),
                transaction(id = 4, amount = 400, date = LocalDate.of(2025, 12, 31))
            )
        )

        val months = GetTransactionHistoryUseCase(repository)().first()

        assertEquals(
            listOf(YearMonth.of(2026, 6), YearMonth.of(2026, 5), YearMonth.of(2025, 12)),
            months.map { it.month }
        )
        assertEquals(listOf(3L, 2L), months.first().transactions.map { it.id })
    }

    @Test
    fun `same-date ties break by id descending so the latest insert is on top`() = runTest {
        val date = LocalDate.of(2026, 6, 10)
        val repository = FakeTransactionRepository(
            listOf(
                transaction(id = 1, amount = 100, date = date),
                transaction(id = 2, amount = 200, date = date)
            )
        )

        val months = GetTransactionHistoryUseCase(repository)().first()

        assertEquals(listOf(2L, 1L), months.single().transactions.map { it.id })
    }

    @Test
    fun `each month carries its gross income, gross expense and net total`() = runTest {
        val repository = FakeTransactionRepository(
            listOf(
                transaction(id = 1, amount = 2000, type = TransactionType.INCOME, date = LocalDate.of(2026, 6, 1)),
                transaction(id = 2, amount = 500, type = TransactionType.EXPENSE, date = LocalDate.of(2026, 6, 10)),
                transaction(id = 3, amount = 300, type = TransactionType.EXPENSE, date = LocalDate.of(2026, 5, 5))
            )
        )

        val months = GetTransactionHistoryUseCase(repository)().first()

        assertEquals(Money(2000), months.first().income)
        assertEquals(Money(500), months.first().expense)
        assertEquals(Money(1500), months.first().total)
        assertEquals(Money.ZERO, months.last().income)
        assertEquals(Money(300), months.last().expense)
        assertEquals(Money(-300), months.last().total)
    }
}

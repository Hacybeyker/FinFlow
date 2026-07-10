package com.hacybeyker.finflow.feature.widget.domain.usecase

import com.hacybeyker.finflow.core.domain.FakeTransactionRepository
import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.domain.TransactionType
import com.hacybeyker.finflow.core.domain.transaction
import com.hacybeyker.finflow.core.domain.usecase.GetBalanceUseCase
import com.hacybeyker.finflow.core.domain.usecase.GetTransactionHistoryUseCase
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetWidgetSummaryUseCaseTest {

    // "Today" is 2026-06-15 for every test, so the current month is June 2026.
    private val clock = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneOffset.UTC)

    private fun useCase(repository: FakeTransactionRepository) = GetWidgetSummaryUseCase(
        getBalance = GetBalanceUseCase(repository),
        getTransactionHistory = GetTransactionHistoryUseCase(repository),
        clock = clock
    )

    @Test
    fun `balance is all-time while income and expense are current-month only`() = runTest {
        val repository = FakeTransactionRepository(
            listOf(
                transaction(id = 1, amount = 5000, type = TransactionType.INCOME, date = LocalDate.of(2026, 5, 1)),
                transaction(id = 2, amount = 2000, type = TransactionType.INCOME, date = LocalDate.of(2026, 6, 5)),
                transaction(id = 3, amount = 500, type = TransactionType.EXPENSE, date = LocalDate.of(2026, 6, 10))
            )
        )

        val summary = useCase(repository)().first()

        assertEquals(Money(6500), summary.balance)
        assertEquals(Money(2000), summary.monthIncome)
        assertEquals(Money(500), summary.monthExpense)
    }

    @Test
    fun `empty current month keeps the all-time balance and zero month stats`() = runTest {
        val repository = FakeTransactionRepository(
            listOf(transaction(id = 1, amount = 3000, type = TransactionType.INCOME, date = LocalDate.of(2026, 4, 1)))
        )

        val summary = useCase(repository)().first()

        assertEquals(Money(3000), summary.balance)
        assertEquals(Money.ZERO, summary.monthIncome)
        assertEquals(Money.ZERO, summary.monthExpense)
    }

    @Test
    fun `latest takes the newest three transactions across month boundaries`() = runTest {
        val repository = FakeTransactionRepository(
            listOf(
                transaction(id = 1, date = LocalDate.of(2026, 4, 1)),
                transaction(id = 2, date = LocalDate.of(2026, 5, 20)),
                transaction(id = 3, date = LocalDate.of(2026, 6, 1)),
                transaction(id = 4, date = LocalDate.of(2026, 6, 10))
            )
        )

        val summary = useCase(repository)().first()

        assertEquals(listOf(4L, 3L, 2L), summary.latest.map { it.id })
    }

    @Test
    fun `no transactions yields an all-zero summary with no latest movements`() = runTest {
        val summary = useCase(FakeTransactionRepository())().first()

        assertEquals(Money.ZERO, summary.balance)
        assertEquals(Money.ZERO, summary.monthIncome)
        assertEquals(Money.ZERO, summary.monthExpense)
        assertEquals(emptyList<Long>(), summary.latest.map { it.id })
    }
}

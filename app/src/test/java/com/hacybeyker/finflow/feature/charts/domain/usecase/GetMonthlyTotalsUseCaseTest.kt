package com.hacybeyker.finflow.feature.charts.domain.usecase

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

class GetMonthlyTotalsUseCaseTest {

    @Test
    fun `returns a fixed window in chronological order with empty months as zero`() = runTest {
        val repository = FakeTransactionRepository(
            listOf(
                transaction(amount = 2000, type = TransactionType.INCOME, date = LocalDate.of(2026, 6, 1)),
                transaction(amount = 500, type = TransactionType.EXPENSE, date = LocalDate.of(2026, 6, 10)),
                transaction(amount = 300, type = TransactionType.EXPENSE, date = LocalDate.of(2026, 4, 5))
                // May 2026 has no movements
            )
        )

        val result = GetMonthlyTotalsUseCase(repository)(monthCount = 3, until = YearMonth.of(2026, 6)).first()

        assertEquals(
            listOf(YearMonth.of(2026, 4), YearMonth.of(2026, 5), YearMonth.of(2026, 6)),
            result.map {
                it.month
            }
        )

        assertEquals(Money.ZERO, result[0].income)
        assertEquals(Money(300), result[0].expense)
        assertEquals(Money.ZERO, result[1].income)
        assertEquals(Money.ZERO, result[1].expense)
        assertEquals(Money(2000), result[2].income)
        assertEquals(Money(500), result[2].expense)
    }
}

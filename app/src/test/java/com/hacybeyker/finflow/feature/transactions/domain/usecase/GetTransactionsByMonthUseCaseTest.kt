package com.hacybeyker.finflow.feature.transactions.domain.usecase

import com.hacybeyker.finflow.core.domain.FakeTransactionRepository
import com.hacybeyker.finflow.core.domain.transaction
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetTransactionsByMonthUseCaseTest {

    @Test
    fun `returns only transactions within the requested month`() = runTest {
        val repository = FakeTransactionRepository(
            listOf(
                transaction(date = LocalDate.of(2026, 6, 1)),
                transaction(date = LocalDate.of(2026, 6, 28)),
                transaction(date = LocalDate.of(2026, 5, 30)),
                transaction(date = LocalDate.of(2026, 7, 1))
            )
        )

        val june = GetTransactionsByMonthUseCase(repository).invoke(YearMonth.of(2026, 6)).first()

        assertEquals(2, june.size)
    }

    @Test
    fun `returns empty when no transaction matches the month`() = runTest {
        val repository = FakeTransactionRepository(
            listOf(transaction(date = LocalDate.of(2026, 5, 30)))
        )

        val june = GetTransactionsByMonthUseCase(repository).invoke(YearMonth.of(2026, 6)).first()

        assertTrue(june.isEmpty())
    }
}

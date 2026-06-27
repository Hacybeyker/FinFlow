package com.hacybeyker.finflow.feature.charts.domain.usecase

import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.domain.TransactionType
import com.hacybeyker.finflow.feature.transactions.domain.FakeTransactionRepository
import com.hacybeyker.finflow.feature.transactions.domain.category
import com.hacybeyker.finflow.feature.transactions.domain.transaction
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetSpendingByCategoryUseCaseTest {

    private val comida = category(id = 1, name = "Comida")
    private val ocio = category(id = 2, name = "Ocio")
    private val nomina = category(id = 3, name = "Nómina")

    @Test
    fun `groups current-month expenses by category, summed and sorted descending`() = runTest {
        val repository = FakeTransactionRepository(
            listOf(
                transaction(
                    amount = 1000,
                    type = TransactionType.EXPENSE,
                    date = LocalDate.of(2026, 6, 1),
                    category = comida
                ),
                transaction(
                    amount = 500,
                    type = TransactionType.EXPENSE,
                    date = LocalDate.of(2026, 6, 2),
                    category = comida
                ),
                transaction(
                    amount = 2000,
                    type = TransactionType.EXPENSE,
                    date = LocalDate.of(2026, 6, 3),
                    category = ocio
                ),
                // income is ignored
                transaction(
                    amount = 9999,
                    type = TransactionType.INCOME,
                    date = LocalDate.of(2026, 6, 4),
                    category = nomina
                ),
                // a previous month is excluded
                transaction(
                    amount = 700,
                    type = TransactionType.EXPENSE,
                    date = LocalDate.of(2026, 5, 30),
                    category = comida
                )
            )
        )

        val result = GetSpendingByCategoryUseCase(repository)(YearMonth.of(2026, 6)).first()

        assertEquals(2, result.size)
        assertEquals(ocio, result[0].category)
        assertEquals(Money(2000), result[0].total)
        assertEquals(comida, result[1].category)
        assertEquals(Money(1500), result[1].total)
    }

    @Test
    fun `a month without expenses yields an empty list`() = runTest {
        val repository = FakeTransactionRepository(
            listOf(
                transaction(
                    amount = 100,
                    type = TransactionType.INCOME,
                    date = LocalDate.of(2026, 6, 1),
                    category = nomina
                )
            )
        )

        val result = GetSpendingByCategoryUseCase(repository)(YearMonth.of(2026, 6)).first()

        assertEquals(emptyList<Any>(), result)
    }
}

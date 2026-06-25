package com.hacybeyker.finflow.feature.transactions.ui.home

import app.cash.turbine.test
import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.test.MainDispatcherRule
import com.hacybeyker.finflow.feature.transactions.domain.FakeTransactionRepository
import com.hacybeyker.finflow.feature.transactions.domain.TransactionType
import com.hacybeyker.finflow.feature.transactions.domain.transaction
import com.hacybeyker.finflow.feature.transactions.domain.usecase.GetBalanceUseCase
import com.hacybeyker.finflow.feature.transactions.domain.usecase.GetTransactionsByMonthUseCase
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock = Clock.fixed(
        LocalDate.of(2026, 6, 15).atStartOfDay(ZoneOffset.UTC).toInstant(),
        ZoneOffset.UTC
    )

    private fun viewModel(repository: FakeTransactionRepository) = HomeViewModel(
        getBalance = GetBalanceUseCase(repository),
        getTransactionsByMonth = GetTransactionsByMonthUseCase(repository),
        clock = clock
    )

    @Test
    fun `keeps the all-time balance even when the current month has no transactions`() = runTest {
        val repository = FakeTransactionRepository(
            listOf(transaction(amount = 2000, type = TransactionType.INCOME, date = LocalDate.of(2026, 5, 20)))
        )

        viewModel(repository).uiState.test {
            assertEquals(HomeUiState.Loading, awaitItem())
            val content = awaitItem() as HomeUiState.Content
            // May is a previous month: the list is empty, but the balance is all-time (2000).
            assertEquals(Money(2000), content.balance)
            assertTrue(content.transactions.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Content with balance and the current month transactions`() = runTest {
        val repository = FakeTransactionRepository(
            listOf(
                transaction(amount = 2000, type = TransactionType.INCOME, date = LocalDate.of(2026, 6, 1)),
                transaction(amount = 500, type = TransactionType.EXPENSE, date = LocalDate.of(2026, 6, 10)),
                transaction(amount = 300, type = TransactionType.EXPENSE, date = LocalDate.of(2026, 5, 30))
            )
        )

        viewModel(repository).uiState.test {
            assertEquals(HomeUiState.Loading, awaitItem())
            val content = awaitItem() as HomeUiState.Content
            // balance is all-time (2000 − 500 − 300); the list is current month only (2 of 3).
            assertEquals(Money(1200), content.balance)
            assertEquals(2, content.transactions.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

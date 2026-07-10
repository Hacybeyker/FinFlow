package com.hacybeyker.finflow.feature.transactions.ui.home

import app.cash.turbine.test
import com.hacybeyker.finflow.core.domain.FakeTransactionRepository
import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.domain.TransactionType
import com.hacybeyker.finflow.core.domain.transaction
import com.hacybeyker.finflow.core.domain.usecase.GetBalanceUseCase
import com.hacybeyker.finflow.core.domain.usecase.GetTransactionHistoryUseCase
import com.hacybeyker.finflow.core.test.MainDispatcherRule
import com.hacybeyker.finflow.feature.transactions.domain.usecase.AddTransactionUseCase
import com.hacybeyker.finflow.feature.transactions.domain.usecase.DeleteTransactionUseCase
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
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
        getTransactionHistory = GetTransactionHistoryUseCase(repository),
        deleteTransaction = DeleteTransactionUseCase(repository),
        addTransaction = AddTransactionUseCase(repository),
        clock = clock
    )

    @Test
    fun `emits Content with the all-time balance, current month stats and the grouped history`() = runTest {
        val repository = FakeTransactionRepository(
            listOf(
                transaction(id = 1, amount = 2000, type = TransactionType.INCOME, date = LocalDate.of(2026, 6, 1)),
                transaction(id = 2, amount = 500, type = TransactionType.EXPENSE, date = LocalDate.of(2026, 6, 10)),
                transaction(id = 3, amount = 300, type = TransactionType.EXPENSE, date = LocalDate.of(2026, 5, 30))
            )
        )

        viewModel(repository).uiState.test {
            assertEquals(HomeUiState.Loading, awaitItem())
            val content = awaitItem() as HomeUiState.Content
            assertEquals(Money(1200), content.balance)
            // Hero stats are the current month (June) only; history covers every month, newest first.
            assertEquals(Money(2000), content.monthIncome)
            assertEquals(Money(500), content.monthExpense)
            assertEquals(listOf(YearMonth.of(2026, 6), YearMonth.of(2026, 5)), content.months.map { it.month })
            assertEquals(3, content.months.sumOf { it.transactions.size })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a current month without movements shows zero stats but keeps the all-time balance`() = runTest {
        val repository = FakeTransactionRepository(
            listOf(transaction(id = 1, amount = 2000, type = TransactionType.INCOME, date = LocalDate.of(2026, 5, 20)))
        )

        viewModel(repository).uiState.test {
            assertEquals(HomeUiState.Loading, awaitItem())
            val content = awaitItem() as HomeUiState.Content
            assertEquals(Money(2000), content.balance)
            assertEquals(Money.ZERO, content.monthIncome)
            assertEquals(Money.ZERO, content.monthExpense)
            assertEquals(1, content.months.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `an empty history emits Content with no months but the real balance`() = runTest {
        viewModel(FakeTransactionRepository()).uiState.test {
            assertEquals(HomeUiState.Loading, awaitItem())
            val content = awaitItem() as HomeUiState.Content
            assertEquals(Money.ZERO, content.balance)
            assertTrue(content.months.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `delete removes the transaction and undo restores it with the same id`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val target = transaction(id = 1, amount = 500, date = LocalDate.of(2026, 6, 10))
            val repository = FakeTransactionRepository(listOf(target))
            val viewModel = viewModel(repository)

            viewModel.delete(target)
            advanceUntilIdle()
            assertTrue(repository.observeAll().first().isEmpty())

            viewModel.undoDelete(target)
            advanceUntilIdle()
            val restored = repository.observeAll().first()
            assertEquals(1, restored.size)
            assertEquals(1L, restored.first().id)
        }
}

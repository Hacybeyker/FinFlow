package com.hacybeyker.finflow.feature.charts.ui

import com.hacybeyker.finflow.core.domain.FakeTransactionRepository
import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.domain.TransactionType
import com.hacybeyker.finflow.core.domain.transaction
import com.hacybeyker.finflow.core.test.MainDispatcherRule
import com.hacybeyker.finflow.feature.charts.domain.usecase.GetMonthlyTotalsUseCase
import com.hacybeyker.finflow.feature.charts.domain.usecase.GetSpendingByCategoryUseCase
import com.hacybeyker.finflow.feature.transactions.domain.category
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChartsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock = Clock.fixed(
        LocalDate.of(2026, 6, 22).atStartOfDay(ZoneOffset.UTC).toInstant(),
        ZoneOffset.UTC
    )

    private fun viewModel(repository: FakeTransactionRepository) = ChartsViewModel(
        getSpendingByCategory = GetSpendingByCategoryUseCase(repository),
        getMonthlyTotals = GetMonthlyTotalsUseCase(repository),
        clock = clock
    )

    @Test
    fun `starts loading and combines spending with monthly totals`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val comida = category(id = 1, name = "Comida")
            val repository = FakeTransactionRepository(
                listOf(
                    transaction(
                        amount = 1500,
                        type = TransactionType.EXPENSE,
                        date = LocalDate.of(2026, 6, 10),
                        category = comida
                    ),
                    transaction(
                        amount = 200000,
                        type = TransactionType.INCOME,
                        date = LocalDate.of(2026, 5, 1),
                        category = category(id = 2, name = "Nómina")
                    )
                )
            )
            val viewModel = viewModel(repository)
            assertEquals(ChartsUiState.Loading, viewModel.uiState.value)

            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            val state = viewModel.uiState.value as ChartsUiState.Content
            assertEquals(listOf(comida), state.spending.map { it.category })
            assertEquals(Money(1500), state.spending.single().total)
            val june = state.monthlyTotals.single { it.month == YearMonth.of(2026, 6) }
            assertEquals(Money(1500), june.expense)
            val may = state.monthlyTotals.single { it.month == YearMonth.of(2026, 5) }
            assertEquals(Money(200000), may.income)
        }

    @Test
    fun `an empty repository still produces content, not an eternal loading`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val viewModel = viewModel(FakeTransactionRepository())

            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            val state = viewModel.uiState.value as ChartsUiState.Content
            assertTrue(state.spending.isEmpty())
        }
}

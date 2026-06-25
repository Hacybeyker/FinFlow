package com.hacybeyker.finflow.feature.transactions.ui.add

import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.test.MainDispatcherRule
import com.hacybeyker.finflow.feature.transactions.domain.FakeTransactionRepository
import com.hacybeyker.finflow.feature.transactions.domain.TransactionType
import com.hacybeyker.finflow.feature.transactions.domain.usecase.AddTransactionUseCase
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock = Clock.fixed(
        LocalDate.of(2026, 6, 22).atStartOfDay(ZoneOffset.UTC).toInstant(),
        ZoneOffset.UTC
    )

    private fun viewModel(repository: FakeTransactionRepository) =
        AddTransactionViewModel(AddTransactionUseCase(repository), clock)

    @Test
    fun `saving a valid transaction stores it and marks state as saved`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val repository = FakeTransactionRepository()
            val viewModel = viewModel(repository)

            viewModel.onIntent(AddTransactionIntent.AmountChanged("45.50"))
            viewModel.onIntent(AddTransactionIntent.TypeChanged(TransactionType.EXPENSE))
            viewModel.onIntent(AddTransactionIntent.CategoryChanged("Compras"))
            viewModel.onIntent(AddTransactionIntent.Save)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isSaved)
            val stored = repository.observeAll().first()
            assertEquals(1, stored.size)
            assertEquals(Money(4550), stored.first().amount)
            assertEquals("Compras", stored.first().category.name)
        }

    @Test
    fun `a non-positive amount sets an error and stores nothing`() = runTest {
        val repository = FakeTransactionRepository()
        val viewModel = viewModel(repository)

        viewModel.onIntent(AddTransactionIntent.AmountChanged("0"))
        viewModel.onIntent(AddTransactionIntent.CategoryChanged("Compras"))
        viewModel.onIntent(AddTransactionIntent.Save)

        assertEquals(AddTransactionError.INVALID_AMOUNT, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isSaved)
        assertTrue(repository.observeAll().first().isEmpty())
    }

    @Test
    fun `a blank category sets an error and stores nothing`() = runTest {
        val repository = FakeTransactionRepository()
        val viewModel = viewModel(repository)

        viewModel.onIntent(AddTransactionIntent.AmountChanged("10"))
        viewModel.onIntent(AddTransactionIntent.Save)

        assertEquals(AddTransactionError.INVALID_CATEGORY, viewModel.uiState.value.error)
        assertTrue(repository.observeAll().first().isEmpty())
    }
}

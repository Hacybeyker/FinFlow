package com.hacybeyker.finflow.feature.transactions.ui.add

import com.hacybeyker.finflow.core.domain.FakeTransactionRepository
import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.domain.TransactionType
import com.hacybeyker.finflow.core.domain.transaction
import com.hacybeyker.finflow.core.test.MainDispatcherRule
import com.hacybeyker.finflow.feature.transactions.domain.FakeCategoryRepository
import com.hacybeyker.finflow.feature.transactions.domain.category
import com.hacybeyker.finflow.feature.transactions.domain.usecase.AddCategoryUseCase
import com.hacybeyker.finflow.feature.transactions.domain.usecase.AddTransactionUseCase
import com.hacybeyker.finflow.feature.transactions.domain.usecase.GetCategoriesUseCase
import com.hacybeyker.finflow.feature.transactions.domain.usecase.GetTransactionByIdUseCase
import com.hacybeyker.finflow.feature.transactions.domain.usecase.UpdateTransactionUseCase
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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

    private fun viewModel(txRepo: FakeTransactionRepository, catRepo: FakeCategoryRepository) = AddTransactionViewModel(
        addTransaction = AddTransactionUseCase(txRepo),
        updateTransaction = UpdateTransactionUseCase(txRepo),
        addCategory = AddCategoryUseCase(catRepo),
        getTransactionById = GetTransactionByIdUseCase(txRepo),
        getCategories = GetCategoriesUseCase(catRepo),
        clock = clock
    )

    @Test
    fun `saving a valid transaction stores it and marks state as saved`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val txRepo = FakeTransactionRepository()
            val catRepo = FakeCategoryRepository(listOf(category(id = 1, name = "Compras")))
            val viewModel = viewModel(txRepo, catRepo)
            backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.onIntent(AddTransactionIntent.AmountChanged("45.50"))
            viewModel.onIntent(AddTransactionIntent.CategorySelected(category(id = 1, name = "Compras")))
            viewModel.onIntent(AddTransactionIntent.Save)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isSaved)
            val stored = txRepo.observeAll().first()
            assertEquals(1, stored.size)
            assertEquals(Money(4550), stored.first().amount)
            assertEquals("Compras", stored.first().category.name)
        }

    @Test
    fun `a non-positive amount sets an error and stores nothing`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val txRepo = FakeTransactionRepository()
            val viewModel = viewModel(txRepo, FakeCategoryRepository())
            backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.onIntent(AddTransactionIntent.AmountChanged("0"))
            viewModel.onIntent(AddTransactionIntent.CategorySelected(category(id = 1, name = "Compras")))
            viewModel.onIntent(AddTransactionIntent.Save)
            advanceUntilIdle()

            assertEquals(AddTransactionError.INVALID_AMOUNT, viewModel.uiState.value.error)
            assertFalse(viewModel.uiState.value.isSaved)
            assertTrue(txRepo.observeAll().first().isEmpty())
        }

    @Test
    fun `a missing category sets an error and stores nothing`() = runTest(mainDispatcherRule.testDispatcher.scheduler) {
        val txRepo = FakeTransactionRepository()
        val viewModel = viewModel(txRepo, FakeCategoryRepository())
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.onIntent(AddTransactionIntent.AmountChanged("10"))
        viewModel.onIntent(AddTransactionIntent.Save)
        advanceUntilIdle()

        assertEquals(AddTransactionError.INVALID_CATEGORY, viewModel.uiState.value.error)
        assertTrue(txRepo.observeAll().first().isEmpty())
    }

    @Test
    fun `loading a transaction prefills the form in edit mode`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val existing = transaction(id = 7, amount = 4550, type = TransactionType.INCOME, note = "Sueldo")
            val txRepo = FakeTransactionRepository(listOf(existing))
            val viewModel = viewModel(txRepo, FakeCategoryRepository())
            backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.onIntent(AddTransactionIntent.Load(7))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.isEditing)
            assertEquals("45.50", state.amountInput)
            assertEquals(TransactionType.INCOME, state.type)
            assertEquals("Sueldo", state.note)
        }

    @Test
    fun `saving an edited transaction updates it in place without adding a new row`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val existing = transaction(id = 7, amount = 4550, category = category(id = 1, name = "Compras"))
            val txRepo = FakeTransactionRepository(listOf(existing))
            val viewModel = viewModel(txRepo, FakeCategoryRepository(listOf(category(id = 1, name = "Compras"))))
            backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.onIntent(AddTransactionIntent.Load(7))
            advanceUntilIdle()
            viewModel.onIntent(AddTransactionIntent.AmountChanged("99.00"))
            viewModel.onIntent(AddTransactionIntent.Save)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isSaved)
            val stored = txRepo.observeAll().first()
            assertEquals(1, stored.size)
            assertEquals(7L, stored.first().id)
            assertEquals(Money(9900), stored.first().amount)
        }

    @Test
    fun `creating a category from the picker selects it`() = runTest(mainDispatcherRule.testDispatcher.scheduler) {
        val catRepo = FakeCategoryRepository()
        val viewModel = viewModel(FakeTransactionRepository(), catRepo)
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.onIntent(AddTransactionIntent.CreateCategory("Comida"))
        advanceUntilIdle()

        assertEquals("Comida", viewModel.uiState.value.selectedCategory?.name)
        assertEquals(listOf("Comida"), catRepo.observeAll().first().map { it.name })
    }

    @Test
    fun `creating a duplicate category selects the existing one instead of adding a copy`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val catRepo = FakeCategoryRepository(listOf(category(id = 1, name = "Comida")))
            val viewModel = viewModel(FakeTransactionRepository(), catRepo)
            backgroundScope.launch { viewModel.uiState.collect {} }
            advanceUntilIdle()

            viewModel.onIntent(AddTransactionIntent.CreateCategory("comida"))
            advanceUntilIdle()

            assertEquals(1L, viewModel.uiState.value.selectedCategory?.id)
            assertEquals(listOf("Comida"), catRepo.observeAll().first().map { it.name })
        }

    @Test
    fun `creating a category with a blank name changes nothing`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val catRepo = FakeCategoryRepository()
            val viewModel = viewModel(FakeTransactionRepository(), catRepo)
            backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.onIntent(AddTransactionIntent.CreateCategory("   "))
            advanceUntilIdle()

            assertEquals(null, viewModel.uiState.value.selectedCategory)
            assertTrue(catRepo.observeAll().first().isEmpty())
        }
}

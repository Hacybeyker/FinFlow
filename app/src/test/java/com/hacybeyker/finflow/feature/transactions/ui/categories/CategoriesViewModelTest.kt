package com.hacybeyker.finflow.feature.transactions.ui.categories

import com.hacybeyker.finflow.core.test.MainDispatcherRule
import com.hacybeyker.finflow.feature.transactions.domain.FakeCategoryRepository
import com.hacybeyker.finflow.feature.transactions.domain.category
import com.hacybeyker.finflow.feature.transactions.domain.usecase.AddCategoryUseCase
import com.hacybeyker.finflow.feature.transactions.domain.usecase.DeleteCategoryUseCase
import com.hacybeyker.finflow.feature.transactions.domain.usecase.GetCategoriesUseCase
import com.hacybeyker.finflow.feature.transactions.domain.usecase.RenameCategoryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoriesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(repository: FakeCategoryRepository) = CategoriesViewModel(
        getCategories = GetCategoriesUseCase(repository),
        addCategory = AddCategoryUseCase(repository),
        renameCategory = RenameCategoryUseCase(repository),
        deleteCategory = DeleteCategoryUseCase(repository)
    )

    @Test
    fun `adds a category and closes the dialog`() = runTest(mainDispatcherRule.testDispatcher.scheduler) {
        val viewModel = viewModel(FakeCategoryRepository())
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.openAdd()
        viewModel.submitAdd("Comida")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("Comida"), state.categories.map { it.name })
        assertEquals(CategoryDialog.Hidden, state.dialog)
    }

    @Test
    fun `a duplicate name surfaces an error and keeps the dialog open`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val viewModel = viewModel(FakeCategoryRepository(listOf(category(name = "Comida"))))
            backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.openAdd()
            viewModel.submitAdd("comida")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(CategoryNameError.DUPLICATE, state.nameError)
            assertEquals(CategoryDialog.Add, state.dialog)
        }

    @Test
    fun `deletes a category`() = runTest(mainDispatcherRule.testDispatcher.scheduler) {
        val repository = FakeCategoryRepository(listOf(category(id = 1, name = "Comida")))
        val viewModel = viewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }

        viewModel.confirmDelete(category(id = 1, name = "Comida"))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.categories.isEmpty())
    }
}

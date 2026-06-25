package com.hacybeyker.finflow.feature.transactions.domain.usecase

import com.hacybeyker.finflow.feature.transactions.domain.FakeCategoryRepository
import com.hacybeyker.finflow.feature.transactions.domain.category
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddCategoryUseCaseTest {

    @Test
    fun `stores a trimmed category and returns it with its assigned id`() = runTest {
        val repository = FakeCategoryRepository()

        val result = AddCategoryUseCase(repository).invoke("  Comida  ")

        result as CategorySaveResult.Success
        assertEquals("Comida", result.category.name)
        assertEquals(listOf("Comida"), repository.observeAll().first().map { it.name })
    }

    @Test
    fun `rejects a blank name and stores nothing`() = runTest {
        val repository = FakeCategoryRepository()

        val result = AddCategoryUseCase(repository).invoke("   ")

        assertEquals(CategorySaveResult.InvalidName, result)
        assertTrue(repository.observeAll().first().isEmpty())
    }

    @Test
    fun `rejects a duplicate name ignoring case and stores nothing new`() = runTest {
        val repository = FakeCategoryRepository(listOf(category(name = "Comida")))

        val result = AddCategoryUseCase(repository).invoke("comida")

        assertEquals(CategorySaveResult.Duplicate, result)
        assertEquals(1, repository.observeAll().first().size)
    }
}

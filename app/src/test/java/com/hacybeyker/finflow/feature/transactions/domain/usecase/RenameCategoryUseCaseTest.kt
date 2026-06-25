package com.hacybeyker.finflow.feature.transactions.domain.usecase

import com.hacybeyker.finflow.feature.transactions.domain.FakeCategoryRepository
import com.hacybeyker.finflow.feature.transactions.domain.category
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RenameCategoryUseCaseTest {

    @Test
    fun `renames a category to a trimmed name`() = runTest {
        val target = category(id = 1, name = "Comida")
        val repository = FakeCategoryRepository(listOf(target))

        val result = RenameCategoryUseCase(repository).invoke(target, "  Alimentación  ")

        assertEquals(CategorySaveResult.Success(target.copy(name = "Alimentación")), result)
        assertEquals(listOf("Alimentación"), repository.observeAll().first().map { it.name })
    }

    @Test
    fun `rejects a blank new name`() = runTest {
        val target = category(id = 1, name = "Comida")
        val repository = FakeCategoryRepository(listOf(target))

        val result = RenameCategoryUseCase(repository).invoke(target, "  ")

        assertEquals(CategorySaveResult.InvalidName, result)
        assertEquals("Comida", repository.observeAll().first().single().name)
    }

    @Test
    fun `rejects renaming onto another existing name but allows keeping its own`() = runTest {
        val comida = category(id = 1, name = "Comida")
        val ocio = category(id = 2, name = "Ocio")
        val repository = FakeCategoryRepository(listOf(comida, ocio))
        val useCase = RenameCategoryUseCase(repository)

        assertEquals(CategorySaveResult.Duplicate, useCase.invoke(comida, "ocio"))
        assertEquals(
            CategorySaveResult.Success(comida.copy(name = "Comida")),
            useCase.invoke(comida, "Comida")
        )
    }
}

package com.hacybeyker.finflow.feature.transactions.domain.usecase

import com.hacybeyker.finflow.feature.transactions.domain.FakeTransactionRepository
import com.hacybeyker.finflow.feature.transactions.domain.transaction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddTransactionUseCaseTest {

    @Test
    fun `stores a transaction with a positive amount`() = runTest {
        val repository = FakeTransactionRepository()

        val result = AddTransactionUseCase(repository).invoke(transaction(amount = 500))

        assertEquals(AddTransactionResult.Success, result)
        assertEquals(1, repository.observeAll().first().size)
    }

    @Test
    fun `rejects a non-positive amount and stores nothing`() = runTest {
        val repository = FakeTransactionRepository()

        val result = AddTransactionUseCase(repository).invoke(transaction(amount = 0))

        assertEquals(AddTransactionResult.InvalidAmount, result)
        assertTrue(repository.observeAll().first().isEmpty())
    }
}

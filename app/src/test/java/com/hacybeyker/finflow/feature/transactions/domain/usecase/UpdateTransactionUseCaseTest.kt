package com.hacybeyker.finflow.feature.transactions.domain.usecase

import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.feature.transactions.domain.FakeTransactionRepository
import com.hacybeyker.finflow.feature.transactions.domain.transaction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateTransactionUseCaseTest {

    @Test
    fun `updates an existing transaction in place`() = runTest {
        val repository = FakeTransactionRepository(listOf(transaction(id = 1, amount = 500)))

        val result = UpdateTransactionUseCase(repository).invoke(transaction(id = 1, amount = 900))

        assertEquals(TransactionWriteResult.Success, result)
        assertEquals(Money(900), repository.observeAll().first().single().amount)
    }

    @Test
    fun `rejects a non-positive amount and leaves the stored transaction untouched`() = runTest {
        val repository = FakeTransactionRepository(listOf(transaction(id = 1, amount = 500)))

        val result = UpdateTransactionUseCase(repository).invoke(transaction(id = 1, amount = 0))

        assertEquals(TransactionWriteResult.InvalidAmount, result)
        assertEquals(Money(500), repository.observeAll().first().single().amount)
    }
}

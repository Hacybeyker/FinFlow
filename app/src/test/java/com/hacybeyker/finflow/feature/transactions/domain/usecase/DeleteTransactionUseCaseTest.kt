package com.hacybeyker.finflow.feature.transactions.domain.usecase

import com.hacybeyker.finflow.feature.transactions.domain.FakeTransactionRepository
import com.hacybeyker.finflow.feature.transactions.domain.transaction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DeleteTransactionUseCaseTest {

    @Test
    fun `removes only the targeted transaction`() = runTest {
        val repository = FakeTransactionRepository(
            listOf(transaction(id = 1), transaction(id = 2))
        )

        DeleteTransactionUseCase(repository).invoke(transaction(id = 1))

        assertEquals(listOf(2L), repository.observeAll().first().map { it.id })
    }
}

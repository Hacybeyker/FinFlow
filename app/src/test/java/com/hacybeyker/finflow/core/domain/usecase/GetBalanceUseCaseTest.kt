package com.hacybeyker.finflow.core.domain.usecase

import com.hacybeyker.finflow.core.domain.FakeTransactionRepository
import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.domain.TransactionType
import com.hacybeyker.finflow.core.domain.transaction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetBalanceUseCaseTest {

    @Test
    fun `balance adds income and subtracts expenses`() = runTest {
        val repository = FakeTransactionRepository(
            listOf(
                transaction(amount = 1000, type = TransactionType.INCOME),
                transaction(amount = 300, type = TransactionType.EXPENSE),
                transaction(amount = 200, type = TransactionType.EXPENSE)
            )
        )

        val balance = GetBalanceUseCase(repository).invoke().first()

        assertEquals(Money(500), balance)
    }

    @Test
    fun `balance is zero when there are no transactions`() = runTest {
        val balance = GetBalanceUseCase(FakeTransactionRepository()).invoke().first()

        assertEquals(Money.ZERO, balance)
    }
}

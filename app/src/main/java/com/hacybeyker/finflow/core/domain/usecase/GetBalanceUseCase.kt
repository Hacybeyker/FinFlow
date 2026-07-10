package com.hacybeyker.finflow.core.domain.usecase

import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.domain.TransactionRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetBalanceUseCase @Inject constructor(private val repository: TransactionRepository) {

    operator fun invoke(): Flow<Money> = repository.observeAll().map { transactions ->
        transactions.fold(Money.ZERO) { acc, transaction -> acc + transaction.signedAmount() }
    }
}

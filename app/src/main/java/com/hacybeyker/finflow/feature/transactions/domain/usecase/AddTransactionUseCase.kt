package com.hacybeyker.finflow.feature.transactions.domain.usecase

import com.hacybeyker.finflow.core.domain.Transaction
import com.hacybeyker.finflow.core.domain.TransactionRepository
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(private val repository: TransactionRepository) {

    suspend operator fun invoke(transaction: Transaction): TransactionWriteResult {
        if (!transaction.amount.isPositive) return TransactionWriteResult.InvalidAmount
        repository.add(transaction)
        return TransactionWriteResult.Success
    }
}

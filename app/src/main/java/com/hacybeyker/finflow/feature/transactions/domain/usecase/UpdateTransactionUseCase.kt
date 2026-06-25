package com.hacybeyker.finflow.feature.transactions.domain.usecase

import com.hacybeyker.finflow.feature.transactions.domain.Transaction
import com.hacybeyker.finflow.feature.transactions.domain.TransactionRepository
import javax.inject.Inject

class UpdateTransactionUseCase @Inject constructor(private val repository: TransactionRepository) {

    suspend operator fun invoke(transaction: Transaction): TransactionWriteResult {
        if (!transaction.amount.isPositive) return TransactionWriteResult.InvalidAmount
        repository.update(transaction)
        return TransactionWriteResult.Success
    }
}

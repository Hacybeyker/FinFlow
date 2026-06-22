package com.hacybeyker.finflow.feature.transactions.domain.usecase

import com.hacybeyker.finflow.feature.transactions.domain.Transaction
import com.hacybeyker.finflow.feature.transactions.domain.TransactionRepository
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(private val repository: TransactionRepository) {

    suspend operator fun invoke(transaction: Transaction): AddTransactionResult {
        if (!transaction.amount.isPositive) return AddTransactionResult.InvalidAmount
        repository.add(transaction)
        return AddTransactionResult.Success
    }
}

sealed interface AddTransactionResult {
    data object Success : AddTransactionResult
    data object InvalidAmount : AddTransactionResult
}

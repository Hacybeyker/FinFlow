package com.hacybeyker.finflow.feature.transactions.domain.usecase

import com.hacybeyker.finflow.feature.transactions.domain.Transaction
import com.hacybeyker.finflow.feature.transactions.domain.TransactionRepository
import javax.inject.Inject

/**
 * Validates and stores a transaction. Validation lives here (not in the UI) so it can be unit tested
 * and reused. Returns an explicit [AddTransactionResult] instead of throwing.
 */
class AddTransaction @Inject constructor(private val repository: TransactionRepository) {

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

package com.hacybeyker.finflow.feature.transactions.domain.usecase

import com.hacybeyker.finflow.feature.transactions.domain.Transaction
import com.hacybeyker.finflow.feature.transactions.domain.TransactionRepository
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(private val repository: TransactionRepository) {

    suspend operator fun invoke(transaction: Transaction) = repository.delete(transaction)
}

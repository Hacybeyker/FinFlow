package com.hacybeyker.finflow.feature.transactions.domain.usecase

import com.hacybeyker.finflow.feature.transactions.domain.Transaction
import com.hacybeyker.finflow.feature.transactions.domain.TransactionRepository
import javax.inject.Inject

class GetTransactionByIdUseCase @Inject constructor(private val repository: TransactionRepository) {

    suspend operator fun invoke(id: Long): Transaction? = repository.getById(id)
}

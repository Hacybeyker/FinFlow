package com.hacybeyker.finflow.feature.transactions.domain.usecase

import com.hacybeyker.finflow.core.domain.Transaction
import com.hacybeyker.finflow.core.domain.TransactionRepository
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetTransactionsByMonthUseCase @Inject constructor(private val repository: TransactionRepository) {

    operator fun invoke(month: YearMonth): Flow<List<Transaction>> = repository.observeByMonth(month)
}

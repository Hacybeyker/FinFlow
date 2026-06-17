package com.hacybeyker.finflow.feature.transactions.domain.usecase

import com.hacybeyker.finflow.feature.transactions.domain.Transaction
import com.hacybeyker.finflow.feature.transactions.domain.TransactionRepository
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/** Reactive list of transactions for a given month. Ordering is left to the data layer. */
class GetTransactionsByMonth @Inject constructor(private val repository: TransactionRepository) {

    operator fun invoke(month: YearMonth): Flow<List<Transaction>> = repository.observeByMonth(month)
}

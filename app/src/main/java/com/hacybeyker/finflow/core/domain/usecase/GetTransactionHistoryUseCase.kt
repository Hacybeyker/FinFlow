package com.hacybeyker.finflow.core.domain.usecase

import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.domain.Transaction
import com.hacybeyker.finflow.core.domain.TransactionMonth
import com.hacybeyker.finflow.core.domain.TransactionRepository
import com.hacybeyker.finflow.core.domain.TransactionType
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The full transaction history grouped by month, newest month (and newest transaction) first, with
 * each month's net total for its section header. Ties on the same date break by id descending, so
 * the latest insert shows on top.
 */
class GetTransactionHistoryUseCase @Inject constructor(private val repository: TransactionRepository) {

    operator fun invoke(): Flow<List<TransactionMonth>> = repository.observeAll().map { transactions ->
        transactions
            .sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.id })
            .groupBy { YearMonth.from(it.date) }
            .map { (month, monthTransactions) ->
                val income = monthTransactions.sumAmountOf(TransactionType.INCOME)
                val expense = monthTransactions.sumAmountOf(TransactionType.EXPENSE)
                TransactionMonth(
                    month = month,
                    transactions = monthTransactions,
                    income = income,
                    expense = expense,
                    total = income - expense
                )
            }
    }
}

private fun List<Transaction>.sumAmountOf(type: TransactionType): Money =
    filter { it.type == type }.fold(Money.ZERO) { acc, t -> acc + t.amount }

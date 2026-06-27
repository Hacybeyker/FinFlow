package com.hacybeyker.finflow.feature.charts.domain.usecase

import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.domain.Transaction
import com.hacybeyker.finflow.core.domain.TransactionRepository
import com.hacybeyker.finflow.core.domain.TransactionType
import com.hacybeyker.finflow.feature.charts.domain.MonthlyTotal
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Income/expense totals for the [monthCount] months ending at [until] (inclusive), in chronological
 * order. Months with no movements are emitted as zero so the bar chart keeps a stable x-axis.
 */
class GetMonthlyTotalsUseCase @Inject constructor(private val repository: TransactionRepository) {

    operator fun invoke(monthCount: Int, until: YearMonth): Flow<List<MonthlyTotal>> =
        repository.observeAll().map { transactions ->
            val byMonth = transactions.groupBy { YearMonth.from(it.date) }
            (0 until monthCount).map { offset ->
                val month = until.minusMonths((monthCount - 1 - offset).toLong())
                val txs = byMonth[month].orEmpty()
                MonthlyTotal(
                    month = month,
                    income = txs.sumAmountOf(TransactionType.INCOME),
                    expense = txs.sumAmountOf(TransactionType.EXPENSE)
                )
            }
        }
}

private fun List<Transaction>.sumAmountOf(type: TransactionType): Money =
    filter { it.type == type }.fold(Money.ZERO) { acc, t -> acc + t.amount }

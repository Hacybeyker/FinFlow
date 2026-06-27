package com.hacybeyker.finflow.feature.charts.domain.usecase

import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.domain.TransactionRepository
import com.hacybeyker.finflow.core.domain.TransactionType
import com.hacybeyker.finflow.feature.charts.domain.CategorySpending
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Expenses of [month] grouped by category and summed, sorted by amount descending — the donut's
 * slices. Income is ignored (the donut answers "where did the money go"); categories with no expense
 * simply don't appear.
 */
class GetSpendingByCategoryUseCase @Inject constructor(private val repository: TransactionRepository) {

    operator fun invoke(month: YearMonth): Flow<List<CategorySpending>> =
        repository.observeByMonth(month).map { transactions ->
            transactions
                .filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.category }
                .map { (category, txs) ->
                    CategorySpending(category, txs.fold(Money.ZERO) { acc, t -> acc + t.amount })
                }
                .sortedByDescending { it.total }
        }
}

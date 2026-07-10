package com.hacybeyker.finflow.feature.widget.domain.usecase

import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.domain.currentMonth
import com.hacybeyker.finflow.core.domain.usecase.GetBalanceUseCase
import com.hacybeyker.finflow.core.domain.usecase.GetTransactionHistoryUseCase
import com.hacybeyker.finflow.feature.widget.domain.WidgetSummary
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** How many recent movements the widget shows at its tall size. */
private const val LATEST_COUNT = 3

/**
 * Composes the widget's snapshot from the `transactions` domain contracts — no new queries: the
 * balance is the existing all-time fold and the month aggregates come from the grouped history
 * (already newest-first, so the latest movements are just the head of the flattened list).
 */
class GetWidgetSummaryUseCase @Inject constructor(
    private val getBalance: GetBalanceUseCase,
    private val getTransactionHistory: GetTransactionHistoryUseCase,
    private val clock: Clock
) {

    operator fun invoke(): Flow<WidgetSummary> = combine(getBalance(), getTransactionHistory()) { balance, months ->
        val currentMonth = months.currentMonth(clock)
        WidgetSummary(
            balance = balance,
            monthIncome = currentMonth?.income ?: Money.ZERO,
            monthExpense = currentMonth?.expense ?: Money.ZERO,
            latest = months.flatMap { it.transactions }.take(LATEST_COUNT)
        )
    }
}

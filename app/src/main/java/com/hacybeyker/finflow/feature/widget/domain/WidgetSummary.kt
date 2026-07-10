package com.hacybeyker.finflow.feature.widget.domain

import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.domain.Transaction

/**
 * Snapshot the home-screen widget renders: all-time [balance], the current calendar month's gross
 * [monthIncome]/[monthExpense] (zero when the month has no movements) and the [latest] transactions
 * across all months, newest first.
 */
data class WidgetSummary(
    val balance: Money,
    val monthIncome: Money,
    val monthExpense: Money,
    val latest: List<Transaction>
)

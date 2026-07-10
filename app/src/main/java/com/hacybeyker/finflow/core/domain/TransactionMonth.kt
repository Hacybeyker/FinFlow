package com.hacybeyker.finflow.core.domain

import java.time.Clock
import java.time.YearMonth

/**
 * One month of history: its transactions (newest first) plus the month's aggregates — gross
 * [income], gross [expense] (both positive) and the net [total] (income − expense) — ready for
 * section headers and the hero month summary.
 */
data class TransactionMonth(
    val month: YearMonth,
    val transactions: List<Transaction>,
    val income: Money,
    val expense: Money,
    val total: Money
)

/** The entry for the current calendar month, or `null` if it has no movements yet. */
fun List<TransactionMonth>.currentMonth(clock: Clock): TransactionMonth? =
    firstOrNull { it.month == YearMonth.now(clock) }

package com.hacybeyker.finflow.feature.transactions.domain

import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.domain.Transaction
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

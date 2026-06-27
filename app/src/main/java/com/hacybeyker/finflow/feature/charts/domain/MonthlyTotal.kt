package com.hacybeyker.finflow.feature.charts.domain

import com.hacybeyker.finflow.core.domain.Money
import java.time.YearMonth

/** Income and expense totals for a single month — a column of the bar chart. */
data class MonthlyTotal(val month: YearMonth, val income: Money, val expense: Money)

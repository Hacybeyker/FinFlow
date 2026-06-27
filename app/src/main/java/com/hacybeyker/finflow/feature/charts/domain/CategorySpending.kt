package com.hacybeyker.finflow.feature.charts.domain

import com.hacybeyker.finflow.core.domain.Category
import com.hacybeyker.finflow.core.domain.Money

/** Total expense for one category over a period — a slice of the donut chart. */
data class CategorySpending(val category: Category, val total: Money)

package com.hacybeyker.finflow.feature.charts.ui

import com.hacybeyker.finflow.feature.charts.domain.CategorySpending
import com.hacybeyker.finflow.feature.charts.domain.MonthlyTotal

sealed interface ChartsUiState {
    data object Loading : ChartsUiState

    /**
     * [spending] is the current month's expenses by category (donut); [monthlyTotals] is the trailing
     * window of income/expense columns (bar chart). [spending] can be empty (no expenses this month)
     * while [monthlyTotals] still has data, so the screen decides per chart whether to show an empty
     * state.
     */
    data class Content(val spending: List<CategorySpending>, val monthlyTotals: List<MonthlyTotal>) : ChartsUiState
}

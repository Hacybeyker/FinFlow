package com.hacybeyker.finflow.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Domain-specific semantic colors that Material 3 does not provide. Income and expense need a
 * stable, brand-defined meaning across the whole app (list, detail, charts, widget), so they live
 * here instead of being reinvented per screen.
 *
 * Usage: `MaterialTheme.financeColors.income`. Never hardcode a green/red in a Composable.
 */
@Immutable
data class FinanceColors(
    val income: Color,
    val onIncome: Color,
    val incomeContainer: Color,
    val onIncomeContainer: Color,
    val expense: Color,
    val onExpense: Color,
    val expenseContainer: Color,
    val onExpenseContainer: Color
)

val LightFinanceColors = FinanceColors(
    income = IncomeLight,
    onIncome = OnIncomeLight,
    incomeContainer = IncomeContainerLight,
    onIncomeContainer = OnIncomeContainerLight,
    expense = ExpenseLight,
    onExpense = OnExpenseLight,
    expenseContainer = ExpenseContainerLight,
    onExpenseContainer = OnExpenseContainerLight
)

val DarkFinanceColors = FinanceColors(
    income = IncomeDark,
    onIncome = OnIncomeDark,
    incomeContainer = IncomeContainerDark,
    onIncomeContainer = OnIncomeContainerDark,
    expense = ExpenseDark,
    onExpense = OnExpenseDark,
    expenseContainer = ExpenseContainerDark,
    onExpenseContainer = OnExpenseContainerDark
)

/**
 * Fails loud (Unspecified) if a Composable reads finance colors outside [FinFlowTheme]; that surfaces
 * the missing provider instead of silently rendering a wrong color.
 */
val LocalFinanceColors = staticCompositionLocalOf {
    FinanceColors(
        income = Color.Unspecified,
        onIncome = Color.Unspecified,
        incomeContainer = Color.Unspecified,
        onIncomeContainer = Color.Unspecified,
        expense = Color.Unspecified,
        onExpense = Color.Unspecified,
        expenseContainer = Color.Unspecified,
        onExpenseContainer = Color.Unspecified
    )
}

/** Ergonomic accessor so call sites read `MaterialTheme.financeColors`. */
val MaterialTheme.financeColors: FinanceColors
    @Composable
    @ReadOnlyComposable
    get() = LocalFinanceColors.current

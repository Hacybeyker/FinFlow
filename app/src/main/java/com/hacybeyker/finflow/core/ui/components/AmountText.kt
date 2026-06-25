package com.hacybeyker.finflow.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.ui.format.LocalMoneyFormatter
import com.hacybeyker.finflow.core.ui.theme.financeColors

/**
 * Renders a [Money] amount with its finance semantics in one place: income green, expense coral,
 * neutral otherwise. With [showSign] it prefixes a `+` for positive amounts (NumberFormat already
 * adds `-` for negatives), so meaning never relies on color alone (accessibility).
 *
 * Screens use this instead of branching on color or formatting money themselves.
 */
@Composable
fun AmountText(
    money: Money,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    showSign: Boolean = true
) {
    val color = when {
        money.isPositive -> MaterialTheme.financeColors.income
        money.isNegative -> MaterialTheme.financeColors.expense
        else -> MaterialTheme.colorScheme.onSurface
    }
    val formatted = LocalMoneyFormatter.current.format(money)
    Text(
        text = if (showSign && money.isPositive) "+$formatted" else formatted,
        color = color,
        style = style,
        modifier = modifier
    )
}

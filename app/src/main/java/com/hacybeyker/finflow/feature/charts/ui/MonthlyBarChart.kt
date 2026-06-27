package com.hacybeyker.finflow.feature.charts.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hacybeyker.finflow.R
import com.hacybeyker.finflow.core.ui.theme.financeColors
import com.hacybeyker.finflow.core.ui.theme.spacing
import com.hacybeyker.finflow.feature.charts.domain.MonthlyTotal
import java.time.format.TextStyle
import java.util.Locale

/**
 * Grouped bars of income vs expense per month, oldest to newest. Heights are normalized to the single
 * largest value across the whole window so months are comparable; income uses the green and expense
 * the coral [financeColors] semantics, reinforced by the legend so meaning never rests on color alone.
 */
@Composable
fun MonthlyBarChart(monthlyTotals: List<MonthlyTotal>, modifier: Modifier = Modifier) {
    val maxValue = monthlyTotals
        .flatMap { listOf(it.income.minorUnits, it.expense.minorUnits) }
        .maxOrNull()
        ?.coerceAtLeast(1L) ?: 1L

    Column(modifier = modifier) {
        Legend(modifier = Modifier.padding(bottom = MaterialTheme.spacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)
        ) {
            monthlyTotals.forEach { total ->
                MonthColumn(total = total, maxValue = maxValue, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MonthColumn(total: MonthlyTotal, maxValue: Long, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.height(PLOT_HEIGHT.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(BAR_GAP.dp)
        ) {
            Bar(fraction = total.income.minorUnits.toFloat() / maxValue, color = MaterialTheme.financeColors.income)
            Bar(fraction = total.expense.minorUnits.toFloat() / maxValue, color = MaterialTheme.financeColors.expense)
        }
        Text(
            text = total.month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = MaterialTheme.spacing.xs)
        )
    }
}

@Composable
private fun Bar(fraction: Float, color: Color) {
    Box(
        modifier = Modifier
            .width(BAR_WIDTH.dp)
            .fillMaxHeight(fraction.coerceIn(0f, 1f))
            .clip(RoundedCornerShape(topStart = BAR_CORNER.dp, topEnd = BAR_CORNER.dp))
            .background(color)
    )
}

@Composable
private fun Legend(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
    ) {
        LegendItem(color = MaterialTheme.financeColors.income, label = stringResource(R.string.charts_legend_income))
        LegendItem(color = MaterialTheme.financeColors.expense, label = stringResource(R.string.charts_legend_expense))
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(SWATCH_SIZE.dp)
                .clip(RoundedCornerShape(SWATCH_CORNER.dp))
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(start = MaterialTheme.spacing.xs)
        )
    }
}

private const val PLOT_HEIGHT = 160
private const val BAR_WIDTH = 12
private const val BAR_GAP = 3
private const val BAR_CORNER = 4
private const val SWATCH_SIZE = 12
private const val SWATCH_CORNER = 3

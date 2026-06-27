package com.hacybeyker.finflow.feature.charts.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hacybeyker.finflow.R
import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.ui.components.AmountText
import com.hacybeyker.finflow.core.ui.theme.chartColors
import com.hacybeyker.finflow.core.ui.theme.spacing
import com.hacybeyker.finflow.feature.charts.domain.CategorySpending

/**
 * Donut of this month's expenses by category, with a legend below. Slices are sized by share of the
 * total and colored from the categorical [chartColors] palette (cycled by index, matching the
 * legend). The hole shows the period's total spend so the chart reads at a glance.
 */
@Composable
fun SpendingDonut(spending: List<CategorySpending>, modifier: Modifier = Modifier) {
    val palette = MaterialTheme.chartColors
    val sliceColors = spending.mapIndexed { index, _ -> palette.colorAt(index) }
    val total = spending.fold(Money.ZERO) { acc, slice -> acc + slice.total }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            DonutRing(
                values = spending.map { it.total.minorUnits },
                colors = sliceColors,
                modifier = Modifier
                    .fillMaxWidth(fraction = DONUT_WIDTH_FRACTION)
                    .aspectRatio(1f)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.charts_total_spent),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AmountText(money = total, style = MaterialTheme.typography.titleLarge, showSign = false)
            }
        }
        Legend(
            spending = spending,
            colors = sliceColors,
            modifier = Modifier.padding(top = MaterialTheme.spacing.lg)
        )
    }
}

@Composable
private fun DonutRing(values: List<Long>, colors: List<Color>, modifier: Modifier = Modifier) {
    val total = values.sum().coerceAtLeast(1L)
    // Reveal the ring clockwise on first show / when the data changes: a single 0→360° budget that we
    // hand out to the slices in order, so the animation is one sweep, not each slice racing on its own.
    val sweepProgress = remember(values) { Animatable(0f) }
    LaunchedEffect(values) { sweepProgress.animateTo(FULL_CIRCLE, tween(SWEEP_DURATION_MILLIS)) }

    Canvas(modifier = modifier) {
        val thickness = size.minDimension * RING_THICKNESS_FRACTION
        val diameter = size.minDimension - thickness
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)
        val budget = sweepProgress.value
        var cumulative = 0f
        values.forEachIndexed { index, value ->
            val slice = FULL_CIRCLE * (value.toFloat() / total)
            val drawn = (budget - cumulative).coerceIn(0f, slice)
            if (drawn > 0f) {
                drawArc(
                    color = colors[index],
                    startAngle = START_ANGLE + cumulative,
                    sweepAngle = (drawn - SLICE_GAP_DEGREES).coerceAtLeast(0f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = thickness)
                )
            }
            cumulative += slice
        }
    }
}

@Composable
private fun Legend(spending: List<CategorySpending>, colors: List<Color>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
        spending.forEachIndexed { index, slice ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(SWATCH_SIZE.dp)
                        .clip(CircleShape)
                        .background(colors[index])
                )
                Text(
                    text = slice.category.name,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = MaterialTheme.spacing.sm)
                )
                AmountText(money = slice.total, style = MaterialTheme.typography.bodyMedium, showSign = false)
            }
        }
    }
}

private const val DONUT_WIDTH_FRACTION = 0.62f
private const val RING_THICKNESS_FRACTION = 0.16f
private const val START_ANGLE = -90f
private const val FULL_CIRCLE = 360f

/** A hair of background between slices so adjacent colors don't blur together. */
private const val SLICE_GAP_DEGREES = 2f
private const val SWEEP_DURATION_MILLIS = 700
private const val SWATCH_SIZE = 12

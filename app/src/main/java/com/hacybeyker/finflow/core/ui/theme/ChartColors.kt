package com.hacybeyker.finflow.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Qualitative palette for category breakdowns (the donut chart). Material 3 has no categorical scale,
 * so slice colors live here as brand tokens instead of being hardcoded per chart. The list is theme
 * aware (light/dark) and cycled by slice index; use [colorAt] so wrapping past the end is handled in
 * one place.
 *
 * Usage: `MaterialTheme.chartColors.colorAt(index)`. Never hardcode a slice color in a Composable.
 */
@Immutable
data class ChartColors(val slices: List<Color>) {
    fun colorAt(index: Int): Color = slices[index % slices.size]
}

val LightChartColors = ChartColors(ChartSliceLight)
val DarkChartColors = ChartColors(ChartSliceDark)

/** Fails loud (empty) if read outside [FinFlowTheme], surfacing the missing provider. */
val LocalChartColors = staticCompositionLocalOf { ChartColors(emptyList()) }

val MaterialTheme.chartColors: ChartColors
    @Composable
    @ReadOnlyComposable
    get() = LocalChartColors.current

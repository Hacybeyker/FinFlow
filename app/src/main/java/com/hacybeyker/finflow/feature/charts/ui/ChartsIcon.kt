package com.hacybeyker.finflow.feature.charts.ui

import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * A bar-chart glyph for the toolbar. Hand-built because the project depends only on
 * `material-icons-core` (no `Assessment`/`BarChart` there), and pulling in the extended icon set just
 * for this one shape isn't worth the size. Three rising bars on a 24dp grid; tinted by the caller.
 */
val ChartsBarIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "ChartsBar",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        val fill = SolidColor(androidx.compose.ui.graphics.Color.Black)
        path(fill = fill) { rect(x = 4f, y = 13f, w = 4f, h = 7f) }
        path(fill = fill) { rect(x = 10f, y = 9f, w = 4f, h = 11f) }
        path(fill = fill) { rect(x = 16f, y = 4f, w = 4f, h = 16f) }
    }.build()
}

private fun androidx.compose.ui.graphics.vector.PathBuilder.rect(x: Float, y: Float, w: Float, h: Float) {
    moveTo(x, y)
    horizontalLineToRelative(w)
    verticalLineToRelative(h)
    horizontalLineToRelative(-w)
    close()
}

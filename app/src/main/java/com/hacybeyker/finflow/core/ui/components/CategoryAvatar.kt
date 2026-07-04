package com.hacybeyker.finflow.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.hacybeyker.finflow.core.domain.Category
import com.hacybeyker.finflow.core.ui.theme.chartColors
import com.hacybeyker.finflow.core.ui.theme.spacing

/** Tint strength of the category accent used as avatar background. */
private const val AVATAR_TINT_ALPHA = 0.16f

/**
 * Colorful anchor for a category: its initial over its accent color (a `chartColors` slice keyed by
 * category id, so the color is stable everywhere the category appears). Decorative by design — the
 * category name must always sit next to it, never rely on the avatar alone.
 */
@Composable
fun CategoryAvatar(category: Category, modifier: Modifier = Modifier) {
    val accent = MaterialTheme.chartColors.colorAt(category.id.toInt())
    Box(
        modifier = modifier
            .size(MaterialTheme.spacing.xxl)
            .clip(CircleShape)
            .background(accent.copy(alpha = AVATAR_TINT_ALPHA)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = category.name.take(1).uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = accent
        )
    }
}

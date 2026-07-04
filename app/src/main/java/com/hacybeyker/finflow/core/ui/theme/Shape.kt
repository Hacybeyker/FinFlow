package com.hacybeyker.finflow.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Corner radius scale. Generous, confident rounding is part of the brand signature: cards and hero
 * surfaces read soft and friendly, never sharp. Consume via `MaterialTheme.shapes.*` (e.g. list
 * rows/cards use `medium`, the hero balance and dialogs use `large`).
 */
val FinFlowShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

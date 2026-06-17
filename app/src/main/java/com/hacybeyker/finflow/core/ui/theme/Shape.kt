package com.hacybeyker.finflow.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Corner radius scale. Soft, generous rounding reinforces the calm, friendly feel. Consume via
 * `MaterialTheme.shapes.*` (e.g. cards use `medium`, dialogs/sheets use `large`).
 */
val FinFlowShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

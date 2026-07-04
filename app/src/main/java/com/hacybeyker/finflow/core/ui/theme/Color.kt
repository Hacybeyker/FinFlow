package com.hacybeyker.finflow.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Raw color tokens for FinFlow. These are the SINGLE SOURCE OF TRUTH at runtime and must stay in
 * sync with the values documented in DESIGN.md (repo root). Never use raw `Color(0x...)` in screens
 * or components — always consume them through `MaterialTheme.colorScheme` / `financeColors`.
 *
 * Palette: an electric violet brand with a fresh teal accent over violet-tinted neutrals, plus
 * dedicated income/expense semantics. Bold where it matters (hero surfaces, CTAs), quiet elsewhere.
 */

// region Brand · Light
val PrimaryLight = Color(0xFF6C3DF4)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFE9DDFF)
val OnPrimaryContainerLight = Color(0xFF22005D)

val SecondaryLight = Color(0xFF5F5A7D)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFE5DFF9)
val OnSecondaryContainerLight = Color(0xFF1B1736)

val TertiaryLight = Color(0xFF00696B)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFF9CF1F2)
val OnTertiaryContainerLight = Color(0xFF002021)

val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

val BackgroundLight = Color(0xFFFBF8FF)
val OnBackgroundLight = Color(0xFF1C1B22)
val SurfaceLight = Color(0xFFFBF8FF)
val OnSurfaceLight = Color(0xFF1C1B22)
val SurfaceVariantLight = Color(0xFFE6E0F2)
val OnSurfaceVariantLight = Color(0xFF48454F)
val OutlineLight = Color(0xFF7A7684)
val OutlineVariantLight = Color(0xFFCBC4D8)

// Tonal surface ladder: violet-tinted elevation steps so cards/sheets separate from the background
// without shadows (surfaceContainerLow for list rows, surfaceContainerHigh for sheets/menus).
val SurfaceDimLight = Color(0xFFDBD7E4)
val SurfaceBrightLight = Color(0xFFFBF8FF)
val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF5F1FC)
val SurfaceContainerLight = Color(0xFFEFEBF7)
val SurfaceContainerHighLight = Color(0xFFE9E5F1)
val SurfaceContainerHighestLight = Color(0xFFE4DFEB)
val InverseSurfaceLight = Color(0xFF312F38)
val InverseOnSurfaceLight = Color(0xFFF3EFFA)
val InversePrimaryLight = Color(0xFFCFBDFF)
// endregion

// region Brand · Dark
val PrimaryDark = Color(0xFFCFBDFF)
val OnPrimaryDark = Color(0xFF3813A0)
val PrimaryContainerDark = Color(0xFF5224C4)
val OnPrimaryContainerDark = Color(0xFFE9DDFF)

val SecondaryDark = Color(0xFFC9C2E8)
val OnSecondaryDark = Color(0xFF312C4C)
val SecondaryContainerDark = Color(0xFF474263)
val OnSecondaryContainerDark = Color(0xFFE5DFF9)

val TertiaryDark = Color(0xFF80D4D6)
val OnTertiaryDark = Color(0xFF003738)
val TertiaryContainerDark = Color(0xFF004F51)
val OnTertiaryContainerDark = Color(0xFF9CF1F2)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

val BackgroundDark = Color(0xFF14121A)
val OnBackgroundDark = Color(0xFFE6E1EB)
val SurfaceDark = Color(0xFF14121A)
val OnSurfaceDark = Color(0xFFE6E1EB)
val SurfaceVariantDark = Color(0xFF48454F)
val OnSurfaceVariantDark = Color(0xFFCBC4D8)
val OutlineDark = Color(0xFF948F9E)
val OutlineVariantDark = Color(0xFF48454F)

val SurfaceDimDark = Color(0xFF14121A)
val SurfaceBrightDark = Color(0xFF3B3841)
val SurfaceContainerLowestDark = Color(0xFF0F0D15)
val SurfaceContainerLowDark = Color(0xFF1C1A22)
val SurfaceContainerDark = Color(0xFF201E27)
val SurfaceContainerHighDark = Color(0xFF2B2831)
val SurfaceContainerHighestDark = Color(0xFF36333C)
val InverseSurfaceDark = Color(0xFFE6E1EB)
val InverseOnSurfaceDark = Color(0xFF312F38)
val InversePrimaryDark = Color(0xFF6C3DF4)
// endregion

// region Semantic · Income (emerald) · Expense (coral)
// These are brand-defined on purpose and never derived from dynamic color, so a positive amount is
// always green and a negative one always coral, regardless of the user's wallpaper.
val IncomeLight = Color(0xFF2E6B4F)
val OnIncomeLight = Color(0xFFFFFFFF)
val IncomeContainerLight = Color(0xFFB4F1CC)
val OnIncomeContainerLight = Color(0xFF002113)
val ExpenseLight = Color(0xFFB3403E)
val OnExpenseLight = Color(0xFFFFFFFF)
val ExpenseContainerLight = Color(0xFFFFDAD6)
val OnExpenseContainerLight = Color(0xFF410006)

val IncomeDark = Color(0xFF98D8B1)
val OnIncomeDark = Color(0xFF003824)
val IncomeContainerDark = Color(0xFF135137)
val OnIncomeContainerDark = Color(0xFFB4F1CC)
val ExpenseDark = Color(0xFFFFB3AD)
val OnExpenseDark = Color(0xFF5F1414)
val ExpenseContainerDark = Color(0xFF8C2A29)
val OnExpenseContainerDark = Color(0xFFFFDAD6)
// endregion

// region Categorical · Chart slices
// A qualitative palette for category breakdowns (donut slices, category avatars). Slice 0 echoes the
// violet brand so charts feel like FinFlow at a glance; the rest are mid-saturation hues chosen to
// stay mutually distinguishable. Income/expense semantics are NOT in here (the donut shows where
// money went, not its sign). Cycled by index, so order is stable. Dark tones are lifted for a dark
// surface.
val ChartSliceLight = listOf(
    Color(0xFF6D4FDB), // violet (brand echo)
    Color(0xFF3F8A8B), // teal
    Color(0xFFC0852E), // amber
    Color(0xFFB85C8A), // rose
    Color(0xFF5B8C4F), // green
    Color(0xFF4A78C8) // blue
)
val ChartSliceDark = listOf(
    Color(0xFFB7A6F8),
    Color(0xFF7FC9CA),
    Color(0xFFE5C07B),
    Color(0xFFE89BC0),
    Color(0xFFA6CF9A),
    Color(0xFF9DBCF0)
)
// endregion

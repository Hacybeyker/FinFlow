package com.hacybeyker.finflow.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Raw color tokens for FinFlow. These are the SINGLE SOURCE OF TRUTH at runtime and must stay in
 * sync with the values documented in DESIGN.md (repo root). Never use raw `Color(0x...)` in screens
 * or components — always consume them through `MaterialTheme.colorScheme` / `financeColors`.
 *
 * Palette: a calm, muted indigo brand with soft neutrals, plus dedicated income/expense semantics.
 */

// region Brand · Light
val PrimaryLight = Color(0xFF4A5BC4)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFDFE0FF)
val OnPrimaryContainerLight = Color(0xFF00105C)

val SecondaryLight = Color(0xFF5A5D72)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFDFE0F9)
val OnSecondaryContainerLight = Color(0xFF171A2C)

val TertiaryLight = Color(0xFF3F6375)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFC3E8FB)
val OnTertiaryContainerLight = Color(0xFF001F2A)

val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

val BackgroundLight = Color(0xFFFBF8FD)
val OnBackgroundLight = Color(0xFF1B1B21)
val SurfaceLight = Color(0xFFFBF8FD)
val OnSurfaceLight = Color(0xFF1B1B21)
val SurfaceVariantLight = Color(0xFFE3E1EC)
val OnSurfaceVariantLight = Color(0xFF46464F)
val OutlineLight = Color(0xFF777680)
val OutlineVariantLight = Color(0xFFC7C5D0)
// endregion

// region Brand · Dark
val PrimaryDark = Color(0xFFBBC3FF)
val OnPrimaryDark = Color(0xFF152178)
val PrimaryContainerDark = Color(0xFF323D90)
val OnPrimaryContainerDark = Color(0xFFDFE0FF)

val SecondaryDark = Color(0xFFC3C5DD)
val OnSecondaryDark = Color(0xFF2C2F42)
val SecondaryContainerDark = Color(0xFF424659)
val OnSecondaryContainerDark = Color(0xFFDFE0F9)

val TertiaryDark = Color(0xFFA7CCDF)
val OnTertiaryDark = Color(0xFF0A3445)
val TertiaryContainerDark = Color(0xFF254B5C)
val OnTertiaryContainerDark = Color(0xFFC3E8FB)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

val BackgroundDark = Color(0xFF131318)
val OnBackgroundDark = Color(0xFFE4E1E9)
val SurfaceDark = Color(0xFF131318)
val OnSurfaceDark = Color(0xFFE4E1E9)
val SurfaceVariantDark = Color(0xFF46464F)
val OnSurfaceVariantDark = Color(0xFFC7C5D0)
val OutlineDark = Color(0xFF918F9A)
val OutlineVariantDark = Color(0xFF46464F)
// endregion

// region Semantic · Income (muted emerald) · Expense (muted coral)
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
// A qualitative palette for category breakdowns (donut slices). Hues are muted to sit calmly next to
// the indigo brand and chosen to stay distinguishable from one another; income/expense semantics are
// NOT in here (the donut shows where money went, not its sign). Cycled by slice index, so order is
// stable. Light tones are mid-saturation on a light surface; dark tones are lifted for a dark surface.
val ChartSliceLight = listOf(
    Color(0xFF5161C9), // indigo
    Color(0xFF3F8A8B), // teal
    Color(0xFFC0852E), // amber
    Color(0xFFB85C8A), // rose
    Color(0xFF5B8C4F), // green
    Color(0xFF8A6FB0) // violet
)
val ChartSliceDark = listOf(
    Color(0xFF9FA8F0),
    Color(0xFF7FC9CA),
    Color(0xFFE5C07B),
    Color(0xFFE89BC0),
    Color(0xFFA6CF9A),
    Color(0xFFC3A8E0)
)
// endregion

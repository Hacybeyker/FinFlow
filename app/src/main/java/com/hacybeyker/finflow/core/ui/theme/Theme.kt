package com.hacybeyker.finflow.core.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.hacybeyker.finflow.core.ui.format.LocalMoneyFormatter
import com.hacybeyker.finflow.core.ui.format.MoneyFormatter

// Internal (not private) so the Glance widget builds its day/night ColorProviders from these same
// schemes — the widget must never fork its own palette.
internal val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    surfaceDim = SurfaceDimLight,
    surfaceBright = SurfaceBrightLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
    inverseSurface = InverseSurfaceLight,
    inverseOnSurface = InverseOnSurfaceLight,
    inversePrimary = InversePrimaryLight
)

internal val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    surfaceDim = SurfaceDimDark,
    surfaceBright = SurfaceBrightDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    inverseSurface = InverseSurfaceDark,
    inverseOnSurface = InverseOnSurfaceDark,
    inversePrimary = InversePrimaryDark
)

/**
 * App theme. By default [dynamicColor] is OFF so FinFlow's brand palette stays consistent on every
 * device; dynamic color is still supported if opted in (e.g. from a user preference).
 * Income/expense semantics ([financeColors]) are always brand-defined and never follow the wallpaper.
 *
 * [moneyFormatter] feeds [LocalMoneyFormatter]; the app root passes one built from the user's
 * currency preference so every amount in the tree re-formats when the setting changes.
 *
 * Switching [darkTheme] (from Settings or the system) **cross-fades** every color — scheme, finance
 * and chart palettes animate together so no element snaps while its neighbors are mid-transition.
 */
@Composable
fun FinFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    moneyFormatter: MoneyFormatter = MoneyFormatter(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val financeColors = if (darkTheme) DarkFinanceColors else LightFinanceColors
    val chartColors = if (darkTheme) DarkChartColors else LightChartColors

    CompositionLocalProvider(
        LocalFinanceColors provides financeColors.animated(),
        LocalChartColors provides chartColors.animated(),
        LocalSpacing provides Spacing(),
        LocalMoneyFormatter provides moneyFormatter
    ) {
        MaterialTheme(
            colorScheme = colorScheme.animated(),
            typography = Typography,
            shapes = FinFlowShapes,
            content = content
        )
    }
}

/** Theme-switch cross-fade duration; long enough to read as intentional, short enough to feel snappy. */
private const val THEME_TRANSITION_MILLIS = 500

/** Animates towards [target] whenever it changes; starts at the target on first composition. */
@Composable
private fun animatedColor(target: Color): Color =
    animateColorAsState(target, tween(THEME_TRANSITION_MILLIS), label = "themeColor").value

@Composable
private fun ColorScheme.animated(): ColorScheme = copy(
    primary = animatedColor(primary),
    onPrimary = animatedColor(onPrimary),
    primaryContainer = animatedColor(primaryContainer),
    onPrimaryContainer = animatedColor(onPrimaryContainer),
    inversePrimary = animatedColor(inversePrimary),
    secondary = animatedColor(secondary),
    onSecondary = animatedColor(onSecondary),
    secondaryContainer = animatedColor(secondaryContainer),
    onSecondaryContainer = animatedColor(onSecondaryContainer),
    tertiary = animatedColor(tertiary),
    onTertiary = animatedColor(onTertiary),
    tertiaryContainer = animatedColor(tertiaryContainer),
    onTertiaryContainer = animatedColor(onTertiaryContainer),
    background = animatedColor(background),
    onBackground = animatedColor(onBackground),
    surface = animatedColor(surface),
    onSurface = animatedColor(onSurface),
    surfaceVariant = animatedColor(surfaceVariant),
    onSurfaceVariant = animatedColor(onSurfaceVariant),
    surfaceTint = animatedColor(surfaceTint),
    inverseSurface = animatedColor(inverseSurface),
    inverseOnSurface = animatedColor(inverseOnSurface),
    error = animatedColor(error),
    onError = animatedColor(onError),
    errorContainer = animatedColor(errorContainer),
    onErrorContainer = animatedColor(onErrorContainer),
    outline = animatedColor(outline),
    outlineVariant = animatedColor(outlineVariant),
    surfaceBright = animatedColor(surfaceBright),
    surfaceDim = animatedColor(surfaceDim),
    surfaceContainer = animatedColor(surfaceContainer),
    surfaceContainerHigh = animatedColor(surfaceContainerHigh),
    surfaceContainerHighest = animatedColor(surfaceContainerHighest),
    surfaceContainerLow = animatedColor(surfaceContainerLow),
    surfaceContainerLowest = animatedColor(surfaceContainerLowest)
)

@Composable
private fun FinanceColors.animated(): FinanceColors = FinanceColors(
    income = animatedColor(income),
    onIncome = animatedColor(onIncome),
    incomeContainer = animatedColor(incomeContainer),
    onIncomeContainer = animatedColor(onIncomeContainer),
    expense = animatedColor(expense),
    onExpense = animatedColor(onExpense),
    expenseContainer = animatedColor(expenseContainer),
    onExpenseContainer = animatedColor(onExpenseContainer)
)

@Composable
private fun ChartColors.animated(): ChartColors = ChartColors(slices.map { animatedColor(it) })

package com.hacybeyker.finflow.feature.transactions.ui.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Velocity
import com.hacybeyker.finflow.R
import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.ui.components.AmountText
import com.hacybeyker.finflow.core.ui.theme.spacing
import kotlin.math.roundToInt

/** Supporting text on the hero card: onPrimary softened just enough to build hierarchy. */
private const val HERO_LABEL_ALPHA = 0.85f

/** Past this fraction a released half-collapse settles closed; below it, back open. */
private const val SETTLE_MIDPOINT = 0.5f

/**
 * Hero of the app: the all-time balance plus the current month's income/expense breakdown, on a
 * solid `primary` card — the single loud element on the screen (vivid violet in light, soft
 * lavender in dark, matching the FAB). Content is always `onPrimary` with signs carrying the
 * income/expense meaning (the semantic green/coral would not pass contrast here), and the balance
 * never wraps: it shrinks to fit one line.
 *
 * [collapseFraction] (0 = full, 1 = compact) folds the month stats **continuously**: it is read
 * inside layout/draw lambdas only, so scroll-driven changes re-layout without recomposing.
 */
@Composable
internal fun BalanceCard(
    balance: Money,
    monthIncome: Money,
    monthExpense: Money,
    modifier: Modifier = Modifier,
    collapseFraction: () -> Float = { 0f }
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = MaterialTheme.spacing.lg, vertical = MaterialTheme.spacing.lg)
    ) {
        Text(
            text = stringResource(R.string.home_balance_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = HERO_LABEL_ALPHA)
        )
        AmountText(
            money = balance,
            style = MaterialTheme.typography.displaySmall,
            showSign = false,
            color = MaterialTheme.colorScheme.onPrimary,
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(
                minFontSize = MaterialTheme.typography.headlineSmall.fontSize,
                maxFontSize = MaterialTheme.typography.displaySmall.fontSize
            ),
            modifier = Modifier
                .padding(top = MaterialTheme.spacing.xs)
                .align(alignment = Alignment.CenterHorizontally)
        )
        HeroStats(
            monthIncome = monthIncome,
            monthExpense = monthExpense,
            modifier = Modifier
                .graphicsLayer {
                    alpha = 1f - collapseFraction().coerceIn(0f, 1f)
                    clip = true
                }
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val fraction = collapseFraction().coerceIn(0f, 1f)
                    val height = (placeable.height * (1f - fraction)).roundToInt()
                    layout(placeable.width, height) { placeable.placeRelative(0, 0) }
                }
        )
    }
}

/**
 * Scroll-driven collapse for the hero, following Material's *enterAlways* app-bar behavior with a
 * [NestedScrollConnection]: dragging up folds the hero over the first ~72dp of the gesture, and the
 * very first drag down — anywhere in the list — unfolds it again. The consumed delta is taken away
 * from the list, so the motion under the finger is exactly the finger's displacement (this is what
 * removes the compound-movement jank a boolean+animation approach had). A release mid-way settles
 * to the nearest state with an interruption-friendly spring.
 */
@Stable
internal class HeroCollapseState(private val collapseRangePx: Float) {

    private var offsetPx by mutableFloatStateOf(0f)

    /** 0 = fully expanded, 1 = fully collapsed. Read it inside layout/draw lambdas. */
    val fraction: Float
        get() = if (collapseRangePx > 0f) offsetPx / collapseRangePx else 0f

    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val previous = offsetPx
            offsetPx = (previous - available.y).coerceIn(0f, collapseRangePx)
            return Offset(x = 0f, y = previous - offsetPx)
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            settle()
            return Velocity.Zero
        }
    }

    /** Never leaves the hero half-open: on release it finishes toward the nearest state. */
    private suspend fun settle() {
        val target = if (fraction > SETTLE_MIDPOINT) collapseRangePx else 0f
        if (target != offsetPx) {
            animate(
                initialValue = offsetPx,
                targetValue = target,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) { value, _ -> offsetPx = value }
        }
    }
}

@Composable
internal fun rememberHeroCollapseState(): HeroCollapseState {
    val collapseRangePx = with(LocalDensity.current) {
        (MaterialTheme.spacing.xxl + MaterialTheme.spacing.lg).toPx()
    }
    return remember(collapseRangePx) { HeroCollapseState(collapseRangePx) }
}

@Composable
private fun HeroStats(monthIncome: Money, monthExpense: Money, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = MaterialTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)
    ) {
        HeroStat(
            label = stringResource(R.string.home_month_income),
            money = monthIncome,
            modifier = Modifier.weight(1f)
        )
        HeroStat(
            label = stringResource(R.string.home_month_expense),
            money = -monthExpense,
            modifier = Modifier.weight(1f)
        )
    }
}

/** One hero mini-stat: quiet label over its amount, centered; the `+`/`−` sign carries the meaning. */
@Composable
private fun HeroStat(label: String, money: Money, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = HERO_LABEL_ALPHA)
        )
        AmountText(
            money = money,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

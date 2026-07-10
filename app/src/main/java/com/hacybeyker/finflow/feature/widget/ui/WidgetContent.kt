package com.hacybeyker.finflow.feature.widget.ui

import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.hacybeyker.finflow.MainActivity
import com.hacybeyker.finflow.R
import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.domain.Transaction
import com.hacybeyker.finflow.core.ui.format.MoneyFormatter
import com.hacybeyker.finflow.core.ui.theme.OnPrimaryDark
import com.hacybeyker.finflow.core.ui.theme.OnPrimaryLight
import com.hacybeyker.finflow.core.ui.theme.Spacing
import java.time.format.DateTimeFormatter
import java.util.Locale

// Glance composes outside FinFlowTheme (no CompositionLocals), so the tokens are taken straight
// from their sources: the Spacing data class and the palettes as day/night providers.
private val WidgetSpacing = Spacing()

/** Same rule as the app hero: `onPrimary` softened to 85% builds hierarchy for supporting text. */
private const val HERO_LABEL_ALPHA = 0.85f
private val HeroLabelColor = ColorProvider(
    day = OnPrimaryLight.copy(alpha = HERO_LABEL_ALPHA),
    night = OnPrimaryDark.copy(alpha = HERO_LABEL_ALPHA)
)

/** Same alpha as the label, for the divider between the balance and the movements list. */
private val DividerColor = ColorProvider(
    day = OnPrimaryLight.copy(alpha = 0.16f),
    night = OnPrimaryDark.copy(alpha = 0.16f)
)

/** A visible "chip" background for the reveal toggle — reads as a tappable control, not plain text. */
private val ToggleChipColor = ColorProvider(
    day = OnPrimaryLight.copy(alpha = 0.20f),
    night = OnPrimaryDark.copy(alpha = 0.20f)
)
private val ToggleChipRadius = 12.dp

private val BalanceFontSize = 30.sp
private val StatFontSize = 15.sp
private val LabelFontSize = 12.sp
private val BodyFontSize = 14.sp

/** Fixed-width placeholder for a masked amount — never varies with the real value's magnitude. */
private const val MASKED_AMOUNT = "••••••"

/**
 * Below this, the hero content alone (label + balance + stats + outer padding) barely fits — the
 * movements list would be cramped or clipped, so it's better hidden than half-shown. With
 * `SizeMode.Exact` this is compared against the widget's real current size, not a declared bucket.
 */
private val MinHeightForMovements = 200.dp

/** Built fresh per call (not cached) since [Locale.getDefault] can change while the process runs. */
private fun dateFormatter(): DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())

/**
 * The whole widget is one continuous surface — no inner "card within a card": the gradient fills
 * every pixel of the widget's bounds (drawn via a drawable, since Glance's `background()` only
 * takes a flat color or an image — see `res/drawable/widget_hero_gradient.xml` and its `-night`
 * counterpart), so any extra space the launcher grants beyond our content is still part of the
 * same colored surface instead of reading as empty/broken. Amounts mask to [MASKED_AMOUNT] unless
 * [revealed]; the "Mostrar"/"Ocultar" control has its own tap target ([ToggleRevealAction]),
 * separate from the rest of the widget's tap target (which opens the app).
 */
@Composable
internal fun WidgetContent(state: WidgetState, revealed: Boolean) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(ImageProvider(R.drawable.widget_hero_gradient))
            .systemCornerRadius()
            .clickable(actionStartActivity<MainActivity>())
            .padding(WidgetSpacing.md)
    ) {
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = LocalContext.current.getString(R.string.home_balance_label),
                style = TextStyle(color = HeroLabelColor, fontSize = LabelFontSize, fontWeight = FontWeight.Medium),
                modifier = GlanceModifier.defaultWeight()
            )
            RevealToggle(revealed)
        }
        Text(
            text = if (revealed) state.formatter.format(state.summary.balance) else MASKED_AMOUNT,
            maxLines = 1,
            style = TextStyle(
                color = GlanceTheme.colors.onPrimary,
                fontSize = BalanceFontSize,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(GlanceModifier.height(WidgetSpacing.sm))
        Row(GlanceModifier.fillMaxWidth()) {
            // As on the app hero: onPrimary + signs carry the meaning (green/coral fails contrast here).
            MonthStat(
                labelRes = R.string.home_month_income,
                money = state.summary.monthIncome,
                formatter = state.formatter,
                revealed = revealed,
                modifier = GlanceModifier.defaultWeight()
            )
            MonthStat(
                labelRes = R.string.home_month_expense,
                money = -state.summary.monthExpense,
                formatter = state.formatter,
                revealed = revealed,
                modifier = GlanceModifier.defaultWeight()
            )
        }
        if (LocalSize.current.height >= MinHeightForMovements && state.summary.latest.isNotEmpty()) {
            LatestMovements(latest = state.summary.latest, formatter = state.formatter, revealed = revealed)
        }
    }
}

/**
 * A padded, backgrounded "chip" rather than plain clickable text: `clickable` wraps `padding`, so
 * the whole padded area (not just the tight text glyphs) is the tap target — plain text was too
 * small to hit reliably and taps kept falling through to the widget's own "open app" tap target.
 */
@Composable
private fun RevealToggle(revealed: Boolean) {
    Box(
        modifier = GlanceModifier
            .background(ToggleChipColor)
            .cornerRadiusCompat(ToggleChipRadius)
            .clickable(actionRunCallback<ToggleRevealAction>())
            .padding(horizontal = WidgetSpacing.sm, vertical = WidgetSpacing.sm)
    ) {
        Text(
            text = LocalContext.current.getString(if (revealed) R.string.widget_hide else R.string.widget_reveal),
            style = TextStyle(color = HeroLabelColor, fontSize = LabelFontSize, fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
private fun MonthStat(
    @StringRes labelRes: Int,
    money: Money,
    formatter: MoneyFormatter,
    revealed: Boolean,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(modifier = modifier) {
        Text(
            text = LocalContext.current.getString(labelRes),
            style = TextStyle(color = HeroLabelColor, fontSize = LabelFontSize)
        )
        Text(
            text = if (revealed) formatter.formatSigned(money) else MASKED_AMOUNT,
            maxLines = 1,
            style = TextStyle(
                color = GlanceTheme.colors.onPrimary,
                fontSize = StatFontSize,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun LatestMovements(
    latest: List<Transaction>,
    formatter: MoneyFormatter,
    revealed: Boolean,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(GlanceModifier.height(WidgetSpacing.md))
        Box(GlanceModifier.fillMaxWidth().height(1.dp).background(DividerColor)) {}
        Spacer(GlanceModifier.height(WidgetSpacing.md))
        Text(
            text = LocalContext.current.getString(R.string.widget_latest),
            style = TextStyle(color = HeroLabelColor, fontSize = LabelFontSize, fontWeight = FontWeight.Medium)
        )
        latest.forEach { transaction ->
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(top = WidgetSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(GlanceModifier.defaultWeight()) {
                    Text(
                        text = transaction.category.name,
                        maxLines = 1,
                        style = TextStyle(
                            color = GlanceTheme.colors.onPrimary,
                            fontSize = BodyFontSize,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = transaction.date.format(dateFormatter()),
                        maxLines = 1,
                        style = TextStyle(color = HeroLabelColor, fontSize = LabelFontSize)
                    )
                }
                Text(
                    text = if (revealed) formatter.formatSigned(transaction.signedAmount()) else MASKED_AMOUNT,
                    maxLines = 1,
                    style = TextStyle(
                        color = GlanceTheme.colors.onPrimary,
                        fontSize = BodyFontSize,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

/**
 * Rounded corners need S+ (RemoteViews outline clipping); below that launchers show the plain rect,
 * same as every non-custom-drawable widget.
 */
private fun GlanceModifier.systemCornerRadius(): GlanceModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    cornerRadius(android.R.dimen.system_app_widget_background_radius)
} else {
    this
}

/** Same S+ gate as [systemCornerRadius], for a fixed radius instead of the launcher's own. */
private fun GlanceModifier.cornerRadiusCompat(radius: Dp): GlanceModifier =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) cornerRadius(radius) else this

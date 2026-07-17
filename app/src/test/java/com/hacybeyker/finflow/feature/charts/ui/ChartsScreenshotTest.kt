package com.hacybeyker.finflow.feature.charts.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.hacybeyker.finflow.core.domain.Category
import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.ui.theme.FinFlowTheme
import com.hacybeyker.finflow.feature.charts.domain.CategorySpending
import com.hacybeyker.finflow.feature.charts.domain.MonthlyTotal
import dagger.hilt.android.testing.HiltTestApplication
import java.time.YearMonth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Golden tests for the Canvas-drawn charts — geometry and theme colors that no semantics assert
 * can see. `recordRoborazziDebug` (re)writes the PNGs under src/test/screenshots (committed);
 * `verifyRoborazziDebug` fails when a pixel changes without re-recording.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
// HiltTestApplication (inert) instead of FinFlowApplication: the real one warms up SQLCipher's
// native library at startup, which doesn't exist for desktop JVMs.
@Config(application = HiltTestApplication::class, qualifiers = "w411dp-h891dp")
class ChartsScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val spending = listOf(
        CategorySpending(Category(id = 1, name = "Comida"), Money(45_000)),
        CategorySpending(Category(id = 2, name = "Transporte"), Money(20_000)),
        CategorySpending(Category(id = 3, name = "Ocio"), Money(12_550))
    )

    private val monthlyTotals = listOf(
        MonthlyTotal(YearMonth.of(2026, 1), income = Money(250_000), expense = Money(180_000)),
        MonthlyTotal(YearMonth.of(2026, 2), income = Money(250_000), expense = Money(210_000)),
        MonthlyTotal(YearMonth.of(2026, 3), income = Money(280_000), expense = Money(90_000)),
        MonthlyTotal(YearMonth.of(2026, 4), income = Money.ZERO, expense = Money(150_000)),
        MonthlyTotal(YearMonth.of(2026, 5), income = Money(250_000), expense = Money.ZERO),
        MonthlyTotal(YearMonth.of(2026, 6), income = Money(250_000), expense = Money(245_990))
    )

    private fun capture(darkTheme: Boolean, content: @Composable () -> Unit) {
        composeRule.setContent {
            FinFlowTheme(darkTheme = darkTheme) {
                Surface {
                    Box(Modifier.width(360.dp).padding(16.dp)) { content() }
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun spendingDonut_light() = capture(darkTheme = false) { SpendingDonut(spending = spending) }

    @Test
    fun spendingDonut_dark() = capture(darkTheme = true) { SpendingDonut(spending = spending) }

    @Test
    fun monthlyBarChart_light() = capture(darkTheme = false) { MonthlyBarChart(monthlyTotals = monthlyTotals) }

    @Test
    fun monthlyBarChart_dark() = capture(darkTheme = true) { MonthlyBarChart(monthlyTotals = monthlyTotals) }
}

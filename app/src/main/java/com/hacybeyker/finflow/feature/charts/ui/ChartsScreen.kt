package com.hacybeyker.finflow.feature.charts.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hacybeyker.finflow.R
import com.hacybeyker.finflow.core.domain.Category
import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.ui.theme.FinFlowTheme
import com.hacybeyker.finflow.core.ui.theme.spacing
import com.hacybeyker.finflow.feature.charts.domain.CategorySpending
import com.hacybeyker.finflow.feature.charts.domain.MonthlyTotal
import java.time.YearMonth

@Composable
fun ChartsScreen(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: ChartsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ChartsContent(uiState = uiState, onBack = onBack, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChartsContent(uiState: ChartsUiState, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.charts_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.charts_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        when (uiState) {
            ChartsUiState.Loading -> CenteredBox(Modifier.padding(innerPadding)) { CircularProgressIndicator() }
            is ChartsUiState.Content -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(MaterialTheme.spacing.screen),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
            ) {
                ChartCard(title = stringResource(R.string.charts_spending_title)) {
                    if (uiState.spending.isEmpty()) {
                        EmptyHint(stringResource(R.string.charts_spending_empty))
                    } else {
                        SpendingDonut(spending = uiState.spending, modifier = Modifier.fillMaxWidth())
                    }
                }
                ChartCard(title = stringResource(R.string.charts_monthly_title)) {
                    MonthlyBarChart(monthlyTotals = uiState.monthlyTotals, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun ChartCard(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(MaterialTheme.spacing.lg)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = MaterialTheme.spacing.md)
            )
            content()
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun CenteredBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) { content() }
}

@Preview(name = "Charts · Content · Light", showBackground = true)
@Preview(name = "Charts · Content · Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChartsContentPreview() {
    FinFlowTheme {
        ChartsContent(
            uiState = ChartsUiState.Content(
                spending = listOf(
                    CategorySpending(Category(1, "Comida"), Money(45000)),
                    CategorySpending(Category(2, "Ocio"), Money(28000)),
                    CategorySpending(Category(3, "Transporte"), Money(12000)),
                    CategorySpending(Category(4, "Hogar"), Money(8000))
                ),
                monthlyTotals = (0..5).map { offset ->
                    val month = YearMonth.of(2026, 1).plusMonths(offset.toLong())
                    MonthlyTotal(
                        month = month,
                        income = Money(200000 + offset * 5000L),
                        expense = Money(90000 + offset * 12000L)
                    )
                }
            ),
            onBack = {}
        )
    }
}

@Preview(name = "Charts · No expenses", showBackground = true)
@Composable
private fun ChartsEmptySpendingPreview() {
    FinFlowTheme {
        ChartsContent(
            uiState = ChartsUiState.Content(
                spending = emptyList(),
                monthlyTotals = (0..5).map { offset ->
                    MonthlyTotal(YearMonth.of(2026, 1).plusMonths(offset.toLong()), Money(150000), Money.ZERO)
                }
            ),
            onBack = {}
        )
    }
}

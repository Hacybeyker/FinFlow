package com.hacybeyker.finflow.feature.transactions.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hacybeyker.finflow.R
import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.ui.components.AmountText
import com.hacybeyker.finflow.core.ui.theme.FinFlowTheme
import com.hacybeyker.finflow.core.ui.theme.spacing
import com.hacybeyker.finflow.feature.transactions.domain.Category
import com.hacybeyker.finflow.feature.transactions.domain.Transaction
import com.hacybeyker.finflow.feature.transactions.domain.TransactionType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun HomeScreen(
    onAddTransaction: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(uiState = uiState, onAddTransaction = onAddTransaction, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(uiState: HomeUiState, onAddTransaction: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransaction) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.home_add_transaction))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(MaterialTheme.spacing.screen)
        ) {
            when (uiState) {
                HomeUiState.Loading -> CenteredBox { CircularProgressIndicator() }
                is HomeUiState.Content -> {
                    BalanceCard(balance = uiState.balance)
                    if (uiState.transactions.isEmpty()) {
                        CenteredBox {
                            Text(stringResource(R.string.home_empty), style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        TransactionList(
                            transactions = uiState.transactions,
                            modifier = Modifier.padding(top = MaterialTheme.spacing.lg)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceCard(balance: Money, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(MaterialTheme.spacing.lg)) {
            Text(
                text = stringResource(R.string.home_balance_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AmountText(
                money = balance,
                style = MaterialTheme.typography.displaySmall,
                showSign = false,
                modifier = Modifier.padding(top = MaterialTheme.spacing.xs)
            )
        }
    }
}

@Composable
private fun TransactionList(transactions: List<Transaction>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
    ) {
        items(transactions, key = { it.id }) { transaction ->
            TransactionRow(transaction)
        }
    }
}

@Composable
private fun TransactionRow(transaction: Transaction, modifier: Modifier = Modifier) {
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(transaction.category.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = transaction.date.format(dateFormatter),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        AmountText(money = transaction.signedAmount())
    }
}

@Composable
private fun CenteredBox(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) { content() }
}

@Preview(name = "Home · Content · Light", showBackground = true)
@Preview(name = "Home · Content · Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeContentPreview() {
    FinFlowTheme {
        HomeContent(
            uiState = HomeUiState.Content(
                balance = Money(123456),
                transactions = listOf(
                    Transaction(
                        1,
                        Money(200000),
                        TransactionType.INCOME,
                        Category(1, "Nómina"),
                        LocalDate.of(2026, 6, 1)
                    ),
                    Transaction(
                        2,
                        Money(4500),
                        TransactionType.EXPENSE,
                        Category(2, "Compras"),
                        LocalDate.of(2026, 6, 10)
                    )
                )
            ),
            onAddTransaction = {}
        )
    }
}

@Preview(name = "Home · Empty", showBackground = true)
@Composable
private fun HomeEmptyPreview() {
    FinFlowTheme {
        HomeContent(
            uiState = HomeUiState.Content(balance = Money.ZERO, transactions = emptyList()),
            onAddTransaction = {}
        )
    }
}

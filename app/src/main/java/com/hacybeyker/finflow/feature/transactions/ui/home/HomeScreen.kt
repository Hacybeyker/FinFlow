package com.hacybeyker.finflow.feature.transactions.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hacybeyker.finflow.R
import com.hacybeyker.finflow.core.domain.Category
import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.domain.Transaction
import com.hacybeyker.finflow.core.domain.TransactionType
import com.hacybeyker.finflow.core.ui.components.AmountText
import com.hacybeyker.finflow.core.ui.components.CategoryAvatar
import com.hacybeyker.finflow.core.ui.theme.FinFlowTheme
import com.hacybeyker.finflow.core.ui.theme.spacing
import com.hacybeyker.finflow.feature.charts.ui.ChartsBarIcon
import com.hacybeyker.finflow.feature.transactions.domain.TransactionMonth
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onAddTransaction: () -> Unit,
    onManageCategories: () -> Unit,
    onEditTransaction: (Long) -> Unit,
    onShowCharts: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val deletedMessage = stringResource(R.string.home_deleted)
    val undoLabel = stringResource(R.string.home_undo)

    HomeContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onAddTransaction = onAddTransaction,
        onManageCategories = onManageCategories,
        onShowCharts = onShowCharts,
        onOpenSettings = onOpenSettings,
        onEditTransaction = onEditTransaction,
        onDeleteTransaction = { transaction ->
            viewModel.delete(transaction)
            scope.launch {
                val result = snackbarHostState.showSnackbar(message = deletedMessage, actionLabel = undoLabel)
                if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete(transaction)
            }
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    uiState: HomeUiState,
    snackbarHostState: SnackbarHostState,
    onAddTransaction: () -> Unit,
    onManageCategories: () -> Unit,
    onShowCharts: () -> Unit,
    onOpenSettings: () -> Unit,
    onEditTransaction: (Long) -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            HomeTopBar(
                onShowCharts = onShowCharts,
                onManageCategories = onManageCategories,
                onOpenSettings = onOpenSettings
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.home_add)) }
            )
        }
    ) { innerPadding ->
        // Only the top inset is consumed here: the list owns the bottom inset as contentPadding so
        // its last items scroll all the way to the device edge instead of stopping above it. The
        // hero's collapse state intercepts the list's scroll via nested scroll (enterAlways).
        val heroState = rememberHeroCollapseState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = MaterialTheme.spacing.screen,
                    top = innerPadding.calculateTopPadding() + MaterialTheme.spacing.md,
                    end = MaterialTheme.spacing.screen
                )
                .nestedScroll(heroState.nestedScrollConnection)
        ) {
            when (uiState) {
                HomeUiState.Loading -> CenteredBox { CircularProgressIndicator() }
                is HomeUiState.Content -> {
                    BalanceCard(
                        balance = uiState.balance,
                        monthIncome = uiState.monthIncome,
                        monthExpense = uiState.monthExpense,
                        collapseFraction = { heroState.fraction }
                    )
                    if (uiState.months.isEmpty()) {
                        EmptyState()
                    } else {
                        TransactionList(
                            months = uiState.months,
                            bottomInset = innerPadding.calculateBottomPadding(),
                            onEdit = onEditTransaction,
                            onDelete = onDeleteTransaction,
                            modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    onShowCharts: () -> Unit,
    onManageCategories: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall) },
        modifier = modifier,
        actions = {
            IconButton(onClick = onShowCharts) {
                Icon(ChartsBarIcon, contentDescription = stringResource(R.string.home_charts))
            }
            IconButton(onClick = onManageCategories) {
                Icon(
                    Icons.AutoMirrored.Filled.List,
                    contentDescription = stringResource(R.string.home_manage_categories)
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.home_settings))
            }
        }
    )
}

@Composable
private fun TransactionList(
    months: List<TransactionMonth>,
    bottomInset: Dp,
    onEdit: (Long) -> Unit,
    onDelete: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy") }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        // Bottom inset + FAB clearance: the last row can always scroll clear of the "Agregar" button.
        contentPadding = PaddingValues(bottom = bottomInset + MaterialTheme.spacing.xxl + MaterialTheme.spacing.lg)
    ) {
        months.forEach { section ->
            item(key = "month-${section.month}") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MaterialTheme.spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = section.month.format(monthFormatter).replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    AmountText(money = section.total, style = MaterialTheme.typography.titleSmall)
                }
            }
            items(section.transactions, key = { it.id }) { transaction ->
                SwipeableTransactionRow(
                    transaction = transaction,
                    onEdit = { onEdit(transaction.id) },
                    onDelete = { onDelete(transaction) }
                )
            }
        }
    }
}

/** Fraction of the row the user must drag before releasing counts as a delete. */
private const val DISMISS_THRESHOLD_FRACTION = 0.5f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTransactionRow(
    transaction: Transaction,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { totalDistance -> totalDistance * DISMISS_THRESHOLD_FRACTION }
    )
    // Delete only once the swipe fully settles (not mid-gesture from confirmValueChange), then reset
    // the state: rows are keyed by id, so an undone delete restores this saved state — without the
    // reset the restored row would reappear already dismissed (stuck off-screen).
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
            dismissState.reset()
        }
    }
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = { DeleteBackground() }
    ) {
        TransactionRow(transaction = transaction, onClick = onEdit)
    }
}

@Composable
private fun DeleteBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = MaterialTheme.spacing.lg),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            Icons.Default.Delete,
            contentDescription = stringResource(R.string.home_delete),
            tint = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
private fun TransactionRow(transaction: Transaction, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
        ) {
            CategoryAvatar(category = transaction.category)
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
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.home_empty),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.home_empty_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = MaterialTheme.spacing.sm)
        )
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
                // Long figure on purpose: the hero amount must shrink to one line, never wrap.
                balance = Money(12345678),
                monthIncome = Money(200000),
                monthExpense = Money(4500),
                months = listOf(
                    TransactionMonth(
                        month = YearMonth.of(2026, 6),
                        transactions = listOf(
                            Transaction(
                                2,
                                Money(4500),
                                TransactionType.EXPENSE,
                                Category(2, "Compras"),
                                LocalDate.of(2026, 6, 10)
                            ),
                            Transaction(
                                1,
                                Money(200000),
                                TransactionType.INCOME,
                                Category(1, "Nómina"),
                                LocalDate.of(2026, 6, 1)
                            )
                        ),
                        income = Money(200000),
                        expense = Money(4500),
                        total = Money(195500)
                    ),
                    TransactionMonth(
                        month = YearMonth.of(2026, 5),
                        transactions = listOf(
                            Transaction(
                                3,
                                Money(80000),
                                TransactionType.EXPENSE,
                                Category(3, "Hogar"),
                                LocalDate.of(2026, 5, 28)
                            )
                        ),
                        income = Money.ZERO,
                        expense = Money(80000),
                        total = Money(-80000)
                    )
                )
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onAddTransaction = {},
            onManageCategories = {},
            onShowCharts = {},
            onOpenSettings = {},
            onEditTransaction = {},
            onDeleteTransaction = {}
        )
    }
}

@Preview(name = "Home · Empty", showBackground = true)
@Composable
private fun HomeEmptyPreview() {
    FinFlowTheme {
        HomeContent(
            uiState = HomeUiState.Content(
                balance = Money.ZERO,
                monthIncome = Money.ZERO,
                monthExpense = Money.ZERO,
                months = emptyList()
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onAddTransaction = {},
            onManageCategories = {},
            onShowCharts = {},
            onOpenSettings = {},
            onEditTransaction = {},
            onDeleteTransaction = {}
        )
    }
}

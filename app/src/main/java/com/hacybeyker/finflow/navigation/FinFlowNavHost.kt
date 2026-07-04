package com.hacybeyker.finflow.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.hacybeyker.finflow.feature.charts.ui.ChartsScreen
import com.hacybeyker.finflow.feature.settings.ui.SettingsScreen
import com.hacybeyker.finflow.feature.transactions.ui.add.AddTransactionScreen
import com.hacybeyker.finflow.feature.transactions.ui.categories.CategoriesScreen
import com.hacybeyker.finflow.feature.transactions.ui.home.HomeScreen

@Composable
fun FinFlowNavHost(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(Home)
    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<Home> {
                HomeScreen(
                    onAddTransaction = { backStack.add(AddTransaction()) },
                    onManageCategories = { backStack.add(Categories) },
                    onEditTransaction = { id -> backStack.add(AddTransaction(transactionId = id)) },
                    onShowCharts = { backStack.add(Charts) },
                    onOpenSettings = { backStack.add(Settings) }
                )
            }
            entry<AddTransaction> { key ->
                AddTransactionScreen(
                    onDone = { backStack.removeLastOrNull() },
                    transactionId = key.transactionId
                )
            }
            entry<Categories> { CategoriesScreen(onBack = { backStack.removeLastOrNull() }) }
            entry<Charts> { ChartsScreen(onBack = { backStack.removeLastOrNull() }) }
            entry<Settings> { SettingsScreen(onBack = { backStack.removeLastOrNull() }) }
        }
    )
}

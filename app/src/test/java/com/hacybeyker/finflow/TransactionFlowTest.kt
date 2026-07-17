package com.hacybeyker.finflow

import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.printToString
import androidx.compose.ui.test.swipeLeft
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hacybeyker.finflow.core.domain.Category
import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.domain.Transaction
import com.hacybeyker.finflow.core.domain.TransactionRepository
import com.hacybeyker.finflow.core.domain.TransactionType
import com.hacybeyker.finflow.feature.transactions.domain.CategoryRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * End-to-end flows through the real UI: Compose screens + navigation + ViewModels + Room, with
 * only the framework boundaries swapped (in-memory DB, fake preferences — see TestModules.kt).
 *
 * Known Robolectric limit: platform dialogs (the category picker) keep their window's recomposer
 * permanently busy, so Espresso never sees Compose as idle while one is open. These flows are
 * deliberately dialog-free; dialog interactions are covered by ViewModel unit tests.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
// Realistic phone screen: Robolectric's default (320x470dp) clips the bottom of the form, so taps
// on "Guardar" would land outside the viewport and silently hit nothing.
@Config(application = HiltTestApplication::class, qualifiers = "w411dp-h891dp")
class TransactionFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var categoryRepository: CategoryRepository

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun string(res: Int): String = composeRule.activity.getString(res)

    // Unmerged tree throughout: texts inside clickable containers (FAB, buttons, rows) live there.
    private fun node(text: String) = composeRule.onNodeWithText(text, useUnmergedTree = true)

    /** The full-width transaction row (its clickable Surface), not the ~50px title text inside. */
    private fun row(title: String) = composeRule.onNode(
        hasClickAction() and hasAnyDescendant(hasText(title)),
        useUnmergedTree = true
    )

    private fun waitForText(text: String) {
        try {
            composeRule.waitUntil(timeoutMillis = 10_000) {
                composeRule.onAllNodesWithText(text, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
            }
        } catch (timeout: androidx.compose.ui.test.ComposeTimeoutException) {
            // The raw timeout says nothing; re-throw with what was actually on screen.
            val tree = composeRule.onRoot(useUnmergedTree = true).printToString(maxDepth = 20)
            throw AssertionError("'$text' never appeared. Semantics tree:\n$tree", timeout)
        }
    }

    private fun seedExpense(amountMinorUnits: Long): Transaction {
        val categoryId = runBlocking { categoryRepository.add(Category(id = 0, name = "Comida")) }
        val transaction = Transaction(
            id = 0,
            amount = Money(amountMinorUnits),
            type = TransactionType.EXPENSE,
            category = Category(id = categoryId, name = "Comida"),
            date = LocalDate.now()
        )
        runBlocking { transactionRepository.add(transaction) }
        return transaction
    }

    @Test
    fun `editing a transaction from its row updates the database and Home`() {
        seedExpense(amountMinorUnits = 4550)

        // Tapping the row opens the shared form in edit mode, prefilled — no dialogs involved.
        waitForText("Comida")
        row("Comida").performClick()
        waitForText(string(R.string.edit_title))

        composeRule.onNodeWithText("45.50").performTextReplacement("99.90")
        node(string(R.string.edit_save)).performScrollTo().performClick()

        // First make sure the write reached the database at all — separates "save never fired"
        // from "Home didn't refresh" when diagnosing failures.
        val updated = runBlocking {
            withTimeoutOrNull(5_000) {
                transactionRepository.observeAll().first { it.singleOrNull()?.amount == Money(9990) }
            }
        }
        checkNotNull(updated) { "The edit was never persisted (still ${runBlocking { first() }})" }

        // Back on Home: the row and the hero balance both show the new amount.
        waitForText("Comida")
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule
                .onAllNodesWithText("99.90", substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes().size >= 2
        }
    }

    @Test
    fun `swiping a row away deletes it and undo restores it`() {
        seedExpense(amountMinorUnits = 4550)

        // Swipe on the full-width clickable row, not the ~50px text node: a swipe inside a node
        // that narrow doesn't clear the touch slop and lands as a tap (which opens the editor).
        waitForText("Comida")
        row("Comida").performTouchInput { swipeLeft() }

        // The row is gone and the snackbar offers to undo.
        waitForText(string(R.string.home_deleted))
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("Comida", useUnmergedTree = true).fetchSemanticsNodes().isEmpty()
        }

        node(string(R.string.home_undo)).performClick()

        waitForText("Comida")
    }

    private suspend fun first(): List<Transaction> = transactionRepository.observeAll().first()
}

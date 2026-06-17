package com.hacybeyker.finflow.feature.transactions.domain

import com.hacybeyker.finflow.core.domain.Money
import java.time.LocalDate

/**
 * A single income or expense entry.
 *
 * [amount] is always the positive magnitude; the direction (adds to or subtracts from the balance)
 * comes from [type]. The full [category] is embedded (not just an id) so the UI can render it
 * directly; the data layer resolves the relation on read.
 *
 * @property id persistence identifier; `0` means a transaction not stored yet.
 * @property note optional free-text description.
 */
data class Transaction(
    val id: Long = 0,
    val amount: Money,
    val type: TransactionType,
    val category: Category,
    val date: LocalDate,
    val note: String = ""
) {
    /** Contribution to the balance: income adds, expense subtracts. */
    fun signedAmount(): Money = when (type) {
        TransactionType.INCOME -> amount
        TransactionType.EXPENSE -> -amount
    }
}

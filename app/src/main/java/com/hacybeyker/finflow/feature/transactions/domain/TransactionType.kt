package com.hacybeyker.finflow.feature.transactions.domain

/**
 * Whether a transaction adds to or subtracts from the balance.
 *
 * The type only classifies the transaction; how each type affects the balance (income adds, expense
 * subtracts) is decided by the balance calculation, not encoded here.
 */
enum class TransactionType {
    INCOME,
    EXPENSE
}

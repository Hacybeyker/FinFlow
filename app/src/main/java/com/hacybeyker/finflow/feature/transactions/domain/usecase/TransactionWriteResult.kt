package com.hacybeyker.finflow.feature.transactions.domain.usecase

/** Outcome of writing a transaction (shared by add and update). */
sealed interface TransactionWriteResult {
    data object Success : TransactionWriteResult
    data object InvalidAmount : TransactionWriteResult
}

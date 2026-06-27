package com.hacybeyker.finflow.core.domain

enum class TransactionType {
    INCOME,
    EXPENSE;

    companion object {
        fun fromStorage(value: String): TransactionType = entries.find { it.name == value }
            ?: error("Unknown TransactionType stored: '$value'")
    }
}

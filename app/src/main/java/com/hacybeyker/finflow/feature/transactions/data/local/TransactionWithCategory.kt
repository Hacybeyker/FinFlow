package com.hacybeyker.finflow.feature.transactions.data.local

import androidx.room.Embedded

/** JOIN result: a transaction row plus the resolved name of its category. */
data class TransactionWithCategory(@Embedded val transaction: TransactionEntity, val categoryName: String)

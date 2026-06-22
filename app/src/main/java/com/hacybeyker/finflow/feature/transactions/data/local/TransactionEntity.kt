package com.hacybeyker.finflow.feature.transactions.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room row for a transaction. Stores only primitives (no TypeConverters): the amount as `Long` minor
 * units, the type as the enum name, and the date as an epoch-day `Long`; conversion to/from the
 * domain model happens in the mapper.
 *
 * The category is **denormalized as a snapshot** ([categoryId] + [categoryName]) for the transactions
 * slice. The `categories` feature (Slice 2) introduces a real categories table and migrates this.
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMinorUnits: Long,
    val type: String,
    val categoryId: Long,
    val categoryName: String,
    val epochDay: Long,
    val note: String
)

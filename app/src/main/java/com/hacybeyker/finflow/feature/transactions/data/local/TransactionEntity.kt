package com.hacybeyker.finflow.feature.transactions.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room row for a transaction. Stores only primitives (no TypeConverters): the amount as `Long` minor
 * units, the type as the enum name, and the date as an epoch-day `Long`.
 *
 * The category is **normalized**: only [categoryId] is stored, as a foreign key into `categories`.
 * Deleting a category cascades to its transactions. Reads resolve the category name with a JOIN (see
 * [TransactionWithCategory]).
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMinorUnits: Long,
    val type: String,
    val categoryId: Long,
    val epochDay: Long,
    val note: String
)

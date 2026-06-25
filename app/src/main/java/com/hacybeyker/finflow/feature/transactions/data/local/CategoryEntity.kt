package com.hacybeyker.finflow.feature.transactions.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room row for a category. The unique index on [name] enforces no duplicates at the storage level
 * (the domain also guards against case-insensitive duplicates before inserting).
 */
@Entity(tableName = "categories", indices = [Index(value = ["name"], unique = true)])
data class CategoryEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String)

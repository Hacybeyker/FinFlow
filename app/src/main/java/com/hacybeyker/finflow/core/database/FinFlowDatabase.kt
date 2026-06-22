package com.hacybeyker.finflow.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hacybeyker.finflow.feature.transactions.data.local.TransactionDao
import com.hacybeyker.finflow.feature.transactions.data.local.TransactionEntity

/**
 * The single Room database for the whole app (SSOT). It is cross-feature by nature: it aggregates the
 * `@Entity`/DAO that each feature contributes, so it lives in `core` rather than inside a feature.
 *
 * `exportSchema` is off for now; it will be enabled (with a schema location) when versioned
 * migrations arrive (categories table in Slice 2, encryption in Slice 4).
 */
@Database(entities = [TransactionEntity::class], version = 1, exportSchema = false)
abstract class FinFlowDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
}

package com.hacybeyker.finflow.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hacybeyker.finflow.feature.transactions.data.local.CategoryDao
import com.hacybeyker.finflow.feature.transactions.data.local.CategoryEntity
import com.hacybeyker.finflow.feature.transactions.data.local.TransactionDao
import com.hacybeyker.finflow.feature.transactions.data.local.TransactionEntity

/**
 * The single Room database for the whole app (SSOT). It is cross-feature by nature: it aggregates the
 * `@Entity`/DAO that each feature contributes, so it lives in `core` rather than inside a feature.
 *
 * `exportSchema` is on (schema JSONs under `app/schemas`) so versioned migrations can be diffed and
 * tested. v2 adds the `categories` table ([MIGRATION_1_2]); v3 normalizes transactions onto a
 * category foreign key ([MIGRATION_2_3]).
 */
@Database(entities = [TransactionEntity::class, CategoryEntity::class], version = 3, exportSchema = true)
abstract class FinFlowDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
}

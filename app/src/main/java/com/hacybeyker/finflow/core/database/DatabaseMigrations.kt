package com.hacybeyker.finflow.core.database

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * v1 → v2: introduces the normalized `categories` table and seeds it from the distinct category
 * names already snapshotted on existing transactions, so no category is lost. The `transactions`
 * table keeps its denormalized `categoryName` for now; the foreign-key normalization lands together
 * with the category picker (so new transactions can supply a real `categoryId`).
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `categories` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)"
        )
        connection.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_categories_name` ON `categories` (`name`)"
        )
        connection.execSQL("INSERT INTO `categories` (`name`) SELECT DISTINCT `categoryName` FROM `transactions`")
    }
}

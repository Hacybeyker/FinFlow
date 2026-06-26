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

/**
 * v2 → v3: normalizes transactions onto a `categoryId` foreign key (ON DELETE CASCADE) and drops the
 * denormalized `categoryName` snapshot. Reads now resolve the name with a JOIN. Robust to category
 * renames/deletes made since v2: it first re-seeds any missing names, then matches each transaction's
 * snapshot to a category id before rebuilding the table.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "INSERT OR IGNORE INTO `categories` (`name`) SELECT DISTINCT `categoryName` FROM `transactions`"
        )
        connection.execSQL(
            "UPDATE `transactions` SET `categoryId` = " +
                "(SELECT `id` FROM `categories` WHERE `categories`.`name` = `transactions`.`categoryName`)"
        )
        connection.execSQL(
            "CREATE TABLE `transactions_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`amountMinorUnits` INTEGER NOT NULL, `type` TEXT NOT NULL, `categoryId` INTEGER NOT NULL, " +
                "`epochDay` INTEGER NOT NULL, `note` TEXT NOT NULL, " +
                "FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        connection.execSQL(
            "INSERT INTO `transactions_new` (`id`, `amountMinorUnits`, `type`, `categoryId`, `epochDay`, `note`) " +
                "SELECT `id`, `amountMinorUnits`, `type`, `categoryId`, `epochDay`, `note` FROM `transactions`"
        )
        connection.execSQL("DROP TABLE `transactions`")
        connection.execSQL("ALTER TABLE `transactions_new` RENAME TO `transactions`")
        connection.execSQL("CREATE INDEX `index_transactions_categoryId` ON `transactions` (`categoryId`)")
    }
}

package com.hacybeyker.finflow.feature.transactions.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query(
        "SELECT t.*, c.name AS categoryName FROM transactions t JOIN categories c ON t.categoryId = c.id " +
            "ORDER BY t.epochDay DESC, t.id DESC"
    )
    fun observeAll(): Flow<List<TransactionWithCategory>>

    @Query(
        "SELECT t.*, c.name AS categoryName FROM transactions t JOIN categories c ON t.categoryId = c.id " +
            "WHERE t.epochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY t.epochDay DESC, t.id DESC"
    )
    fun observeByEpochDayRange(startEpochDay: Long, endEpochDay: Long): Flow<List<TransactionWithCategory>>

    @Insert
    suspend fun insert(entity: TransactionEntity)

    @Update
    suspend fun update(entity: TransactionEntity)

    @Delete
    suspend fun delete(entity: TransactionEntity)
}

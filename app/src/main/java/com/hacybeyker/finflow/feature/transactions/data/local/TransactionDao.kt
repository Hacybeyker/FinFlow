package com.hacybeyker.finflow.feature.transactions.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY epochDay DESC, id DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query(
        "SELECT * FROM transactions WHERE epochDay BETWEEN :startEpochDay AND :endEpochDay " +
            "ORDER BY epochDay DESC, id DESC"
    )
    fun observeByEpochDayRange(startEpochDay: Long, endEpochDay: Long): Flow<List<TransactionEntity>>

    @Insert
    suspend fun insert(entity: TransactionEntity)

    @Update
    suspend fun update(entity: TransactionEntity)

    @Delete
    suspend fun delete(entity: TransactionEntity)
}

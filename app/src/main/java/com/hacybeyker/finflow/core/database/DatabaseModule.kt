package com.hacybeyker.finflow.core.database

import android.content.Context
import androidx.room.Room
import com.hacybeyker.finflow.feature.transactions.data.local.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FinFlowDatabase =
        Room.databaseBuilder(context, FinFlowDatabase::class.java, "finflow.db").build()

    @Provides
    fun provideTransactionDao(database: FinFlowDatabase): TransactionDao = database.transactionDao()
}

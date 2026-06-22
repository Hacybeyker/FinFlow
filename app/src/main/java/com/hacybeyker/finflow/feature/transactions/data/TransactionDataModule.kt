package com.hacybeyker.finflow.feature.transactions.data

import com.hacybeyker.finflow.feature.transactions.domain.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TransactionDataModule {

    @Binds
    abstract fun bindTransactionRepository(impl: RoomTransactionRepository): TransactionRepository
}

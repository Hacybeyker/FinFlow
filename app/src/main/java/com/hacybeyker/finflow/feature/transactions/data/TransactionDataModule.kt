package com.hacybeyker.finflow.feature.transactions.data

import com.hacybeyker.finflow.core.domain.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface TransactionDataModule {

    @Binds
    fun bindTransactionRepository(impl: RoomTransactionRepository): TransactionRepository
}

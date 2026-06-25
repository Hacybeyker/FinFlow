package com.hacybeyker.finflow.feature.transactions.data

import com.hacybeyker.finflow.feature.transactions.domain.CategoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CategoryDataModule {

    @Binds
    abstract fun bindCategoryRepository(impl: RoomCategoryRepository): CategoryRepository
}

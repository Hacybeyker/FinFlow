package com.hacybeyker.finflow.core.database

import android.content.Context
import androidx.room.Room
import com.hacybeyker.finflow.feature.transactions.data.local.CategoryDao
import com.hacybeyker.finflow.feature.transactions.data.local.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "finflow.db"

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context, databaseKey: DatabaseKey): FinFlowDatabase {
        // Idempotent safety net: FinFlowApplication already loads this on IO at startup; if that
        // warmup hasn't finished yet, this call resolves it (a no-op once loaded).
        System.loadLibrary("sqlcipher")
        databaseKey.purgeLegacyPlaintextDbOnce(DATABASE_NAME)
        return Room.databaseBuilder(context, FinFlowDatabase::class.java, DATABASE_NAME)
            .openHelperFactory(SupportOpenHelperFactory(databaseKey.passphrase()))
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }

    @Provides
    fun provideTransactionDao(database: FinFlowDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideCategoryDao(database: FinFlowDatabase): CategoryDao = database.categoryDao()
}

package com.hacybeyker.finflow

import android.content.Context
import androidx.room.Room
import com.hacybeyker.finflow.core.database.DatabaseModule
import com.hacybeyker.finflow.core.database.FinFlowDatabase
import com.hacybeyker.finflow.core.domain.FakePreferencesRepository
import com.hacybeyker.finflow.core.domain.PreferencesRepository
import com.hacybeyker.finflow.core.domain.UserPreferences
import com.hacybeyker.finflow.feature.settings.data.SettingsDataModule
import com.hacybeyker.finflow.feature.settings.domain.CsvSaver
import com.hacybeyker.finflow.feature.settings.domain.FakeCsvSaver
import com.hacybeyker.finflow.feature.transactions.data.local.CategoryDao
import com.hacybeyker.finflow.feature.transactions.data.local.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * SQLCipher ships Android-only native libraries, so the JVM tests swap the encrypted database for
 * a plain in-memory Room over the same schema. Encryption itself stays out of scope by design:
 * it's a framework-boundary concern (same criterion as the Kover filters).
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DatabaseModule::class])
object TestDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FinFlowDatabase =
        Room.inMemoryDatabaseBuilder(context, FinFlowDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    @Provides
    fun provideTransactionDao(database: FinFlowDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideCategoryDao(database: FinFlowDatabase): CategoryDao = database.categoryDao()
}

/**
 * The fake starts with the app lock disabled so flow tests land directly on Home — the biometric
 * prompt is system UI that a JVM test can neither render nor dismiss.
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [SettingsDataModule::class])
object TestSettingsModule {

    @Provides
    @Singleton
    fun providePreferencesRepository(): PreferencesRepository =
        FakePreferencesRepository(UserPreferences(appLockEnabled = false))

    @Provides
    fun provideCsvSaver(): CsvSaver = FakeCsvSaver()
}

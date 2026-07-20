package com.hacybeyker.finflow.feature.reminders.data

import com.hacybeyker.finflow.feature.reminders.domain.ReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface RemindersDataModule {

    @Binds
    fun bindReminderScheduler(impl: WorkManagerReminderScheduler): ReminderScheduler
}

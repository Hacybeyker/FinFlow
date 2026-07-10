package com.hacybeyker.finflow.feature.reminders.domain.usecase

import com.hacybeyker.finflow.core.domain.FakePreferencesRepository
import com.hacybeyker.finflow.core.domain.UserPreferences
import com.hacybeyker.finflow.feature.reminders.domain.FakeReminderScheduler
import java.time.LocalTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SetReminderTimeUseCaseTest {

    private val scheduler = FakeReminderScheduler()

    @Test
    fun `changing the time reschedules when the reminder is enabled`() = runTest {
        val repository = FakePreferencesRepository(UserPreferences(reminderEnabled = true))
        val useCase = SetReminderTimeUseCase(repository, scheduler)

        useCase(LocalTime.of(7, 15))

        assertEquals(LocalTime.of(7, 15), repository.observePreferences().first().reminderTime)
        assertEquals(LocalTime.of(7, 15), scheduler.scheduledTime)
    }

    @Test
    fun `changing the time only persists when the reminder is disabled`() = runTest {
        val repository = FakePreferencesRepository(UserPreferences(reminderEnabled = false))
        val useCase = SetReminderTimeUseCase(repository, scheduler)

        useCase(LocalTime.of(7, 15))

        assertEquals(LocalTime.of(7, 15), repository.observePreferences().first().reminderTime)
        assertNull(scheduler.scheduledTime)
    }
}

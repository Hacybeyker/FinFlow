package com.hacybeyker.finflow.feature.reminders.domain.usecase

import com.hacybeyker.finflow.core.domain.FakePreferencesRepository
import com.hacybeyker.finflow.core.domain.UserPreferences
import com.hacybeyker.finflow.feature.reminders.domain.FakeReminderScheduler
import java.time.LocalTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SetReminderEnabledUseCaseTest {

    private val repository = FakePreferencesRepository(
        UserPreferences(reminderTime = LocalTime.of(9, 30))
    )
    private val scheduler = FakeReminderScheduler()
    private val useCase = SetReminderEnabledUseCase(repository, scheduler)

    @Test
    fun `enabling persists the preference and schedules at the stored time`() = runTest {
        useCase(true)

        assertTrue(repository.observePreferences().first().reminderEnabled)
        assertEquals(LocalTime.of(9, 30), scheduler.scheduledTime)
    }

    @Test
    fun `disabling persists the preference and cancels the scheduled work`() = runTest {
        useCase(true)
        useCase(false)

        assertFalse(repository.observePreferences().first().reminderEnabled)
        assertTrue(scheduler.cancelled)
        assertEquals(null, scheduler.scheduledTime)
    }
}

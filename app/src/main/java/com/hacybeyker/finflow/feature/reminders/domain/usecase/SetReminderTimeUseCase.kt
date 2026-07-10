package com.hacybeyker.finflow.feature.reminders.domain.usecase

import com.hacybeyker.finflow.core.domain.PreferencesRepository
import com.hacybeyker.finflow.feature.reminders.domain.ReminderScheduler
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class SetReminderTimeUseCase @Inject constructor(
    private val repository: PreferencesRepository,
    private val scheduler: ReminderScheduler
) {
    suspend operator fun invoke(time: LocalTime) {
        repository.setReminderTime(time)
        if (repository.observePreferences().first().reminderEnabled) {
            scheduler.scheduleDaily(time)
        }
    }
}

package com.hacybeyker.finflow.feature.reminders.domain.usecase

import com.hacybeyker.finflow.core.domain.PreferencesRepository
import com.hacybeyker.finflow.feature.reminders.domain.ReminderScheduler
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Persists the toggle and keeps the scheduled work in sync with it — the preference and the
 * WorkManager job must never disagree, so both changes live behind this single entry point.
 */
class SetReminderEnabledUseCase @Inject constructor(
    private val repository: PreferencesRepository,
    private val scheduler: ReminderScheduler
) {
    suspend operator fun invoke(enabled: Boolean) {
        repository.setReminderEnabled(enabled)
        if (enabled) {
            scheduler.scheduleDaily(repository.observePreferences().first().reminderTime)
        } else {
            scheduler.cancel()
        }
    }
}

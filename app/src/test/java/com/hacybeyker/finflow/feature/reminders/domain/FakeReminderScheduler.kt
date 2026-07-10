package com.hacybeyker.finflow.feature.reminders.domain

import java.time.LocalTime

/** Records the last interaction so tests can assert scheduling side effects without WorkManager. */
class FakeReminderScheduler : ReminderScheduler {

    var scheduledTime: LocalTime? = null
        private set
    var cancelled: Boolean = false
        private set

    override fun scheduleDaily(time: LocalTime) {
        scheduledTime = time
        cancelled = false
    }

    override fun cancel() {
        scheduledTime = null
        cancelled = true
    }
}

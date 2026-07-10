package com.hacybeyker.finflow.feature.reminders.data

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.hacybeyker.finflow.feature.reminders.domain.ReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Daily reminder as unique periodic work: 24h interval, with an initial delay that lands the first
 * run on the next occurrence of [scheduleDaily]'s time. WorkManager (not an exact AlarmManager
 * alarm) on purpose: a nudge tolerates the system's inexact window, and in exchange it survives
 * reboots and needs no `SCHEDULE_EXACT_ALARM` special permission.
 *
 * The policy is CANCEL_AND_REENQUEUE, not UPDATE: our initial delay *encodes the fire hour*, and
 * UPDATE preserves the original period clock — changing 20:00 → 09:00 with UPDATE would keep firing
 * at 20:00.
 */
class WorkManagerReminderScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val clock: Clock
) : ReminderScheduler {

    override fun scheduleDaily(time: LocalTime) {
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelayUntilNext(time, clock))
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, request)
    }

    override fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    companion object {
        const val WORK_NAME = "finflow-daily-reminder"
    }
}

/** Time until the next occurrence of [target]: later today, or tomorrow if it already passed. */
internal fun initialDelayUntilNext(target: LocalTime, clock: Clock): Duration {
    val now = LocalDateTime.now(clock)
    val todayAtTarget = now.toLocalDate().atTime(target)
    val next = if (todayAtTarget.isAfter(now)) todayAtTarget else todayAtTarget.plusDays(1)
    return Duration.between(now, next)
}

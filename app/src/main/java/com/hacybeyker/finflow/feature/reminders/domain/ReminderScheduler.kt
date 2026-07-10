package com.hacybeyker.finflow.feature.reminders.domain

import java.time.LocalTime

/**
 * Domain contract for the daily reminder alarm. Implementations must be idempotent: scheduling
 * twice with the same time leaves exactly one pending reminder, and scheduling with a new time
 * replaces the old one.
 */
interface ReminderScheduler {
    fun scheduleDaily(time: LocalTime)
    fun cancel()
}

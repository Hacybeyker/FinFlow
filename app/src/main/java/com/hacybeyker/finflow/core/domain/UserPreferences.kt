package com.hacybeyker.finflow.core.domain

import java.time.LocalTime

/**
 * The user's persistent settings. Defaults are the app's original behavior (system theme, device
 * currency, lock on, no reminder), so a fresh install — or a missing key — changes nothing for the
 * user.
 *
 * [currencyCode] is an ISO 4217 code (e.g. "PEN", "USD") or `null` to follow the device locale.
 * [reminderTime] defaults to 20:00 — end of day, when there are movements worth logging.
 */
data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val currencyCode: String? = null,
    val appLockEnabled: Boolean = true,
    val reminderEnabled: Boolean = false,
    val reminderTime: LocalTime = DefaultReminderTime
) {
    companion object {
        val DefaultReminderTime: LocalTime = LocalTime.of(20, 0)
    }
}

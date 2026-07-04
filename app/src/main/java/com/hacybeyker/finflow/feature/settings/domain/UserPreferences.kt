package com.hacybeyker.finflow.feature.settings.domain

/**
 * The user's persistent settings. Defaults are the app's original behavior (system theme, device
 * currency, lock on), so a fresh install — or a missing key — changes nothing for the user.
 *
 * [currencyCode] is an ISO 4217 code (e.g. "PEN", "USD") or `null` to follow the device locale.
 */
data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val currencyCode: String? = null,
    val appLockEnabled: Boolean = true
)

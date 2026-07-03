package com.hacybeyker.finflow.feature.security.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Holds the lock gate's state. Living in a ViewModel means rotation keeps the app unlocked (the grace
 * period below covers the quick stop/start of a config change) while process death resets it to locked.
 *
 * Re-locks when the app comes back from background after more than [RELOCK_GRACE_MILLIS]: a finance
 * app must not stay open indefinitely from recents, but quick exits (share sheet, permission dialog,
 * a fast app switch) shouldn't re-prompt either. Timestamps come in as parameters (the UI passes
 * `SystemClock.elapsedRealtime()`) so the logic is testable without mocking the clock.
 */
@HiltViewModel
class SecurityViewModel @Inject constructor() : ViewModel() {

    var unlocked by mutableStateOf(false)
        private set

    private var backgroundedAtMillis: Long? = null

    fun markUnlocked() {
        unlocked = true
    }

    fun onAppBackgrounded(nowMillis: Long) {
        backgroundedAtMillis = nowMillis
    }

    fun onAppForegrounded(nowMillis: Long) {
        val backgroundedAt = backgroundedAtMillis ?: return
        if (unlocked && nowMillis - backgroundedAt > RELOCK_GRACE_MILLIS) {
            unlocked = false
        }
    }

    companion object {
        const val RELOCK_GRACE_MILLIS = 30_000L
    }
}

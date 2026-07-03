package com.hacybeyker.finflow.feature.security.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityViewModelTest {

    private val grace = SecurityViewModel.RELOCK_GRACE_MILLIS

    @Test
    fun `starts locked and unlocks on markUnlocked`() {
        val viewModel = SecurityViewModel()
        assertFalse(viewModel.unlocked)

        viewModel.markUnlocked()

        assertTrue(viewModel.unlocked)
    }

    @Test
    fun `stays unlocked when returning within the grace period`() {
        val viewModel = SecurityViewModel()
        viewModel.markUnlocked()

        viewModel.onAppBackgrounded(nowMillis = 1_000L)
        viewModel.onAppForegrounded(nowMillis = 1_000L + grace)

        assertTrue(viewModel.unlocked)
    }

    @Test
    fun `re-locks when returning after the grace period`() {
        val viewModel = SecurityViewModel()
        viewModel.markUnlocked()

        viewModel.onAppBackgrounded(nowMillis = 1_000L)
        viewModel.onAppForegrounded(nowMillis = 1_000L + grace + 1L)

        assertFalse(viewModel.unlocked)
    }

    @Test
    fun `a foreground event without a prior background is ignored`() {
        val viewModel = SecurityViewModel()
        viewModel.markUnlocked()

        viewModel.onAppForegrounded(nowMillis = Long.MAX_VALUE)

        assertTrue(viewModel.unlocked)
    }

    @Test
    fun `staying locked in background does not unlock on return`() {
        val viewModel = SecurityViewModel()

        viewModel.onAppBackgrounded(nowMillis = 1_000L)
        viewModel.onAppForegrounded(nowMillis = 1_000L + grace + 1L)

        assertFalse(viewModel.unlocked)
    }
}

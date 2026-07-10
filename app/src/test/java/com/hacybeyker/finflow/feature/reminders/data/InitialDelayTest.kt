package com.hacybeyker.finflow.feature.reminders.data

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class InitialDelayTest {

    // 2026-07-09T10:00 in a fixed zone, so the math is deterministic.
    private val clock = Clock.fixed(Instant.parse("2026-07-09T10:00:00Z"), ZoneId.of("UTC"))

    @Test
    fun `target later today waits until today's occurrence`() {
        assertEquals(Duration.ofHours(10), initialDelayUntilNext(LocalTime.of(20, 0), clock))
    }

    @Test
    fun `target already passed today waits until tomorrow`() {
        assertEquals(Duration.ofHours(23), initialDelayUntilNext(LocalTime.of(9, 0), clock))
    }

    @Test
    fun `target exactly now waits a full day instead of firing immediately`() {
        assertEquals(Duration.ofHours(24), initialDelayUntilNext(LocalTime.of(10, 0), clock))
    }
}

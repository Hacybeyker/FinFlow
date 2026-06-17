package com.hacybeyker.finflow.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyTest {

    @Test
    fun `plus adds minor units`() {
        val result = Money(1250) + Money(750)

        assertEquals(Money(2000), result)
    }

    @Test
    fun `minus subtracts minor units and can go negative`() {
        val result = Money(500) - Money(1200)

        assertEquals(Money(-700), result)
    }

    @Test
    fun `unaryMinus negates the amount`() {
        assertEquals(Money(-300), -Money(300))
    }

    @Test
    fun `ZERO has no minor units and reports isZero`() {
        assertEquals(0L, Money.ZERO.minorUnits)
        assertTrue(Money.ZERO.isZero)
        assertFalse(Money.ZERO.isPositive)
        assertFalse(Money.ZERO.isNegative)
    }

    @Test
    fun `sign helpers reflect the amount`() {
        assertTrue(Money(1).isPositive)
        assertTrue(Money(-1).isNegative)
    }

    @Test
    fun `compareTo orders by minor units`() {
        assertTrue(Money(100) < Money(200))
        assertTrue(Money(-50) < Money.ZERO)
        assertEquals(0, Money(100).compareTo(Money(100)))
    }

    @Test
    fun `equality is based on the minor units value`() {
        assertEquals(Money(999), Money(999))
    }
}

package com.hacybeyker.finflow.core.domain

/**
 * Monetary amount stored as an exact integer count of **minor units** (e.g. cents).
 *
 * Money is never a floating-point type: `Double`/`Float` cannot represent most decimal amounts
 * exactly (`0.1 + 0.2 != 0.3`), which corrupts balances over time. Storing minor units in a [Long]
 * keeps every operation exact.
 *
 * It is a `value class`, so it gives type-safety (you can't pass a raw `Long` where `Money` is
 * expected) with **no runtime allocation**: the compiler inlines it to a plain `Long` wherever it can.
 *
 * Amounts can be negative (a balance may be below zero). Parsing/formatting per locale (e.g. turning
 * "12.34 €" into 1234 minor units and back) is a UI concern and deliberately lives outside the domain.
 */
@JvmInline
value class Money(val minorUnits: Long) : Comparable<Money> {

    val isZero: Boolean get() = minorUnits == 0L
    val isPositive: Boolean get() = minorUnits > 0L
    val isNegative: Boolean get() = minorUnits < 0L

    operator fun plus(other: Money): Money = Money(minorUnits + other.minorUnits)

    operator fun minus(other: Money): Money = Money(minorUnits - other.minorUnits)

    operator fun unaryMinus(): Money = Money(-minorUnits)

    override fun compareTo(other: Money): Int = minorUnits.compareTo(other.minorUnits)

    companion object {
        val ZERO = Money(0L)
    }
}

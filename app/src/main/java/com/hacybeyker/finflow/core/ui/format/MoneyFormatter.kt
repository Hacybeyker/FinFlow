package com.hacybeyker.finflow.core.ui.format

import androidx.compose.runtime.staticCompositionLocalOf
import com.hacybeyker.finflow.core.domain.Money
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Turns the domain [Money] (exact minor units) into a localized currency string. Formatting is a UI
 * concern, so it lives here and never leaks into the domain.
 *
 * Defaults to the device locale and its currency. Both are overridable so a future Settings screen
 * can let the user pick a currency (e.g. PEN/USD) by providing a different formatter through
 * [LocalMoneyFormatter] — no component needs to change.
 */
class MoneyFormatter(locale: Locale = Locale.getDefault(), currency: Currency? = null) {
    private val format: NumberFormat = NumberFormat.getCurrencyInstance(locale).apply {
        if (currency != null) this.currency = currency
    }

    private val fractionDigits: Int = format.currency?.defaultFractionDigits ?: DEFAULT_FRACTION_DIGITS

    fun format(money: Money): String = format.format(BigDecimal.valueOf(money.minorUnits).movePointLeft(fractionDigits))

    /** Same as [format], plus a `+` prefix for positive amounts (NumberFormat already adds `-`). */
    fun formatSigned(money: Money): String {
        val formatted = format(money)
        return if (money.isPositive) "+$formatted" else formatted
    }

    private companion object {
        const val DEFAULT_FRACTION_DIGITS = 2
    }
}

val LocalMoneyFormatter = staticCompositionLocalOf { MoneyFormatter() }

/** Builds a formatter for a stored ISO 4217 currency code (`null` = device default). */
fun moneyFormatterFor(currencyCode: String?): MoneyFormatter =
    MoneyFormatter(currency = currencyCode?.let(Currency::getInstance))

/**
 * Parses user input ("45.50", "45,50", "1200") into exact [Money] minor units, or `null` if it is
 * not a valid number. Both `.` and `,` are accepted as the decimal separator. Uses [BigDecimal] so
 * no floating-point rounding ever touches an amount.
 */
fun parseMoneyOrNull(input: String, fractionDigits: Int = 2): Money? {
    val normalized = input.trim().replace(',', '.')
    val decimal = normalized.toBigDecimalOrNull() ?: return null
    return Money(decimal.movePointRight(fractionDigits).setScale(0, RoundingMode.HALF_UP).toLong())
}

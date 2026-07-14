package com.hacybeyker.finflow.feature.transactions.domain.usecase

import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.domain.Transaction
import com.hacybeyker.finflow.core.domain.TransactionRepository
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Builds a CSV snapshot of every transaction, oldest first (spreadsheet-friendly order). The note
 * is free text, so fields are escaped per RFC 4180: anything containing commas, quotes or line
 * breaks gets quoted, and inner quotes are doubled. Amounts are signed plain decimals ("." point,
 * no currency symbol) so a spreadsheet SUM() over the column reproduces the balance.
 */
class ExportTransactionsCsvUseCase @Inject constructor(private val repository: TransactionRepository) {

    suspend operator fun invoke(): String {
        val rows = repository.observeAll().first()
            .sortedWith(compareBy({ it.date }, { it.id }))
            .map { it.toCsvRow() }
        return (listOf(HEADER) + rows).joinToString(separator = "\n", postfix = "\n")
    }

    private fun Transaction.toCsvRow(): String = listOf(
        date.toString(), // ISO-8601 (yyyy-MM-dd): unambiguous and sorts lexicographically
        category.name,
        type.name,
        signedAmount().toDecimalString(),
        note
    ).joinToString(",") { it.escapeCsv() }

    private companion object {
        const val HEADER = "date,category,type,amount,note"
        const val FRACTION_DIGITS = 2
    }

    private fun Money.toDecimalString(): String =
        BigDecimal.valueOf(minorUnits).movePointLeft(FRACTION_DIGITS).toPlainString()
}

private const val CSV_SPECIAL_CHARS = ",\"\n\r"

private fun String.escapeCsv(): String = if (any { it in CSV_SPECIAL_CHARS }) {
    "\"${replace("\"", "\"\"")}\""
} else {
    this
}

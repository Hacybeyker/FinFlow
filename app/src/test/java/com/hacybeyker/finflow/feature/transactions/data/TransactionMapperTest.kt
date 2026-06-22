package com.hacybeyker.finflow.feature.transactions.data

import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.feature.transactions.data.local.TransactionEntity
import com.hacybeyker.finflow.feature.transactions.domain.Category
import com.hacybeyker.finflow.feature.transactions.domain.Transaction
import com.hacybeyker.finflow.feature.transactions.domain.TransactionType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TransactionMapperTest {

    private val domain = Transaction(
        id = 7,
        amount = Money(1599),
        type = TransactionType.EXPENSE,
        category = Category(id = 3, name = "Groceries"),
        date = LocalDate.of(2026, 6, 15),
        note = "Weekly shop"
    )

    @Test
    fun `toEntity maps every field to primitive columns`() {
        val entity = domain.toEntity()

        assertEquals(7, entity.id)
        assertEquals(1599, entity.amountMinorUnits)
        assertEquals("EXPENSE", entity.type)
        assertEquals(3, entity.categoryId)
        assertEquals("Groceries", entity.categoryName)
        assertEquals(LocalDate.of(2026, 6, 15).toEpochDay(), entity.epochDay)
        assertEquals("Weekly shop", entity.note)
    }

    @Test
    fun `toDomain rebuilds the domain model from columns`() {
        val entity = TransactionEntity(
            id = 7,
            amountMinorUnits = 1599,
            type = "EXPENSE",
            categoryId = 3,
            categoryName = "Groceries",
            epochDay = LocalDate.of(2026, 6, 15).toEpochDay(),
            note = "Weekly shop"
        )

        assertEquals(domain, entity.toDomain())
    }

    @Test
    fun `entity to domain round-trip preserves the transaction`() {
        assertEquals(domain, domain.toEntity().toDomain())
    }

    @Test
    fun `toDomain fails fast on an unknown transaction type`() {
        val corrupted = TransactionEntity(
            id = 7,
            amountMinorUnits = 1599,
            type = "TRANSFER",
            categoryId = 3,
            categoryName = "Groceries",
            epochDay = LocalDate.of(2026, 6, 15).toEpochDay(),
            note = "Weekly shop"
        )

        assertThrows(IllegalStateException::class.java) { corrupted.toDomain() }
    }
}

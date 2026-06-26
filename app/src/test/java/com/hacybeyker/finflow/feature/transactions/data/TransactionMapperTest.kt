package com.hacybeyker.finflow.feature.transactions.data

import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.feature.transactions.data.local.TransactionEntity
import com.hacybeyker.finflow.feature.transactions.data.local.TransactionWithCategory
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
    fun `toEntity maps every field to primitive columns and keeps only the category id`() {
        val entity = domain.toEntity()

        assertEquals(7, entity.id)
        assertEquals(1599, entity.amountMinorUnits)
        assertEquals("EXPENSE", entity.type)
        assertEquals(3, entity.categoryId)
        assertEquals(LocalDate.of(2026, 6, 15).toEpochDay(), entity.epochDay)
        assertEquals("Weekly shop", entity.note)
    }

    @Test
    fun `toDomain rebuilds the domain model from a joined row`() {
        val row = TransactionWithCategory(transaction = domain.toEntity(), categoryName = "Groceries")

        assertEquals(domain, row.toDomain())
    }

    @Test
    fun `toDomain fails fast on an unknown transaction type`() {
        val corrupted = TransactionWithCategory(
            transaction = TransactionEntity(
                id = 7,
                amountMinorUnits = 1599,
                type = "TRANSFER",
                categoryId = 3,
                epochDay = LocalDate.of(2026, 6, 15).toEpochDay(),
                note = "Weekly shop"
            ),
            categoryName = "Groceries"
        )

        assertThrows(IllegalStateException::class.java) { corrupted.toDomain() }
    }
}

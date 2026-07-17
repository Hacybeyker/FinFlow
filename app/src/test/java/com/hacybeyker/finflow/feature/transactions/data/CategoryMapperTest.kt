package com.hacybeyker.finflow.feature.transactions.data

import com.hacybeyker.finflow.core.domain.Category
import com.hacybeyker.finflow.feature.transactions.data.local.CategoryEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryMapperTest {

    @Test
    fun `entity and domain round-trip preserves every field`() {
        val domain = Category(id = 5, name = "Groceries")

        assertEquals(domain, domain.toEntity().toDomain())
        assertEquals(CategoryEntity(id = 5, name = "Groceries"), domain.toEntity())
    }
}

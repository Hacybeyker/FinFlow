package com.hacybeyker.finflow.feature.transactions.domain.usecase

import com.hacybeyker.finflow.core.domain.Category
import com.hacybeyker.finflow.core.domain.FakeTransactionRepository
import com.hacybeyker.finflow.core.domain.TransactionType
import com.hacybeyker.finflow.core.domain.transaction
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ExportTransactionsCsvUseCaseTest {

    @Test
    fun `empty repository exports only the header`() = runTest {
        val useCase = ExportTransactionsCsvUseCase(FakeTransactionRepository())

        assertEquals("date,category,type,amount,note\n", useCase())
    }

    @Test
    fun `rows come out oldest first with signed decimal amounts`() = runTest {
        val useCase = ExportTransactionsCsvUseCase(
            FakeTransactionRepository(
                listOf(
                    transaction(
                        id = 2,
                        amount = 4550,
                        type = TransactionType.EXPENSE,
                        date = LocalDate.of(2026, 7, 2),
                        category = Category(id = 1, name = "Comida")
                    ),
                    transaction(
                        id = 1,
                        amount = 250000,
                        type = TransactionType.INCOME,
                        date = LocalDate.of(2026, 7, 1),
                        category = Category(id = 2, name = "Sueldo"),
                        note = "Julio"
                    )
                )
            )
        )

        assertEquals(
            """
            date,category,type,amount,note
            2026-07-01,Sueldo,INCOME,2500.00,Julio
            2026-07-02,Comida,EXPENSE,-45.50,

            """.trimIndent(),
            useCase()
        )
    }

    @Test
    fun `fields with commas quotes or line breaks are escaped per RFC 4180`() = runTest {
        val useCase = ExportTransactionsCsvUseCase(
            FakeTransactionRepository(
                listOf(
                    transaction(
                        id = 1,
                        amount = 100,
                        date = LocalDate.of(2026, 7, 3),
                        category = Category(id = 1, name = "Casa, jardín"),
                        note = "compra \"urgente\"\nsegunda línea"
                    )
                )
            )
        )

        assertEquals(
            "date,category,type,amount,note\n" +
                "2026-07-03,\"Casa, jardín\",EXPENSE,-1.00,\"compra \"\"urgente\"\"\nsegunda línea\"\n",
            useCase()
        )
    }
}

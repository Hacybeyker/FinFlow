package com.hacybeyker.finflow.feature.transactions.data

import com.hacybeyker.finflow.core.domain.Category
import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.core.domain.Transaction
import com.hacybeyker.finflow.core.domain.TransactionType
import com.hacybeyker.finflow.feature.transactions.data.local.TransactionEntity
import com.hacybeyker.finflow.feature.transactions.data.local.TransactionWithCategory
import java.time.LocalDate

fun TransactionWithCategory.toDomain(): Transaction = Transaction(
    id = transaction.id,
    amount = Money(transaction.amountMinorUnits),
    type = TransactionType.fromStorage(transaction.type),
    category = Category(id = transaction.categoryId, name = categoryName),
    date = LocalDate.ofEpochDay(transaction.epochDay),
    note = transaction.note
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    amountMinorUnits = amount.minorUnits,
    type = type.name,
    categoryId = category.id,
    epochDay = date.toEpochDay(),
    note = note
)

package com.hacybeyker.finflow.feature.transactions.data

import com.hacybeyker.finflow.core.domain.Money
import com.hacybeyker.finflow.feature.transactions.data.local.TransactionEntity
import com.hacybeyker.finflow.feature.transactions.domain.Category
import com.hacybeyker.finflow.feature.transactions.domain.Transaction
import com.hacybeyker.finflow.feature.transactions.domain.TransactionType
import java.time.LocalDate

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    amount = Money(amountMinorUnits),
    type = TransactionType.fromStorage(type),
    category = Category(id = categoryId, name = categoryName),
    date = LocalDate.ofEpochDay(epochDay),
    note = note
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    amountMinorUnits = amount.minorUnits,
    type = type.name,
    categoryId = category.id,
    categoryName = category.name,
    epochDay = date.toEpochDay(),
    note = note
)

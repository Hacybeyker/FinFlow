package com.hacybeyker.finflow.feature.transactions.data

import com.hacybeyker.finflow.feature.transactions.data.local.CategoryEntity
import com.hacybeyker.finflow.feature.transactions.domain.Category

fun CategoryEntity.toDomain(): Category = Category(id = id, name = name)

fun Category.toEntity(): CategoryEntity = CategoryEntity(id = id, name = name)

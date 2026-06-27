package com.hacybeyker.finflow.feature.transactions.data

import com.hacybeyker.finflow.core.domain.Category
import com.hacybeyker.finflow.feature.transactions.data.local.CategoryEntity

fun CategoryEntity.toDomain(): Category = Category(id = id, name = name)

fun Category.toEntity(): CategoryEntity = CategoryEntity(id = id, name = name)

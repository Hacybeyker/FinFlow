package com.hacybeyker.finflow.core.domain

/**
 * A user-defined grouping for transactions (e.g. "Groceries", "Salary").
 *
 * @property id persistence identifier; `0` means a category that has not been stored yet
 *  (the data layer assigns the real id on insert).
 * @property name human-readable label shown in the UI.
 */
data class Category(val id: Long = 0, val name: String)

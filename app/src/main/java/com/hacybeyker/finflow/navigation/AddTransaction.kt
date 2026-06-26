package com.hacybeyker.finflow.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** @property transactionId the row to edit, or `null` to create a new transaction. */
@Serializable
data class AddTransaction(val transactionId: Long? = null) : NavKey

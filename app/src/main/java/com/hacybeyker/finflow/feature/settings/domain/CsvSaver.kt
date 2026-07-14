package com.hacybeyker.finflow.feature.settings.domain

/**
 * Writes CSV content to a user-chosen destination. The destination travels as a plain string (the
 * SAF Uri serialized with `toString()`) so this contract — and the ViewModel that calls it — stays
 * free of Android types and unit-testable on the JVM.
 */
fun interface CsvSaver {
    /** @return `true` if the content was fully written. */
    suspend fun save(destinationUri: String, content: String): Boolean
}

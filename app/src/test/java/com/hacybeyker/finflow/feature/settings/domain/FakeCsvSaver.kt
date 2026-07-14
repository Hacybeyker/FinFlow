package com.hacybeyker.finflow.feature.settings.domain

/** Records what was saved (and where) and answers with a configurable outcome. */
class FakeCsvSaver(private val result: Boolean = true) : CsvSaver {

    var savedDestination: String? = null
        private set
    var savedContent: String? = null
        private set

    override suspend fun save(destinationUri: String, content: String): Boolean {
        savedDestination = destinationUri
        savedContent = content
        return result
    }
}

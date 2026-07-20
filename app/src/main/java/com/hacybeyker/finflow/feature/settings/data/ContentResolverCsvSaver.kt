package com.hacybeyker.finflow.feature.settings.data

import android.content.Context
import androidx.core.net.toUri
import com.hacybeyker.finflow.core.di.IoDispatcher
import com.hacybeyker.finflow.feature.settings.domain.CsvSaver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * [CsvSaver] over the document the user picked with SAF. Any failure (revoked grant, provider gone,
 * disk full) reports `false` instead of throwing: the caller turns it into a snackbar, never a crash.
 */
class ContentResolverCsvSaver @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CsvSaver {

    override suspend fun save(destinationUri: String, content: String): Boolean = withContext(ioDispatcher) {
        runCatching {
            val stream = requireNotNull(context.contentResolver.openOutputStream(destinationUri.toUri()))
            stream.use { it.write(content.toByteArray(Charsets.UTF_8)) }
        }.isSuccess
    }
}

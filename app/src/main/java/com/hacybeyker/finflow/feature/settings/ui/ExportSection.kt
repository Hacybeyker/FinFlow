package com.hacybeyker.finflow.feature.settings.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.hacybeyker.finflow.R
import com.hacybeyker.finflow.core.ui.theme.spacing
import java.time.LocalDate

@Composable
internal fun ExportSection(onExportTo: (String) -> Unit, modifier: Modifier = Modifier) {
    // SAF: the user picks where the file lands; the app needs no storage permission and only
    // receives a write grant for that single document.
    val createDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri -> uri?.let { onExportTo(it.toString()) } }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { createDocument.launch("finflow-movimientos-${LocalDate.now()}.csv") }
            .padding(horizontal = MaterialTheme.spacing.screen, vertical = MaterialTheme.spacing.sm)
    ) {
        Text(stringResource(R.string.settings_export_title), style = MaterialTheme.typography.bodyLarge)
        Text(
            text = stringResource(R.string.settings_export_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

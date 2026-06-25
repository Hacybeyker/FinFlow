package com.hacybeyker.finflow.feature.transactions.ui.categories

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hacybeyker.finflow.R
import com.hacybeyker.finflow.core.ui.theme.FinFlowTheme
import com.hacybeyker.finflow.core.ui.theme.spacing
import com.hacybeyker.finflow.feature.transactions.domain.Category

@Composable
fun CategoriesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CategoriesContent(
        uiState = uiState,
        onBack = onBack,
        onAdd = viewModel::openAdd,
        onRename = viewModel::openRename,
        onDelete = viewModel::openDelete,
        onDismissDialog = viewModel::dismissDialog,
        onSubmitAdd = viewModel::submitAdd,
        onSubmitRename = viewModel::submitRename,
        onConfirmDelete = viewModel::confirmDelete,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoriesContent(
    uiState: CategoriesUiState,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onRename: (Category) -> Unit,
    onDelete: (Category) -> Unit,
    onDismissDialog: () -> Unit,
    onSubmitAdd: (String) -> Unit,
    onSubmitRename: (Category, String) -> Unit,
    onConfirmDelete: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.categories_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.add_cancel)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.categories_add))
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(MaterialTheme.spacing.screen)
        ) {
            if (uiState.categories.isEmpty() && !uiState.isLoading) {
                Text(
                    text = stringResource(R.string.categories_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                    items(uiState.categories, key = { it.id }) { category ->
                        CategoryRow(category = category, onRename = onRename, onDelete = onDelete)
                    }
                }
            }
        }
    }

    CategoryDialogs(
        dialog = uiState.dialog,
        nameError = uiState.nameError,
        onDismiss = onDismissDialog,
        onSubmitAdd = onSubmitAdd,
        onSubmitRename = onSubmitRename,
        onConfirmDelete = onConfirmDelete
    )
}

@Composable
private fun CategoryDialogs(
    dialog: CategoryDialog,
    nameError: CategoryNameError?,
    onDismiss: () -> Unit,
    onSubmitAdd: (String) -> Unit,
    onSubmitRename: (Category, String) -> Unit,
    onConfirmDelete: (Category) -> Unit
) {
    when (dialog) {
        CategoryDialog.Hidden -> Unit
        CategoryDialog.Add -> CategoryNameDialog(
            title = stringResource(R.string.categories_add),
            initialName = "",
            error = nameError,
            onConfirm = onSubmitAdd,
            onDismiss = onDismiss
        )

        is CategoryDialog.Rename -> CategoryNameDialog(
            title = stringResource(R.string.categories_rename),
            initialName = dialog.category.name,
            error = nameError,
            onConfirm = { onSubmitRename(dialog.category, it) },
            onDismiss = onDismiss
        )

        is CategoryDialog.ConfirmDelete -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.categories_delete_title)) },
            text = { Text(stringResource(R.string.categories_delete_message, dialog.category.name)) },
            confirmButton = {
                TextButton(onClick = { onConfirmDelete(dialog.category) }) {
                    Text(stringResource(R.string.categories_delete))
                }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.add_cancel)) } }
        )
    }
}

@Composable
private fun CategoryRow(category: Category, onRename: (Category) -> Unit, onDelete: (Category) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = category.name,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .weight(1f)
                .clickable { onRename(category) }
                .padding(vertical = MaterialTheme.spacing.sm)
        )
        IconButton(onClick = { onDelete(category) }) {
            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.categories_delete))
        }
    }
}

@Composable
private fun CategoryNameDialog(
    title: String,
    initialName: String,
    error: CategoryNameError?,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.categories_name_label)) },
                    singleLine = true,
                    isError = error != null
                )
                error?.let {
                    Text(
                        text = stringResource(it.messageRes()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(name) }) { Text(stringResource(R.string.add_save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.add_cancel)) } }
    )
}

private fun CategoryNameError.messageRes(): Int = when (this) {
    CategoryNameError.INVALID -> R.string.categories_error_invalid
    CategoryNameError.DUPLICATE -> R.string.categories_error_duplicate
}

@Preview(name = "Categories · Light", showBackground = true)
@Preview(name = "Categories · Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CategoriesPreview() {
    FinFlowTheme {
        CategoriesContent(
            uiState = CategoriesUiState(
                categories = listOf(Category(1, "Comida"), Category(2, "Nómina"), Category(3, "Ocio")),
                isLoading = false
            ),
            onBack = {},
            onAdd = {},
            onRename = {},
            onDelete = {},
            onDismissDialog = {},
            onSubmitAdd = {},
            onSubmitRename = { _, _ -> },
            onConfirmDelete = {}
        )
    }
}

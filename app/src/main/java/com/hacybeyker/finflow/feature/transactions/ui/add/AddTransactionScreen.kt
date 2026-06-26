package com.hacybeyker.finflow.feature.transactions.ui.add

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hacybeyker.finflow.R
import com.hacybeyker.finflow.core.ui.theme.FinFlowTheme
import com.hacybeyker.finflow.core.ui.theme.spacing
import com.hacybeyker.finflow.feature.transactions.domain.Category
import com.hacybeyker.finflow.feature.transactions.domain.TransactionType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun AddTransactionScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    transactionId: Long? = null,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(transactionId) {
        if (transactionId != null) viewModel.onIntent(AddTransactionIntent.Load(transactionId))
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onDone()
    }

    AddTransactionContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onCancel = onDone,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionContent(
    uiState: AddTransactionUiState,
    onIntent: (AddTransactionIntent) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(if (uiState.isEditing) R.string.edit_title else R.string.add_title))
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.add_cancel)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        AddTransactionForm(
            uiState = uiState,
            onIntent = onIntent,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun AddTransactionForm(
    uiState: AddTransactionUiState,
    onIntent: (AddTransactionIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.screen)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
    ) {
        OutlinedTextField(
            value = uiState.amountInput,
            onValueChange = { onIntent(AddTransactionIntent.AmountChanged(it)) },
            label = { Text(stringResource(R.string.add_amount_label)) },
            singleLine = true,
            isError = uiState.error == AddTransactionError.INVALID_AMOUNT,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        TypeSelector(selected = uiState.type, onSelect = { onIntent(AddTransactionIntent.TypeChanged(it)) })

        CategoryField(
            categories = uiState.categories,
            selected = uiState.selectedCategory,
            onSelect = { onIntent(AddTransactionIntent.CategorySelected(it)) },
            onCreate = { onIntent(AddTransactionIntent.CreateCategory(it)) }
        )

        DateField(
            date = uiState.date,
            formatted = uiState.date.format(dateFormatter),
            onDateChange = { onIntent(AddTransactionIntent.DateChanged(it)) }
        )

        OutlinedTextField(
            value = uiState.note,
            onValueChange = { onIntent(AddTransactionIntent.NoteChanged(it)) },
            label = { Text(stringResource(R.string.add_note_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        uiState.error?.let { error ->
            Text(
                text = stringResource(error.messageRes()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = { onIntent(AddTransactionIntent.Save) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(if (uiState.isEditing) R.string.edit_save else R.string.add_save))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeSelector(
    selected: TransactionType,
    onSelect: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
        FilterChip(
            selected = selected == TransactionType.EXPENSE,
            onClick = { onSelect(TransactionType.EXPENSE) },
            label = { Text(stringResource(R.string.add_type_expense)) }
        )
        FilterChip(
            selected = selected == TransactionType.INCOME,
            onClick = { onSelect(TransactionType.INCOME) },
            label = { Text(stringResource(R.string.add_type_income)) }
        )
    }
}

@Composable
private fun CategoryField(
    categories: List<Category>,
    selected: Category?,
    onSelect: (Category) -> Unit,
    onCreate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }

    OutlinedButton(onClick = { showPicker = true }, modifier = modifier.fillMaxWidth()) {
        val label = stringResource(R.string.add_category_label)
        Text(if (selected != null) "$label: ${selected.name}" else label)
    }

    if (showPicker) {
        CategoryPickerDialog(
            categories = categories,
            onSelect = {
                onSelect(it)
                showPicker = false
            },
            onCreate = {
                onCreate(it)
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }
}

@Composable
private fun CategoryPickerDialog(
    categories: List<Category>,
    onSelect: (Category) -> Unit,
    onCreate: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newName by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_category_label)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)
            ) {
                categories.forEach { category ->
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(category) }
                            .padding(vertical = MaterialTheme.spacing.sm)
                    )
                }
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text(stringResource(R.string.add_category_new)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onCreate(newName) }, enabled = newName.isNotBlank()) {
                Text(stringResource(R.string.add_category_create))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.add_cancel)) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    date: LocalDate,
    formatted: String,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }

    OutlinedButton(onClick = { showPicker = true }, modifier = modifier.fillMaxWidth()) {
        Text("${stringResource(R.string.add_date_label)}: $formatted")
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onDateChange(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    showPicker = false
                }) { Text(stringResource(R.string.add_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text(stringResource(R.string.add_cancel)) }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

private fun AddTransactionError.messageRes(): Int = when (this) {
    AddTransactionError.INVALID_AMOUNT -> R.string.add_error_invalid_amount
    AddTransactionError.INVALID_CATEGORY -> R.string.add_error_invalid_category
}

@Preview(name = "Add · Light", showBackground = true)
@Preview(name = "Add · Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AddTransactionPreview() {
    FinFlowTheme {
        AddTransactionContent(
            uiState = AddTransactionUiState(
                amountInput = "45.50",
                categories = listOf(Category(1, "Comida"), Category(2, "Ocio")),
                selectedCategory = Category(1, "Comida"),
                date = LocalDate.of(2026, 6, 22)
            ),
            onIntent = {},
            onCancel = {}
        )
    }
}

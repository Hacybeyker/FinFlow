package com.hacybeyker.finflow.feature.transactions.ui.add

import android.content.res.Configuration
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
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                title = { Text(stringResource(R.string.add_title)) },
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

        OutlinedTextField(
            value = uiState.category,
            onValueChange = { onIntent(AddTransactionIntent.CategoryChanged(it)) },
            label = { Text(stringResource(R.string.add_category_label)) },
            singleLine = true,
            isError = uiState.error == AddTransactionError.INVALID_CATEGORY,
            modifier = Modifier.fillMaxWidth()
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
            Text(stringResource(R.string.add_save))
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
                category = "Compras",
                date = LocalDate.of(2026, 6, 22)
            ),
            onIntent = {},
            onCancel = {}
        )
    }
}

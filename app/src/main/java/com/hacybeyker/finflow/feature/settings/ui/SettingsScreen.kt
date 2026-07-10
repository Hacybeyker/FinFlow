package com.hacybeyker.finflow.feature.settings.ui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hacybeyker.finflow.R
import com.hacybeyker.finflow.core.domain.ThemeMode
import com.hacybeyker.finflow.core.domain.UserPreferences
import com.hacybeyker.finflow.core.ui.theme.FinFlowTheme
import com.hacybeyker.finflow.core.ui.theme.spacing
import java.time.LocalTime
import java.util.Currency
import java.util.Locale

/**
 * Currencies offered in the picker, besides "device default". A short curated list keeps the dialog
 * scannable; the repository accepts any valid ISO 4217 code, so growing this later costs nothing.
 */
private val SUPPORTED_CURRENCIES = listOf("PEN", "USD", "EUR", "MXN", "COP", "CLP", "ARS", "BRL", "GBP", "JPY")

@Composable
fun SettingsScreen(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(
        uiState = uiState,
        onBack = onBack,
        onThemeModeSelected = viewModel::setThemeMode,
        onCurrencySelected = viewModel::setCurrency,
        onAppLockChanged = viewModel::setAppLockEnabled,
        onReminderChanged = viewModel::setReminderEnabled,
        onReminderTimeChanged = viewModel::setReminderTime,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onCurrencySelected: (String?) -> Unit,
    onAppLockChanged: (Boolean) -> Unit,
    onReminderChanged: (Boolean) -> Unit,
    onReminderTimeChanged: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(vertical = MaterialTheme.spacing.sm)
        ) {
            SectionTitle(stringResource(R.string.settings_section_appearance))
            ThemeSection(selected = uiState.preferences.themeMode, onSelect = onThemeModeSelected)

            SectionTitle(stringResource(R.string.settings_section_currency))
            CurrencySection(selectedCode = uiState.preferences.currencyCode, onSelect = onCurrencySelected)

            SectionTitle(stringResource(R.string.settings_section_security))
            SecuritySection(enabled = uiState.preferences.appLockEnabled, onChange = onAppLockChanged)

            SectionTitle(stringResource(R.string.settings_section_reminder))
            ReminderSection(
                enabled = uiState.preferences.reminderEnabled,
                time = uiState.preferences.reminderTime,
                onEnabledChange = onReminderChanged,
                onTimeChange = onReminderTimeChanged
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(
            start = MaterialTheme.spacing.screen,
            end = MaterialTheme.spacing.screen,
            top = MaterialTheme.spacing.md,
            bottom = MaterialTheme.spacing.xs
        )
    )
}

@Composable
private fun ThemeSection(selected: ThemeMode, onSelect: (ThemeMode) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.selectableGroup()) {
        ThemeMode.entries.forEach { mode ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = mode == selected, role = Role.RadioButton) { onSelect(mode) }
                    .padding(horizontal = MaterialTheme.spacing.screen, vertical = MaterialTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = mode == selected, onClick = null)
                Text(
                    text = stringResource(mode.labelRes()),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = MaterialTheme.spacing.md)
                )
            }
        }
    }
}

@Composable
private fun CurrencySection(selectedCode: String?, onSelect: (String?) -> Unit, modifier: Modifier = Modifier) {
    var showPicker by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showPicker = true }
            .padding(horizontal = MaterialTheme.spacing.screen, vertical = MaterialTheme.spacing.sm)
    ) {
        Text(
            text = selectedCode?.let { currencyLabel(it) } ?: stringResource(R.string.settings_currency_device),
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = stringResource(R.string.settings_currency_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (showPicker) {
        CurrencyPickerDialog(
            selectedCode = selectedCode,
            onSelect = { code ->
                onSelect(code)
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }
}

@Composable
private fun CurrencyPickerDialog(selectedCode: String?, onSelect: (String?) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_section_currency)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()).selectableGroup()) {
                CurrencyOption(
                    label = stringResource(R.string.settings_currency_device),
                    selected = selectedCode == null
                ) { onSelect(null) }
                SUPPORTED_CURRENCIES.forEach { code ->
                    CurrencyOption(label = currencyLabel(code), selected = code == selectedCode) { onSelect(code) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.settings_currency_cancel)) }
        }
    )
}

@Composable
private fun CurrencyOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = MaterialTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = MaterialTheme.spacing.md)
        )
    }
}

@Composable
private fun SecuritySection(enabled: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.screen, vertical = MaterialTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.settings_lock_title), style = MaterialTheme.typography.bodyLarge)
            Text(
                text = stringResource(R.string.settings_lock_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = enabled, onCheckedChange = onChange)
    }
}

private fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.settings_theme_system
    ThemeMode.LIGHT -> R.string.settings_theme_light
    ThemeMode.DARK -> R.string.settings_theme_dark
}

/** "USD — US Dollar" in the device language; falls back to the raw code for unknown currencies. */
private fun currencyLabel(code: String): String =
    runCatching { "$code — ${Currency.getInstance(code).getDisplayName(Locale.getDefault())}" }.getOrDefault(code)

@Preview(name = "Settings · Light", showBackground = true)
@Preview(name = "Settings · Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsPreview() {
    FinFlowTheme {
        SettingsContent(
            uiState = SettingsUiState(
                preferences = UserPreferences(themeMode = ThemeMode.DARK, currencyCode = "PEN"),
                isLoading = false
            ),
            onBack = {},
            onThemeModeSelected = {},
            onCurrencySelected = {},
            onAppLockChanged = {},
            onReminderChanged = {},
            onReminderTimeChanged = {}
        )
    }
}

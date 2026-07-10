package com.hacybeyker.finflow

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hacybeyker.finflow.core.ui.format.moneyFormatterFor
import com.hacybeyker.finflow.core.ui.theme.FinFlowTheme
import com.hacybeyker.finflow.feature.security.ui.AppLock
import com.hacybeyker.finflow.feature.settings.domain.ThemeMode
import com.hacybeyker.finflow.feature.settings.ui.SettingsViewModel
import com.hacybeyker.finflow.navigation.FinFlowNavHost
import dagger.hilt.android.AndroidEntryPoint

// FragmentActivity (not ComponentActivity) because androidx.biometric's BiometricPrompt requires it.
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Activity-scoped instance: the root reacts to settings (theme, currency, lock toggle);
            // the Settings screen edits them through its own nav-scoped instance of the same VM.
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
            val preferences = uiState.preferences

            val darkTheme = when (preferences.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            val moneyFormatter = remember(preferences.currencyCode) {
                moneyFormatterFor(preferences.currencyCode)
            }

            FinFlowTheme(darkTheme = darkTheme, moneyFormatter = moneyFormatter) {
                if (uiState.isLoading) {
                    // Don't compose the lock gate until DataStore emits: with the default we could
                    // prompt a user who disabled the lock (or skip one flash of the wrong theme).
                    Surface(modifier = Modifier.fillMaxSize()) {}
                } else {
                    AppLock(enabled = preferences.appLockEnabled) {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            FinFlowNavHost(modifier = Modifier.padding(paddingValues = innerPadding))
                        }
                    }
                }
            }
        }
    }
}

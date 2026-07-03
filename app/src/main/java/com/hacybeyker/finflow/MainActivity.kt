package com.hacybeyker.finflow

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.hacybeyker.finflow.core.ui.theme.FinFlowTheme
import com.hacybeyker.finflow.feature.security.ui.AppLock
import com.hacybeyker.finflow.navigation.FinFlowNavHost
import dagger.hilt.android.AndroidEntryPoint

// FragmentActivity (not ComponentActivity) because androidx.biometric's BiometricPrompt requires it.
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinFlowTheme {
                AppLock {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        FinFlowNavHost(modifier = Modifier.padding(paddingValues = innerPadding))
                    }
                }
            }
        }
    }
}

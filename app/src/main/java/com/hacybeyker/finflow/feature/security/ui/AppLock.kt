package com.hacybeyker.finflow.feature.security.ui

import android.content.res.Configuration
import android.os.SystemClock
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.hacybeyker.finflow.R
import com.hacybeyker.finflow.core.ui.theme.FinFlowTheme
import com.hacybeyker.finflow.core.ui.theme.spacing

/**
 * Gates [content] behind a biometric/device-credential prompt. While locked it shows [LockScreen] and
 * nothing of the app is composed; once authenticated, [content] replaces it until the app spends more
 * than the grace period in background, which re-arms the lock (see [SecurityViewModel]).
 *
 * [enabled] comes from the user's settings: when off, [content] composes directly. The caller must
 * only pass a **loaded** preference (never a default while DataStore is still reading), otherwise a
 * user who disabled the lock would get prompted on cold start.
 */
@Composable
fun AppLock(modifier: Modifier = Modifier, enabled: Boolean = true, content: @Composable () -> Unit) {
    if (!enabled) {
        content()
        return
    }
    val viewModel: SecurityViewModel = hiltViewModel()
    RelockOnBackground(viewModel)
    if (viewModel.unlocked) {
        content()
    } else {
        LockScreen(onUnlocked = viewModel::markUnlocked, modifier = modifier)
    }
}

/** Feeds ON_STOP/ON_START into the ViewModel so it can re-lock after a long stay in background. */
@Composable
private fun RelockOnBackground(viewModel: SecurityViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> viewModel.onAppBackgrounded(SystemClock.elapsedRealtime())
                Lifecycle.Event.ON_START -> viewModel.onAppForegrounded(SystemClock.elapsedRealtime())
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

@Composable
private fun LockScreen(onUnlocked: () -> Unit, modifier: Modifier = Modifier) {
    val activity = LocalContext.current.findFragmentActivity()
    val authenticate: () -> Unit = {
        activity?.authenticateToUnlock(onSuccess = onUnlocked, onFailure = {}) ?: onUnlocked()
    }
    // Prompt automatically on first show; the button is the retry path after a cancel/error.
    LaunchedEffect(Unit) { authenticate() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.screen),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(LOCK_ICON_SIZE.dp)
        )
        Text(
            text = stringResource(R.string.lock_locked_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = MaterialTheme.spacing.md)
        )
        Button(onClick = authenticate, modifier = Modifier.padding(top = MaterialTheme.spacing.lg)) {
            Text(stringResource(R.string.lock_unlock_button))
        }
    }
}

private const val LOCK_ICON_SIZE = 64

@Preview(name = "Lock · Light", showBackground = true)
@Preview(name = "Lock · Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LockScreenPreview() {
    FinFlowTheme {
        LockScreen(onUnlocked = {})
    }
}

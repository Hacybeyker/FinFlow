package com.hacybeyker.finflow.feature.security.ui

import android.app.KeyguardManager
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.fragment.app.FragmentActivity
import com.hacybeyker.finflow.R

/**
 * App-entry lock. We don't bind the SQLCipher key to biometrics (that would lose data on fingerprint
 * re-enrollment); this is purely a door in front of the UI, so no `CryptoObject` is used.
 *
 * Falls back to the device PIN/pattern when no biometric is enrolled. The strong/weak split exists
 * because `BIOMETRIC_STRONG or DEVICE_CREDENTIAL` is only valid from API 30; below that the same combo
 * with STRONG throws, so we use WEAK there (safe without a CryptoObject).
 *
 * Fail-open is gated on [KeyguardManager.isDeviceSecure] — the only state with truly nothing to
 * authenticate against (the data stays encrypted at rest regardless). We deliberately do NOT gate on
 * `BiometricManager.canAuthenticate`: it reports non-success for transient states (sensor busy) and
 * `STATUS_UNKNOWN` on API < 30 for DEVICE_CREDENTIAL combos, all of which must still show the prompt.
 * Any real failure surfaces through `onAuthenticationError`, which keeps the app locked.
 */
fun FragmentActivity.authenticateToUnlock(onSuccess: () -> Unit, onFailure: () -> Unit) {
    if (getSystemService<KeyguardManager>()?.isDeviceSecure != true) {
        onSuccess()
        return
    }
    val prompt = BiometricPrompt(
        this,
        ContextCompat.getMainExecutor(this),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onSuccess()

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = onFailure()
        }
    )
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle(getString(R.string.lock_title))
        .setSubtitle(getString(R.string.lock_subtitle))
        .setAllowedAuthenticators(allowedAuthenticators())
        .build()
    prompt.authenticate(info)
}

private fun allowedAuthenticators(): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    BIOMETRIC_STRONG or DEVICE_CREDENTIAL
} else {
    BIOMETRIC_WEAK or DEVICE_CREDENTIAL
}

/** Unwraps the Compose `LocalContext` to the hosting [FragmentActivity], or null if there isn't one. */
fun Context.findFragmentActivity(): FragmentActivity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is FragmentActivity) return current
        current = current.baseContext
    }
    return null
}

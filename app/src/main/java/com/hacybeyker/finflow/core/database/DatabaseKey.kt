package com.hacybeyker.finflow.core.database

import android.content.Context
import android.util.Base64
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the SQLCipher passphrase. A 256-bit random key is generated once and kept in
 * [EncryptedSharedPreferences] — encrypted at rest by a [MasterKey] held in the Android Keystore, so
 * the passphrase never lives in plaintext on disk and never leaves the device. Both files are
 * excluded from Auto Backup (see `backup_rules.xml`): a restored copy would be undecryptable anyway
 * because the Keystore key stays on the original device.
 *
 * The biometric prompt gates *entry to the app*; it does not derive this key. That keeps the threat
 * model simple and avoids losing data when the user re-enrolls a fingerprint.
 *
 * Critical writes use synchronous `commit()`, not `apply()`: the encrypted DB is created with this
 * passphrase right after, so an async write lost to process death would leave a database no future
 * key can open (or, for the purge flag, re-delete the already-encrypted database).
 */
@Singleton
class DatabaseKey @Inject constructor(@ApplicationContext private val context: Context) {

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            SECURE_PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Pre-creates the Keystore-backed prefs and the passphrase. Called from a background thread at
     * app start so the first DAO injection (main thread) finds everything ready and pays ~nothing.
     */
    fun warmUp() {
        passphrase()
    }

    /**
     * Returns the passphrase bytes. sqlcipher-android keeps the array it receives (it does NOT zero
     * it after opening, unlike the legacy `android-database-sqlcipher` SupportFactory), so the same
     * bytes remain valid for any later re-open.
     */
    @Synchronized
    fun passphrase(): ByteArray {
        prefs.getString(KEY_PASSPHRASE, null)?.let { return Base64.decode(it, Base64.NO_WRAP) }
        val generated = ByteArray(PASSPHRASE_BYTES).also { SecureRandom().nextBytes(it) }
        prefs.edit(commit = true) { putString(KEY_PASSPHRASE, Base64.encodeToString(generated, Base64.NO_WRAP)) }
        return generated
    }

    /**
     * One-time removal of the pre-encryption plaintext database. The chosen rollout recreates an empty
     * encrypted DB (no in-place migration), so the legacy file is deleted exactly once and the new
     * encrypted file then persists normally.
     */
    fun purgeLegacyPlaintextDbOnce(databaseName: String) {
        if (prefs.getBoolean(KEY_LEGACY_PURGED, false)) return
        context.deleteDatabase(databaseName)
        prefs.edit(commit = true) { putBoolean(KEY_LEGACY_PURGED, true) }
    }

    private companion object {
        const val SECURE_PREFS_FILE = "finflow_secure_prefs"
        const val KEY_PASSPHRASE = "db_passphrase"
        const val KEY_LEGACY_PURGED = "legacy_plaintext_purged"
        const val PASSPHRASE_BYTES = 32
    }
}

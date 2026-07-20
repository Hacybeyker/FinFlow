package com.hacybeyker.finflow.core.database

import android.content.Context
import android.util.Base64
import androidx.core.content.edit
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the SQLCipher passphrase. A 256-bit random key is generated once and kept, AES-256-GCM
 * encrypted, in a plain [android.content.SharedPreferences] file — the encryption key itself lives in
 * the Android Keystore via Tink's [AndroidKeysetManager], so the passphrase never lives in plaintext
 * on disk and never leaves the device. Both files are excluded from Auto Backup (see
 * `backup_rules.xml`): a restored copy would be undecryptable anyway because the Keystore key stays on
 * the original device.
 *
 * Built directly on Tink (not `androidx.security-crypto`'s `MasterKey`/`EncryptedSharedPreferences`,
 * which are themselves thin wrappers over Tink and are `@Deprecated` without a published replacement).
 * Tink is not deprecated and is already a transitive dependency of this project through that same
 * library — this uses it directly instead of through a deprecated wrapper.
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

    private val aead: Aead by lazy {
        AeadConfig.register()
        val keysetManager = AndroidKeysetManager.Builder()
            .withSharedPref(context, KEYSET_NAME, KEYSET_PREFS_FILE)
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
        keysetManager.keysetHandle.getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    }

    private val prefs by lazy { context.getSharedPreferences(SECURE_PREFS_FILE, Context.MODE_PRIVATE) }

    /**
     * Pre-creates the Keystore-backed keyset and the passphrase. Called from a background thread at
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
        prefs.getString(KEY_PASSPHRASE, null)?.let { stored ->
            return aead.decrypt(Base64.decode(stored, Base64.NO_WRAP), PASSPHRASE_AAD)
        }
        val generated = ByteArray(PASSPHRASE_BYTES).also { SecureRandom().nextBytes(it) }
        val ciphertext = aead.encrypt(generated, PASSPHRASE_AAD)
        prefs.edit(commit = true) { putString(KEY_PASSPHRASE, Base64.encodeToString(ciphertext, Base64.NO_WRAP)) }
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
        const val KEYSET_NAME = "finflow_tink_keyset"
        const val KEYSET_PREFS_FILE = "finflow_tink_keyset_prefs"
        const val MASTER_KEY_URI = "android-keystore://finflow_master_key"
        const val SECURE_PREFS_FILE = "finflow_secure_prefs"
        const val KEY_PASSPHRASE = "db_passphrase"
        const val KEY_LEGACY_PURGED = "legacy_plaintext_purged"
        const val PASSPHRASE_BYTES = 32

        // Binds the ciphertext to its purpose (domain separation), not a secret in itself.
        val PASSPHRASE_AAD = "finflow_db_passphrase".toByteArray(Charsets.UTF_8)
    }
}

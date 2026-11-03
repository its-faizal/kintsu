package com.kintsu.app.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import java.security.SecureRandom
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

data class SecureBackupInfo(
    val exists: Boolean,
    val lastModifiedMillis: Long,
    val fileSizeBytes: Long,
    val isEncrypted: Boolean = true
)

object SecureEncryptedBackupManager {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "kintsu_secure_backup_key"
    private const val GCM_IV_LENGTH_BYTES = 12
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val BACKUP_DIR = "secure_backups"
    private const val BACKUP_FILE_NAME = "kintsu_secure_backup.enc"

    @Synchronized
    private fun getOrCreateSecretKey(context: Context): SecretKey {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (keyStore.containsAlias(KEY_ALIAS)) {
                val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
                entry?.secretKey ?: generateKeystoreKey()
            } else {
                generateKeystoreKey()
            }
        } catch (e: Exception) {
            getFallbackSecretKey(context)
        }
    }

    private fun generateKeystoreKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun getFallbackSecretKey(context: Context): SecretKey {
        val secretFile = File(context.filesDir, ".kintsu_backup_sec")
        val keyBytes = if (secretFile.exists() && secretFile.length() == 32L) {
            secretFile.readBytes()
        } else {
            val random = ByteArray(32)
            SecureRandom().nextBytes(random)
            secretFile.writeBytes(random)
            random
        }
        return SecretKeySpec(keyBytes, "AES")
    }

    fun compressGzip(input: ByteArray): ByteArray {
        val byteOut = ByteArrayOutputStream()
        GZIPOutputStream(byteOut).use { gzip ->
            gzip.write(input)
            gzip.finish()
        }
        return byteOut.toByteArray()
    }

    fun decompressGzip(input: ByteArray): ByteArray {
        val byteIn = ByteArrayInputStream(input)
        val byteOut = ByteArrayOutputStream()
        GZIPInputStream(byteIn).use { gzip ->
            gzip.copyTo(byteOut)
        }
        return byteOut.toByteArray()
    }

    fun encryptAesGcm(data: ByteArray, secretKey: SecretKey): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        // Android KeyStore strictly forbids caller-provided IVs when setRandomizedEncryptionRequired is true.
        // We let the cipher provider generate a secure random 12-byte IV upon init(Cipher.ENCRYPT_MODE, secretKey).
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv ?: error("Cipher failed to generate an initialization vector")
        require(iv.size == GCM_IV_LENGTH_BYTES) { "Invalid IV size generated: ${iv.size}" }

        val cipherText = cipher.doFinal(data)
        val combined = ByteArray(iv.size + cipherText.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)
        return combined
    }

    fun decryptAesGcm(data: ByteArray, secretKey: SecretKey): ByteArray {
        require(data.size > GCM_IV_LENGTH_BYTES) { "Corrupt encrypted backup: size too small" }
        val iv = ByteArray(GCM_IV_LENGTH_BYTES)
        System.arraycopy(data, 0, iv, 0, GCM_IV_LENGTH_BYTES)

        val cipherTextSize = data.size - GCM_IV_LENGTH_BYTES
        val cipherText = ByteArray(cipherTextSize)
        System.arraycopy(data, GCM_IV_LENGTH_BYTES, cipherText, 0, cipherTextSize)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return cipher.doFinal(cipherText)
    }

    /**
     * Serializes database, compresses with GZIP, encrypts with AES-256-GCM,
     * and stores in app-private storage strictly accessible to Kintsu.
     */
    suspend fun performAutoBackup(
        context: Context,
        database: KintsuDatabase,
        settings: KintsuSettings
    ): Result<SecureBackupInfo> = withContext(Dispatchers.IO) {
        try {
            val jsonString = BackupRestoreManager.createBackupJson(database, settings)
            val jsonBytes = jsonString.toByteArray(Charsets.UTF_8)
            val compressedBytes = compressGzip(jsonBytes)
            val secretKey = getOrCreateSecretKey(context)
            val encryptedBytes = encryptAesGcm(compressedBytes, secretKey)

            val backupDir = File(context.filesDir, BACKUP_DIR)
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            val targetFile = File(backupDir, BACKUP_FILE_NAME)
            val tempFile = File(backupDir, "$BACKUP_FILE_NAME.tmp")
            FileOutputStream(tempFile).use { out ->
                out.write(encryptedBytes)
                out.flush()
            }
            if (targetFile.exists()) {
                targetFile.delete()
            }
            tempFile.renameTo(targetFile)

            Result.success(
                SecureBackupInfo(
                    exists = true,
                    lastModifiedMillis = targetFile.lastModified(),
                    fileSizeBytes = targetFile.length(),
                    isEncrypted = true
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreFromEncryptedBackup(
        context: Context,
        database: KintsuDatabase
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val backupDir = File(context.filesDir, BACKUP_DIR)
            val backupFile = File(backupDir, BACKUP_FILE_NAME)
            if (!backupFile.exists() || backupFile.length() == 0L) {
                return@withContext Result.failure(IllegalStateException("No encrypted backup found"))
            }

            val encryptedBytes = FileInputStream(backupFile).use { it.readBytes() }
            val secretKey = getOrCreateSecretKey(context)
            val compressedBytes = decryptAesGcm(encryptedBytes, secretKey)
            val jsonBytes = decompressGzip(compressedBytes)
            val jsonString = String(jsonBytes, Charsets.UTF_8)

            val validation = BackupRestoreManager.validateBackup(jsonString)
            if (validation.isValid && validation.data != null) {
                BackupRestoreManager.applyBackup(database, validation.data, ImportMode.REPLACE)
                Result.success(1)
            } else {
                Result.failure(Exception(validation.errorMessage ?: "Backup integrity verification failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getBackupInfo(context: Context): SecureBackupInfo {
        val backupDir = File(context.filesDir, BACKUP_DIR)
        val backupFile = File(backupDir, BACKUP_FILE_NAME)
        return if (backupFile.exists() && backupFile.length() > 0) {
            SecureBackupInfo(
                exists = true,
                lastModifiedMillis = backupFile.lastModified(),
                fileSizeBytes = backupFile.length(),
                isEncrypted = true
            )
        } else {
            SecureBackupInfo(
                exists = false,
                lastModifiedMillis = 0L,
                fileSizeBytes = 0L,
                isEncrypted = true
            )
        }
    }
}

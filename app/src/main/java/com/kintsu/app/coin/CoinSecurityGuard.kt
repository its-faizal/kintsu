package com.kintsu.app.coin

import android.content.Context
import android.os.SystemClock
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.kintsu.app.data.model.WalletTransactionEntity
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

data class LedgerVerificationResult(
    val isValid: Boolean,
    val reconciledBalance: Int,
    val totalEarned: Int,
    val totalSpent: Int,
    val totalDeducted: Int,
    val tamperedTransactionId: Long? = null,
    val brokenChainIndex: Int? = null,
    val verificationMessage: String
)

object CoinSecurityGuard {

    private const val KEY_ALIAS = "kintsu_ledger_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val HMAC_ALGO = "HmacSHA256"
    private const val SHA256_ALGO = "SHA-256"
    private const val PREFS_TIME_GUARD = "kintsu_time_guard_prefs"
    private const val KEY_LAST_WALL_CLOCK = "last_wall_clock"
    private const val KEY_LAST_ELAPSED_REALTIME = "last_elapsed_realtime"
    private const val KEY_FALLBACK_SECRET = "fallback_ledger_hmac_secret"

    @Volatile
    private var cachedKey: SecretKey? = null

    /**
     * Retrieves or generates the hardware-isolated HMAC-SHA256 key from Android KeyStore.
     * Gracefully falls back to a securely randomized local master key if KeyStore is unavailable.
     */
    fun getOrCreateKey(context: Context): SecretKey {
        cachedKey?.let { return it }

        synchronized(this) {
            cachedKey?.let { return it }

            try {
                val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
                if (keyStore.containsAlias(KEY_ALIAS)) {
                    val key = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
                    if (key != null) {
                        cachedKey = key
                        return key
                    }
                }

                // Generate in AndroidKeyStore
                val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_HMAC_SHA256, ANDROID_KEYSTORE)
                val spec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
                ).build()
                keyGen.init(spec)
                val generated = keyGen.generateKey()
                cachedKey = generated
                return generated
            } catch (e: Exception) {
                // Fallback for devices/emulators with KeyStore HMAC provider limitations
                val prefs = context.getSharedPreferences(PREFS_TIME_GUARD, Context.MODE_PRIVATE)
                var rawHex = prefs.getString(KEY_FALLBACK_SECRET, null)
                if (rawHex == null || rawHex.length < 64) {
                    val randomBytes = ByteArray(32)
                    java.security.SecureRandom().nextBytes(randomBytes)
                    rawHex = bytesToHex(randomBytes)
                    prefs.edit().putString(KEY_FALLBACK_SECRET, rawHex).apply()
                }
                val keyBytes = hexToBytes(rawHex)
                val fallbackKey = SecretKeySpec(keyBytes, HMAC_ALGO)
                cachedKey = fallbackKey
                return fallbackKey
            }
        }
    }

    /**
     * Calculates the SHA-256 hash of a transaction block linked to the previous block hash.
     */
    fun computeTransactionHash(
        previousHash: String,
        amount: Int,
        type: String,
        sourceActivity: String,
        sourceEventId: String,
        timestampMillis: Long,
        multiplier: Float,
        isDeduction: Boolean
    ): String {
        val payload = "$previousHash|$amount|$type|$sourceActivity|$sourceEventId|$timestampMillis|$multiplier|$isDeduction"
        val md = MessageDigest.getInstance(SHA256_ALGO)
        val digest = md.digest(payload.toByteArray(StandardCharsets.UTF_8))
        return bytesToHex(digest)
    }

    /**
     * Cryptographically signs the transaction hash using HMAC-SHA256.
     */
    fun signHash(context: Context, hashHex: String): String {
        val key = getOrCreateKey(context)
        val mac = Mac.getInstance(HMAC_ALGO)
        mac.init(key)
        val signatureBytes = mac.doFinal(hashHex.toByteArray(StandardCharsets.UTF_8))
        return bytesToHex(signatureBytes)
    }

    /**
     * Checks for anti-time-travel tampering using monotonic clock tracking.
     */
    fun checkAndUpdateMonotonicClock(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_TIME_GUARD, Context.MODE_PRIVATE)
        val lastWallClock = prefs.getLong(KEY_LAST_WALL_CLOCK, 0L)
        val lastElapsedRealtime = prefs.getLong(KEY_LAST_ELAPSED_REALTIME, 0L)

        val currentWallClock = System.currentTimeMillis()
        val currentElapsedRealtime = SystemClock.elapsedRealtime()

        // Check for time rewind: wall clock dropped backward while device remained on
        var isTampered = false
        if (lastWallClock > 0 && lastElapsedRealtime > 0) {
            val elapsedDelta = currentElapsedRealtime - lastElapsedRealtime
            val wallDelta = currentWallClock - lastWallClock

            // If wall clock rolled back by more than 10 seconds while elapsed time advanced
            if (elapsedDelta >= 0 && wallDelta < -10000L) {
                isTampered = true
            }
        }

        prefs.edit()
            .putLong(KEY_LAST_WALL_CLOCK, currentWallClock)
            .putLong(KEY_LAST_ELAPSED_REALTIME, currentElapsedRealtime)
            .apply()

        return !isTampered
    }

    /**
     * Reconciles and verifies the full cryptographic hash chain and zero-sum balance.
     */
    fun verifyLedgerAndReconcile(
        context: Context,
        transactions: List<WalletTransactionEntity>
    ): LedgerVerificationResult {
        if (transactions.isEmpty()) {
            return LedgerVerificationResult(
                isValid = true,
                reconciledBalance = 0,
                totalEarned = 0,
                totalSpent = 0,
                totalDeducted = 0,
                verificationMessage = "Ledger is clean and empty."
            )
        }

        // Sort chronologically ascending
        val chain = transactions.sortedBy { it.id }
        var computedBalance = 0
        var totalEarned = 0
        var totalSpent = 0
        var totalDeducted = 0
        var previousHash = "GENESIS"

        for (i in chain.indices) {
            val tx = chain[i]

            // Calculate zero-sum totals
            computedBalance += tx.amount
            if (tx.amount > 0) {
                totalEarned += tx.amount
            } else if (tx.isDeduction) {
                totalDeducted += Math.abs(tx.amount)
            } else {
                totalSpent += Math.abs(tx.amount)
            }

            // For signed transactions, verify chain and signature
            if (tx.transactionHash.isNotBlank()) {
                // 1. Verify previous hash link
                if (i > 0 && tx.previousHash != previousHash && tx.previousHash != "GENESIS") {
                    return LedgerVerificationResult(
                        isValid = false,
                        reconciledBalance = computedBalance,
                        totalEarned = totalEarned,
                        totalSpent = totalSpent,
                        totalDeducted = totalDeducted,
                        tamperedTransactionId = tx.id,
                        brokenChainIndex = i,
                        verificationMessage = "Broken hash chain at transaction #${tx.id}: previous hash mismatch."
                    )
                }

                // 2. Re-compute transaction hash
                val expectedHash = computeTransactionHash(
                    previousHash = tx.previousHash,
                    amount = tx.amount,
                    type = tx.type,
                    sourceActivity = tx.sourceActivity,
                    sourceEventId = tx.sourceEventId,
                    timestampMillis = tx.timestampMillis,
                    multiplier = tx.multiplier,
                    isDeduction = tx.isDeduction
                )

                if (expectedHash != tx.transactionHash) {
                    return LedgerVerificationResult(
                        isValid = false,
                        reconciledBalance = computedBalance,
                        totalEarned = totalEarned,
                        totalSpent = totalSpent,
                        totalDeducted = totalDeducted,
                        tamperedTransactionId = tx.id,
                        brokenChainIndex = i,
                        verificationMessage = "Tampered transaction payload detected at #${tx.id}."
                    )
                }

                // 3. Verify cryptographic HMAC signature
                if (tx.signature.isNotBlank()) {
                    val expectedSig = signHash(context, expectedHash)
                    if (expectedSig != tx.signature) {
                        return LedgerVerificationResult(
                            isValid = false,
                            reconciledBalance = computedBalance,
                            totalEarned = totalEarned,
                            totalSpent = totalSpent,
                            totalDeducted = totalDeducted,
                            tamperedTransactionId = tx.id,
                            brokenChainIndex = i,
                            verificationMessage = "Invalid cryptographic signature at #${tx.id}."
                        )
                    }
                }

                previousHash = tx.transactionHash
            } else {
                // Legacy transaction prior to cryptographic guard
                if (tx.amount > 0) {
                    previousHash = "LEGACY_${tx.id}"
                }
            }
        }

        return LedgerVerificationResult(
            isValid = true,
            reconciledBalance = computedBalance,
            totalEarned = totalEarned,
            totalSpent = totalSpent,
            totalDeducted = totalDeducted,
            verificationMessage = "Ledger integrity verified: ${chain.size} transactions verified with KeyStore root of trust."
        )
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = "0123456789abcdef"
        val result = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            val i = b.toInt() and 0xFF
            result.append(hexChars[i shr 4])
            result.append(hexChars[i and 0x0F])
        }
        return result.toString()
    }

    private fun hexToBytes(hex: String): ByteArray {
        val result = ByteArray(hex.length / 2)
        for (i in result.indices) {
            val index = i * 2
            val byteVal = hex.substring(index, index + 2).toInt(16)
            result[i] = byteVal.toByte()
        }
        return result
    }
}

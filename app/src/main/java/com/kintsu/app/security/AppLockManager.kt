package com.kintsu.app.security

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import java.security.MessageDigest
import java.util.concurrent.Executors

enum class AppLockType(val displayName: String) {
    PIN("PIN Code"),
    PATTERN("Pattern"),
    PASSWORD("Password")
}

object AppLockManager {

    private const val SALT = "KintsuSecureEnclaveSalt#2026"

    /**
     * Compute SHA-256 hash with salt for secure storage.
     * Preserves strict case sensitivity and special characters.
     */
    fun hashSecret(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest((SALT + input).toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies user input against stored hash.
     * Strict case-sensitive matching for passwords and passcodes.
     */
    fun verifySecret(input: String, storedHash: String): Boolean {
        if (storedHash.isBlank()) return false
        val computed = hashSecret(input)
        return computed == storedHash.lowercase()
    }

    /**
     * Formats pattern list into a string (e.g. "0-1-2-5-8" or "5x5:0-6-12-18-24").
     */
    fun patternToString(points: List<Int>, gridSize: Int = 3): String {
        return if (gridSize == 5) "5x5:${points.joinToString("-")}" else points.joinToString("-")
    }

    /**
     * Verifies pattern with fallback for standard and grid-prefixed formats.
     */
    fun verifyPattern(points: List<Int>, gridSize: Int, storedHash: String): Boolean {
        if (storedHash.isBlank() || points.isEmpty()) return false
        val formatted = patternToString(points, gridSize)
        val plain = points.joinToString("-")
        return verifySecret(formatted, storedHash) || verifySecret(plain, storedHash)
    }

    /**
     * Triggers native Android BiometricPrompt on Android 9+ (API 28+).
     */
    fun authenticateWithBiometrics(
        context: Context,
        title: String = "Unlock Kintsu",
        subtitle: String = "Verify your fingerprint or face to continue",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ): CancellationSignal? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val cancellationSignal = CancellationSignal()
            val executor = Executors.newSingleThreadExecutor()

            val activity = findActivity(context)
            val promptBuilder = BiometricPrompt.Builder(context)
                .setTitle(title)
                .setSubtitle(subtitle)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                promptBuilder.setNegativeButton("Use Passcode", executor) { _, _ ->
                    activity?.runOnUiThread { onError("Passcode chosen") }
                }
            } else {
                promptBuilder.setNegativeButton("Cancel", executor) { _, _ ->
                    activity?.runOnUiThread { onError("Cancelled") }
                }
            }

            val prompt = promptBuilder.build()
            prompt.authenticate(
                cancellationSignal,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                        super.onAuthenticationSucceeded(result)
                        activity?.runOnUiThread { onSuccess() }
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                        super.onAuthenticationError(errorCode, errString)
                        activity?.runOnUiThread { onError(errString?.toString() ?: "Biometric error") }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        activity?.runOnUiThread { onError("Biometric not recognized") }
                    }
                }
            )
            return cancellationSignal
        } else {
            onError("Biometrics require Android 9.0+")
            return null
        }
    }

    private fun findActivity(context: Context): Activity? {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) {
                return currentContext
            }
            currentContext = currentContext.baseContext
        }
        return null
    }
}

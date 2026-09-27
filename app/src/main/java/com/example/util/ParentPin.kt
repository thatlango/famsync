package com.example.util

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import android.util.Base64

/** Local parent gate. Never persist a readable PIN. */
object ParentPin {
    private const val ITERATIONS = 120_000
    private const val KEY_BITS = 256
    private const val PREFIX = "pbkdf2-sha256"

    fun isValid(pin: String): Boolean = pin.length in 4..8 && pin.all(Char::isDigit) && pin != "1234"

    fun hash(pin: String): String {
        require(isValid(pin))
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val key = derive(pin, salt, ITERATIONS)
        return listOf(PREFIX, ITERATIONS.toString(), encode(salt), encode(key)).joinToString(":")
    }

    fun verify(pin: String, stored: String): Boolean {
        if (pin.isEmpty() || stored.isEmpty()) return false
        val parts = stored.split(":")
        if (parts.size != 4 || parts[0] != PREFIX) return false
        return try {
            val iterations = parts[1].toInt()
            if (iterations != ITERATIONS) return false
            val salt = Base64.decode(parts[2], Base64.NO_WRAP)
            val expected = Base64.decode(parts[3], Base64.NO_WRAP)
            MessageDigest.isEqual(expected, derive(pin, salt, iterations))
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    private fun derive(pin: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, iterations, KEY_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun encode(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)
}

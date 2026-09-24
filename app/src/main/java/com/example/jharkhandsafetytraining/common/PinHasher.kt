package com.example.jharkhandsafetytraining.common

import java.security.MessageDigest
import java.security.SecureRandom

object PinHasher {

    // Returns "salt:hash" so each user gets a different random salt
    fun hash(pin: String): String {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        return "${toHex(salt)}:${toHex(digest(salt, pin))}"
    }

    fun verify(pin: String, stored: String): Boolean {
        val parts = stored.split(":")
        if (parts.size != 2) return false
        val salt = fromHex(parts[0])
        return MessageDigest.isEqual(digest(salt, pin), fromHex(parts[1]))
    }

    private fun digest(salt: ByteArray, pin: String): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(salt + pin.toByteArray())

    private fun toHex(bytes: ByteArray): String =
        bytes.joinToString("") { "%02x".format(it) }

    private fun fromHex(hex: String): ByteArray =
        ByteArray(hex.length / 2) { hex.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
}
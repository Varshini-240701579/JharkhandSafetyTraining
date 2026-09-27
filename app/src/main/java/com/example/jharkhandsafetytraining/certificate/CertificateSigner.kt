package com.example.jharkhandsafetytraining.certificate

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object CertificateSigner {

    // Fixed key baked into the app — fine for this project's purpose (see note below)
    private const val SECRET_KEY = "JharkhandSafetyTraining_2026_SecretKey"

    /**
     * Builds the payload string that goes inside the QR code.
     * Format: userId|moduleId|issuedAt
     */
    fun buildPayload(userId: Long, moduleId: String, issuedAt: Long): String {
        return "$userId|$moduleId|$issuedAt"
    }

    /**
     * Signs a payload and returns "payload::signature" — this full string
     * is what gets stored in Certificate.signedPayload and encoded into the QR.
     */
    fun sign(payload: String): String {
        val signature = hmac(payload)
        return "$payload::$signature"
    }

    /**
     * Verifies a signed string (as scanned from a QR code).
     * Returns true if the signature matches and hasn't been tampered with.
     */
    fun verify(signedString: String): Boolean {
        val parts = signedString.split("::")
        if (parts.size != 2) return false
        val payload = parts[0]
        val givenSignature = parts[1]
        val expectedSignature = hmac(payload)
        return givenSignature == expectedSignature
    }

    private fun hmac(payload: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(SECRET_KEY.toByteArray(), "HmacSHA256"))
        val bytes = mac.doFinal(payload.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
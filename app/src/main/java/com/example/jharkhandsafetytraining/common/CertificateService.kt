package com.example.jharkhandsafetytraining.common

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONObject
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

data class CertificatePayload(
    val version: Int = 1,
    val certId: String,
    val userId: Long,
    val workerName: String,
    val workerPhone: String,
    val role: String,
    val moduleId: String,
    val modulesCompleted: List<String>,
    val issuedAt: Long,
    val signature: String
)

sealed class VerificationResult {
    data class Valid(val payload: CertificatePayload) : VerificationResult()
    data class Invalid(val reason: String, val tamperedPayload: CertificatePayload? = null) : VerificationResult()
}

object CertificateService {

    private const val HMAC_ALGORITHM = "HmacSHA256"
    // Offline shared secret used by issuer and field supervisor verifiers
    private const val OFFLINE_HMAC_SECRET = "JHARKHAND_VOCATIONAL_SAFETY_CERT_HMAC_KEY_2026"

    val REQUIRED_MODULES = listOf(
        "FIRE",
        "GAS",
        "COLLAPSE",
        "FLOOD",
        "OXYGEN",
        "MACHINERY",
        "PPE"
    )

    fun hasPassedAllRequiredModules(passedModuleIds: Set<String>): Boolean {
        return REQUIRED_MODULES.all { it in passedModuleIds }
    }

    /**
     * Generates a tamper-evident JSON string signed with HMAC-SHA256.
     */
    fun generateSignedPayload(
        userId: Long,
        workerName: String,
        workerPhone: String,
        role: String = "WORKER",
        moduleId: String = "ALL",
        modulesCompleted: List<String> = REQUIRED_MODULES,
        issuedAt: Long = System.currentTimeMillis()
    ): String {
        val version = 1
        val normalizedModules = modulesCompleted.map { it.uppercase() }.distinct().sorted()
        val certId = "JST-$userId-${issuedAt.toString().takeLast(6)}"

        val canonical = buildCanonicalString(
            version = version,
            certId = certId,
            userId = userId,
            workerName = workerName.trim(),
            workerPhone = workerPhone.trim(),
            role = role.trim(),
            moduleId = moduleId.uppercase(),
            modulesCsv = normalizedModules.joinToString(","),
            issuedAt = issuedAt
        )

        val signature = computeHmacSha256(canonical)

        return JSONObject().apply {
            put("v", version)
            put("cid", certId)
            put("uid", userId)
            put("name", workerName.trim())
            put("phone", workerPhone.trim())
            put("role", role.trim())
            put("mod", moduleId.uppercase())
            put("mods", normalizedModules.joinToString(","))
            put("iat", issuedAt)
            put("sig", signature)
        }.toString()
    }

    /**
     * Verifies a scanned or pasted JSON certificate payload completely offline using HMAC-SHA256.
     */
    fun verifySignedPayload(rawPayload: String): VerificationResult {
        if (rawPayload.isBlank()) {
            return VerificationResult.Invalid("Empty QR code payload.")
        }

        return try {
            val json = JSONObject(rawPayload.trim())
            if (!json.has("uid") || !json.has("name") || !json.has("iat") || !json.has("sig")) {
                return VerificationResult.Invalid("Missing required certificate fields in payload.")
            }

            val version = json.optInt("v", 1)
            val userId = json.getLong("uid")
            val issuedAt = json.getLong("iat")
            val certId = json.optString("cid", "JST-$userId-${issuedAt.toString().takeLast(6)}")
            val workerName = json.getString("name")
            val workerPhone = json.optString("phone", "")
            val role = json.optString("role", "WORKER")
            val moduleId = json.optString("mod", "ALL")
            val modsCsv = json.optString("mods", moduleId)
            val modulesList = modsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val providedSig = json.getString("sig").trim().lowercase()

            val parsedPayload = CertificatePayload(
                version = version,
                certId = certId,
                userId = userId,
                workerName = workerName,
                workerPhone = workerPhone,
                role = role,
                moduleId = moduleId,
                modulesCompleted = modulesList,
                issuedAt = issuedAt,
                signature = providedSig
            )

            val canonical = buildCanonicalString(
                version = version,
                certId = certId,
                userId = userId,
                workerName = workerName,
                workerPhone = workerPhone,
                role = role,
                moduleId = moduleId,
                modulesCsv = modulesList.sorted().joinToString(","),
                issuedAt = issuedAt
            )

            val expectedSig = computeHmacSha256(canonical)

            val isSignatureValid = MessageDigest.isEqual(
                expectedSig.toByteArray(Charsets.UTF_8),
                providedSig.toByteArray(Charsets.UTF_8)
            )

            if (isSignatureValid) {
                VerificationResult.Valid(parsedPayload)
            } else {
                VerificationResult.Invalid(
                    reason = "HMAC-SHA256 signature verification failed. Certificate data or signature has been tampered with.",
                    tamperedPayload = parsedPayload
                )
            }
        } catch (e: Exception) {
            VerificationResult.Invalid("Invalid certificate format: ${e.localizedMessage ?: "Malformed JSON"}")
        }
    }

    /**
     * Generates an offline-scannable QR code Bitmap using ZXing.
     */
    fun generateQrBitmap(payload: String, sizePx: Int = 640): Bitmap? {
        if (payload.isBlank()) return null
        return try {
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to 1
            )
            val bitMatrix = QRCodeWriter().encode(
                payload,
                BarcodeFormat.QR_CODE,
                sizePx,
                sizePx,
                hints
            )
            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                }
            }
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                setPixels(pixels, 0, width, 0, 0, width, height)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun buildCanonicalString(
        version: Int,
        certId: String,
        userId: Long,
        workerName: String,
        workerPhone: String,
        role: String,
        moduleId: String,
        modulesCsv: String,
        issuedAt: Long
    ): String {
        return "v=$version|cid=$certId|uid=$userId|name=$workerName|phone=$workerPhone|role=$role|mod=$moduleId|mods=$modulesCsv|iat=$issuedAt"
    }

    fun computeHmacSha256(data: String): String {
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        val secretKey = SecretKeySpec(OFFLINE_HMAC_SECRET.toByteArray(Charsets.UTF_8), HMAC_ALGORITHM)
        mac.init(secretKey)
        val rawHmac = mac.doFinal(data.toByteArray(Charsets.UTF_8))
        return rawHmac.joinToString("") { "%02x".format(it) }
    }
}

package com.example.jharkhandsafetytraining.certificate

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.example.jharkhandsafetytraining.data.Certificate
import com.example.jharkhandsafetytraining.data.TrainingDao
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun CertificateScreen(
    moduleId: String,
    userId: Long,
    dao: TrainingDao,
    onDone: () -> Unit
) {
    var certificate by remember { mutableStateOf<Certificate?>(null) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(moduleId, userId) {
        val existing = dao.getCertificates(userId).find { it.moduleId == moduleId }
        val cert = if (existing != null) {
            existing
        } else {
            val issuedAt = System.currentTimeMillis()
            val payload = CertificateSigner.buildPayload(userId, moduleId, issuedAt)
            val signed = CertificateSigner.sign(payload)
            val newCert = Certificate(
                userId = userId,
                moduleId = moduleId,
                issuedAt = issuedAt,
                signedPayload = signed
            )
            dao.insertCertificate(newCert)
            dao.getCertificates(userId).find { it.moduleId == moduleId }
        }
        certificate = cert
        cert?.let {
            qrBitmap = generateQrBitmap(it.signedPayload)
        }
    }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val cert = certificate
        if (cert == null) {
            CircularProgressIndicator()
        } else {
            Text("Certificate Issued", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("Module: $moduleId", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(24.dp))

            qrBitmap?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Certificate QR code",
                    modifier = Modifier.size(220.dp)
                )
            } ?: CircularProgressIndicator()

            Spacer(Modifier.height(16.dp))
            Text(
                "Scan to verify this certificate",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                Text("Done")
            }
        }
    }
}

private fun generateQrBitmap(content: String, size: Int = 512): Bitmap {
    val bits = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bitmap
}
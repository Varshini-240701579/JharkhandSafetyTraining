package com.example.jharkhandsafetytraining.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.jharkhandsafetytraining.common.CertificateService
import com.example.jharkhandsafetytraining.common.SessionManager
import com.example.jharkhandsafetytraining.common.VerificationResult
import com.example.jharkhandsafetytraining.data.AppDatabase
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifierScreen(
    initialPayload: String? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    var payloadInput by remember(initialPayload) { mutableStateOf(initialPayload ?: "") }
    var verificationResult by remember(initialPayload) {
        mutableStateOf<VerificationResult?>(
            if (!initialPayload.isNullOrBlank()) {
                CertificateService.verifySignedPayload(initialPayload)
            } else {
                null
            }
        )
    }

    // ZXing Offline Camera QR Scanner Launcher
    val qrScannerLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val scannedContents = result.contents
        if (scannedContents != null) {
            payloadInput = scannedContents
            verificationResult = CertificateService.verifySignedPayload(scannedContents)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offline Certificate Verifier") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("← Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Scan a worker's QR code or verify a signed certificate payload locally using HMAC-SHA256 (100% Offline — No Internet Required).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 1. Live Camera QR Scanner Button (ZXing)
            Button(
                onClick = {
                    val options = ScanOptions().apply {
                        setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                        setPrompt("Align Worker Safety Certificate QR Code within frame")
                        setBeepEnabled(true)
                        setOrientationLocked(false)
                    }
                    qrScannerLauncher.launch(options)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Scan QR Code with Camera")
            }

            // 2. Manual / Emulator Payload Input & Quick Test Controls
            OutlinedTextField(
                value = payloadInput,
                onValueChange = { payloadInput = it },
                label = { Text("Signed Certificate QR Payload (JSON)") },
                placeholder = { Text("Paste or scan QR payload...") },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val certPayload = withContext(Dispatchers.IO) {
                                val db = AppDatabase.getInstance(context)
                                val userId = SessionManager(context).getUserId() ?: 1L
                                val certs = db.trainingDao().getCertificates(userId)
                                certs.firstOrNull()?.signedPayload ?: run {
                                    val user = db.userDao().findById(userId)
                                    CertificateService.generateSignedPayload(
                                        userId = userId,
                                        workerName = user?.name ?: "Sample Mine Worker",
                                        workerPhone = user?.phone ?: "9876543210",
                                        role = user?.role ?: "WORKER",
                                        moduleId = "ALL"
                                    )
                                }
                            }
                            payloadInput = certPayload
                            verificationResult = CertificateService.verifySignedPayload(certPayload)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Load Local Cert")
                }

                OutlinedButton(
                    onClick = {
                        if (payloadInput.isNotBlank()) {
                            // Tamper with the payload to demonstrate HMAC failure
                            val tampered = if (payloadInput.contains("\"mod\":\"ALL\"")) {
                                payloadInput.replace("\"mod\":\"ALL\"", "\"mod\":\"HACKED\"")
                            } else if (payloadInput.contains("\"uid\":")) {
                                payloadInput.replaceFirst("\"uid\":", "\"uid\":999")
                            } else {
                                payloadInput + "tampered"
                            }
                            payloadInput = tampered
                            verificationResult = CertificateService.verifySignedPayload(tampered)
                        }
                    },
                    enabled = payloadInput.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Simulate Tamper")
                }
            }

            Button(
                onClick = {
                    verificationResult = CertificateService.verifySignedPayload(payloadInput)
                },
                enabled = payloadInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Verify HMAC-SHA256 Signature")
            }

            // 3. Verification Result Display
            when (val res = verificationResult) {
                is VerificationResult.Valid -> {
                    val p = res.payload
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(2.dp, Color(0xFF2E7D32)),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8F5E9)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "✓ VALID CERTIFICATE",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "HMAC-SHA256 Cryptographic Signature Verified Offline",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF2E7D32)
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = Color(0xFFA5D6A7)
                            )

                            VerifierDetailRow("Certificate ID", p.certId)
                            VerifierDetailRow("Worker Name", p.workerName)
                            VerifierDetailRow("Worker Phone", p.workerPhone)
                            VerifierDetailRow("Worker ID", "#${p.userId} (${p.role})")
                            VerifierDetailRow("Certificate Scope", p.moduleId)
                            VerifierDetailRow("Modules Passed", p.modulesCompleted.joinToString(", "))
                            VerifierDetailRow("Issued Date", dateFormatter.format(Date(p.issuedAt)))

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "HMAC-SHA256: ${p.signature.take(16)}...${p.signature.takeLast(16)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF1B5E20)
                            )
                        }
                    }
                }

                is VerificationResult.Invalid -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.error),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "✗ INVALID / TAMPERED",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = res.reason,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center
                            )

                            res.tamperedPayload?.let { p ->
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                Text(
                                    text = "Unverified Claims in Tampered Payload:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Name: ${p.workerName} | Scope: ${p.moduleId} | UID: ${p.userId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                null -> {
                    // Initial state before scan/verify
                }
            }
        }
    }
}

@Composable
private fun VerifierDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF2E7D32),
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1B5E20),
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
    }
}

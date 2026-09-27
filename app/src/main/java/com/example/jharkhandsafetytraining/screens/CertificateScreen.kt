package com.example.jharkhandsafetytraining.screens

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.jharkhandsafetytraining.common.CertificatePayload
import com.example.jharkhandsafetytraining.common.CertificateService
import com.example.jharkhandsafetytraining.common.SessionManager
import com.example.jharkhandsafetytraining.common.VerificationResult
import com.example.jharkhandsafetytraining.data.AppDatabase
import com.example.jharkhandsafetytraining.data.Certificate
import com.example.jharkhandsafetytraining.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificateScreen(
    onBack: () -> Unit,
    onOpenVerifier: (initialPayload: String?) -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var user by remember { mutableStateOf<User?>(null) }
    var passedModules by remember { mutableStateOf<Set<String>>(emptySet()) }
    var activeCertificate by remember { mutableStateOf<Certificate?>(null) }
    var decodedPayload by remember { mutableStateOf<CertificatePayload?>(null) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var statusBanner by remember { mutableStateOf<String?>(null) }

    suspend fun loadOrGenerateCertificate() {
        isLoading = true
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            val trainingDao = db.trainingDao()
            val userDao = db.userDao()
            val session = SessionManager(context)
            val userId = session.getUserId() ?: 1L

            val loadedUser = userDao.findById(userId) ?: User(
                id = userId,
                name = "Industrial Worker #$userId",
                phone = "9999999999",
                pinHash = "",
                role = "WORKER",
                language = "hi"
            )
            user = loadedUser

            val progressList = trainingDao.getProgress(userId)
            val passedSet = progressList
                .filter { it.quizPassed }
                .map { it.moduleId.uppercase() }
                .toSet()
            passedModules = passedSet

            val allRequiredPassed = CertificateService.hasPassedAllRequiredModules(passedSet)
            var cert: Certificate? = null

            if (allRequiredPassed) {
                // Check if master certificate already exists in Room
                cert = trainingDao.getCertificateForModule(userId, "ALL")
                if (cert == null) {
                    val now = System.currentTimeMillis()
                    val signedPayload = CertificateService.generateSignedPayload(
                        userId = loadedUser.id,
                        workerName = loadedUser.name,
                        workerPhone = loadedUser.phone,
                        role = loadedUser.role,
                        moduleId = "ALL",
                        modulesCompleted = CertificateService.REQUIRED_MODULES,
                        issuedAt = now
                    )
                    val newCert = Certificate(
                        userId = loadedUser.id,
                        moduleId = "ALL",
                        issuedAt = now,
                        signedPayload = signedPayload,
                        synced = false
                    )
                    trainingDao.insertCertificate(newCert)
                    cert = trainingDao.getCertificateForModule(userId, "ALL") ?: newCert
                }
            } else {
                // Check if a module certificate exists for already passed module(s)
                val existingCerts = trainingDao.getCertificates(userId)
                cert = existingCerts.firstOrNull()
                if (cert == null && passedSet.isNotEmpty()) {
                    val now = System.currentTimeMillis()
                    val primaryMod = passedSet.first()
                    val signedPayload = CertificateService.generateSignedPayload(
                        userId = loadedUser.id,
                        workerName = loadedUser.name,
                        workerPhone = loadedUser.phone,
                        role = loadedUser.role,
                        moduleId = primaryMod,
                        modulesCompleted = passedSet.toList(),
                        issuedAt = now
                    )
                    val newCert = Certificate(
                        userId = loadedUser.id,
                        moduleId = primaryMod,
                        issuedAt = now,
                        signedPayload = signedPayload,
                        synced = false
                    )
                    trainingDao.insertCertificate(newCert)
                    cert = newCert
                }
            }

            activeCertificate = cert
            if (cert != null && cert.signedPayload.isNotBlank()) {
                val verification = CertificateService.verifySignedPayload(cert.signedPayload)
                decodedPayload = when (verification) {
                    is VerificationResult.Valid -> verification.payload
                    is VerificationResult.Invalid -> verification.tamperedPayload
                }
                qrBitmap = CertificateService.generateQrBitmap(cert.signedPayload, 640)
            } else {
                decodedPayload = null
                qrBitmap = null
            }
        }
        isLoading = false
    }

    LaunchedEffect(Unit) {
        loadOrGenerateCertificate()
    }

    val allPassed = CertificateService.hasPassedAllRequiredModules(passedModules)
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Safety Certification") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("← Back")
                    }
                },
                actions = {
                    TextButton(onClick = { onOpenVerifier(activeCertificate?.signedPayload) }) {
                        Text("Verify QR")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Module Completion Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (allPassed) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (allPassed) {
                                "All ${CertificateService.REQUIRED_MODULES.size} Required Safety Modules Passed"
                            } else {
                                "Training Progress: ${passedModules.size} / ${CertificateService.REQUIRED_MODULES.size} Modules Passed"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = {
                                passedModules.size.toFloat() / CertificateService.REQUIRED_MODULES.size.toFloat()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = CertificateService.REQUIRED_MODULES.joinToString(" • ") { mod ->
                                if (mod in passedModules) "✓ $mod" else "○ $mod"
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                val cert = activeCertificate
                val currentUser = user

                if (cert != null && currentUser != null) {
                    // Official Digital Certificate Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "GOVERNMENT OF JHARKHAND",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "VOCATIONAL MINE & INDUSTRIAL SAFETY CERTIFICATE",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = if (cert.moduleId == "ALL") {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.secondaryContainer
                                },
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = if (cert.moduleId == "ALL") {
                                        "FULL 7-MODULE SAFETY CERTIFICATION"
                                    } else {
                                        "MODULE CERTIFICATE: ${cert.moduleId}"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            // Worker Details
                            CertificateDetailRow("Certificate ID", decodedPayload?.certId ?: "JST-${cert.userId}-${cert.id}")
                            CertificateDetailRow("Worker Name", currentUser.name)
                            CertificateDetailRow("Mobile / ID", currentUser.phone)
                            CertificateDetailRow("Role", currentUser.role)
                            CertificateDetailRow(
                                "Certified Modules",
                                decodedPayload?.modulesCompleted?.joinToString(", ") ?: cert.moduleId
                            )
                            CertificateDetailRow("Issued Date", dateFormatter.format(Date(cert.issuedAt)))

                            Spacer(modifier = Modifier.height(16.dp))

                            // Offline-Scannable QR Code
                            qrBitmap?.let { bmp ->
                                Box(
                                    modifier = Modifier
                                        .background(Color.White, shape = MaterialTheme.shapes.medium)
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = "HMAC-SHA256 Signed Offline Certificate QR Code",
                                        modifier = Modifier.size(220.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Offline Tamper-Evident HMAC-SHA256 Signature:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = decodedPayload?.signature?.let {
                                    "${it.take(16)}...${it.takeLast(16)}"
                                } ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    statusBanner?.let { msg ->
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(cert.signedPayload))
                                statusBanner = "Signed QR payload copied to clipboard!"
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Copy Payload")
                        }

                        Button(
                            onClick = { onOpenVerifier(cert.signedPayload) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Verify Offline")
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No Certificate Issued Yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Complete the safety module quizzes to earn your tamper-evident HMAC-SHA256 signed certificate.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // If not all 7 modules are passed yet, allow completing all required modules or returning to Home
                if (!allPassed) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    val db = AppDatabase.getInstance(context)
                                    val trainingDao = db.trainingDao()
                                    val userId = SessionManager(context).getUserId() ?: 1L
                                    CertificateService.REQUIRED_MODULES.forEach { modId ->
                                        trainingDao.markQuizPassed(userId, modId)
                                    }
                                }
                                loadOrGenerateCertificate()
                                statusBanner = "All 7 required safety modules marked passed & Master Certificate generated!"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Complete All 7 Modules & Generate Full Certificate")
                    }
                }
            }
        }
    }
}

@Composable
private fun CertificateDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
    }
}

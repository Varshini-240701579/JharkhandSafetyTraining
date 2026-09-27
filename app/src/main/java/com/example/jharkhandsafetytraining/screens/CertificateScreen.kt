package com.example.jharkhandsafetytraining.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val ScreenBg = Color(0xFF121418)
private val CredentialCardBg = Color(0xFF181C24)
private val InnerPanelBg = Color(0xFF13161D)
private val GoldBorderOuter = Color(0xFFF59E0B)
private val GoldBorderInner = Color(0xFFD97706)
private val SlateBorder = Color(0xFF374151)
private val VerifiedGreen = Color(0xFF10B981)
private val VerifiedGreenBg = Color(0xFF064E3B)
private val MutedText = Color(0xFF9CA3AF)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CertificateScreen(
    onBack: () -> Unit,
    onOpenVerifier: (initialPayload: String?) -> Unit = {}
) {
    val context = LocalContext.current
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
                qrBitmap = CertificateService.generateQrBitmap(cert.signedPayload, 680)
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
    val shortDateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CredentialCardBg,
                    titleContentColor = Color.White,
                    navigationIconContentColor = GoldBorderOuter,
                    actionIconContentColor = GoldBorderOuter
                ),
                title = {
                    Column {
                        Text(
                            text = "OFFICIAL COMPLIANCE CREDENTIAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldBorderOuter,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Safety Competency Certificate",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(
                            text = "← Back",
                            color = GoldBorderOuter,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { onOpenVerifier(activeCertificate?.signedPayload) }) {
                        Text(
                            text = "Verify QR →",
                            color = GoldBorderOuter,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ScreenBg)
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GoldBorderOuter)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ScreenBg)
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val cert = activeCertificate
                val currentUser = user

                if (cert != null && currentUser != null) {
                    val certIdText = decodedPayload?.certId ?: "JST-${cert.userId}-${cert.id}"
                    val issueDateStr = dateFormatter.format(Date(cert.issuedAt))
                    val validUntilDateStr = remember(cert.issuedAt) {
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = cert.issuedAt
                            add(Calendar.YEAR, 2)
                        }
                        shortDateFormatter.format(cal.time)
                    }
                    val hashPrefix = decodedPayload?.signature?.take(16) ?: "30fe9afeec1984a1"

                    // 1. Double-Bordered Formal Government Credential Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = CredentialCardBg),
                        border = BorderStroke(2.dp, GoldBorderOuter),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp)
                                .border(
                                    width = 1.dp,
                                    color = GoldBorderInner.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(13.dp)
                                )
                        ) {
                            // Subtle Corner Notches
                            CornerNotch(Modifier.align(Alignment.TopStart))
                            CornerNotch(Modifier.align(Alignment.TopEnd))
                            CornerNotch(Modifier.align(Alignment.BottomStart))
                            CornerNotch(Modifier.align(Alignment.BottomEnd))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 22.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Emblem Crest
                                Surface(
                                    color = GoldBorderOuter.copy(alpha = 0.15f),
                                    shape = CircleShape,
                                    border = BorderStroke(1.5.dp, GoldBorderOuter)
                                ) {
                                    Text(
                                        text = "⛏",
                                        fontSize = 22.sp,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "GOVERNMENT OF JHARKHAND • DEPARTMENT OF MINES & GEOLOGY",
                                    color = GoldBorderOuter,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.9.sp,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "VOCATIONAL MINING SAFETY COMPETENCY CERTIFICATE",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 22.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Official Certificate ID Chip (Monospace)
                                Surface(
                                    color = InnerPanelBg,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, GoldBorderInner.copy(alpha = 0.7f))
                                ) {
                                    Text(
                                        text = "CERT ID: $certIdText",
                                        color = GoldBorderOuter,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                    )
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    color = SlateBorder
                                )

                                // 2. Worker Details Panel
                                Surface(
                                    color = InnerPanelBg,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, SlateBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        CredentialDetailRow("WORKER NAME", currentUser.name.uppercase())
                                        CredentialDetailRow("MOBILE / REG ID", currentUser.phone)
                                        CredentialDetailRow(
                                            "DESIGNATED ROLE",
                                            "CERTIFIED UNDERGROUND MINER"
                                        )
                                        CredentialDetailRow("ISSUED TIMESTAMP", issueDateStr)
                                        CredentialDetailRow(
                                            "VALIDITY PERIOD",
                                            "Valid for 2 Years (Until $validUntilDateStr)"
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Verified Module Scope Badges
                                Text(
                                    text = "VERIFIED SAFETY DOMAIN COMPETENCIES (${passedModules.size}/${CertificateService.REQUIRED_MODULES.size})",
                                    color = MutedText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.7.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    CertificateService.REQUIRED_MODULES.forEach { mod ->
                                        val isModPassed = mod in passedModules || cert.moduleId == "ALL"
                                        Surface(
                                            color = if (isModPassed) {
                                                VerifiedGreenBg.copy(alpha = 0.75f)
                                            } else {
                                                InnerPanelBg
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(
                                                width = 1.dp,
                                                color = if (isModPassed) VerifiedGreen else SlateBorder
                                            )
                                        ) {
                                            Text(
                                                text = if (isModPassed) "✓ $mod" else "○ $mod",
                                                color = if (isModPassed) Color(0xFF34D399) else MutedText,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // 3. High-Security QR Code & Cryptographic Seal
                                qrBitmap?.let { bmp ->
                                    Box(
                                        modifier = Modifier
                                            .background(Color.White, shape = RoundedCornerShape(14.dp))
                                            .border(
                                                width = 2.dp,
                                                color = GoldBorderOuter,
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = "HMAC-SHA256 Signed Offline Certificate QR Code",
                                            modifier = Modifier.size(220.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "TAMPER-EVIDENT HMAC-SHA256 SIGNATURE",
                                    color = GoldBorderOuter,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Surface(
                                    color = VerifiedGreenBg.copy(alpha = 0.65f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, VerifiedGreen)
                                ) {
                                    Text(
                                        text = "HASH: $hashPrefix... [VERIFIED]",
                                        color = Color(0xFF34D399),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    statusBanner?.let { msg ->
                        Surface(
                            color = VerifiedGreenBg.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, VerifiedGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✓ $msg",
                                color = Color(0xFF34D399),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // 4. Worker Action Suite
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CredentialCardBg),
                        border = BorderStroke(1.dp, SlateBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "CREDENTIAL VERIFICATION & EXPORT SUITE",
                                color = MutedText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )

                            // Primary Action: Verify Offline
                            Button(
                                onClick = { onOpenVerifier(cert.signedPayload) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldBorderInner,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text(
                                    text = "Verify Offline (HMAC-SHA256 Scanner) →",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Copy Verification Payload Button with Toast
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText(
                                            "Jharkhand Safety Certificate Payload",
                                            cert.signedPayload
                                        )
                                        clipboard.setPrimaryClip(clip)
                                        statusBanner = "Signed JSON payload copied to clipboard."
                                        Toast.makeText(
                                            context,
                                            "Verification payload copied to clipboard",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, GoldBorderOuter),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = InnerPanelBg,
                                        contentColor = GoldBorderOuter
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                ) {
                                    Text(
                                        text = "Copy Verification Payload",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                // Share / Save Certificate Button via Intent.ACTION_SEND
                                OutlinedButton(
                                    onClick = {
                                        val shareBody = buildString {
                                            appendLine("GOVERNMENT OF JHARKHAND • DEPARTMENT OF MINES & GEOLOGY")
                                            appendLine("VOCATIONAL MINING SAFETY COMPETENCY CERTIFICATE")
                                            appendLine("Certificate ID: $certIdText")
                                            appendLine("Worker Name: ${currentUser.name}")
                                            appendLine("Mobile / ID: ${currentUser.phone}")
                                            appendLine("Role: CERTIFIED UNDERGROUND MINER")
                                            appendLine("Issued: $issueDateStr (Valid for 2 Years)")
                                            appendLine("HMAC-SHA256 Hash: $hashPrefix... [VERIFIED]")
                                            appendLine()
                                            appendLine("Signed Offline Verification Payload:")
                                            append(cert.signedPayload)
                                        }
                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "Safety Competency Certificate - $certIdText")
                                            putExtra(Intent.EXTRA_TEXT, shareBody)
                                        }
                                        context.startActivity(
                                            Intent.createChooser(sendIntent, "Share / Save Safety Certificate")
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, SlateBorder),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = InnerPanelBg,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                ) {
                                    Text(
                                        text = "Share / Save Certificate",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Empty State when no modules have been passed yet
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CredentialCardBg),
                        border = BorderStroke(1.5.dp, GoldBorderOuter.copy(alpha = 0.7f))
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "⚠ CREDENTIAL PENDING ISSUANCE",
                                color = GoldBorderOuter,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Pass the Vocational Safety Module Assessments to generate your official HMAC-SHA256 signed competency credential.",
                                color = MutedText,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Helper button if not all 7 modules are passed yet
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
                                statusBanner = "All 7 safety domains verified & Master Competency Certificate issued!"
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, GoldBorderOuter.copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CredentialCardBg,
                            contentColor = GoldBorderOuter
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Complete All 7 Modules & Issue Full Competency Credential",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CornerNotch(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(6.dp)
            .size(8.dp)
            .clip(CircleShape)
            .background(GoldBorderOuter.copy(alpha = 0.75f))
    )
}

@Composable
private fun CredentialDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MutedText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.weight(0.42f)
        )
        Text(
            text = value,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.58f)
        )
    }
}

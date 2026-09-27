package com.example.jharkhandsafetytraining.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jharkhandsafetytraining.common.SessionManager
import com.example.jharkhandsafetytraining.data.AppDatabase
import com.example.jharkhandsafetytraining.data.ModuleProgress
import com.example.jharkhandsafetytraining.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val safetyModules = listOf(
    "Fire & Explosion" to "FIRE",
    "Gas Leak" to "GAS",
    "Mine Collapse" to "COLLAPSE",
    "Flooding" to "FLOOD",
    "Oxygen Depletion" to "OXYGEN",
    "Machinery Safety" to "MACHINERY",
    "PPE Compliance" to "PPE"
)

private data class SafetyModuleSpec(
    val id: String,
    val title: String,
    val hindiSubtitle: String,
    val domainTag: String,
    val symbol: String,
    val accentColor: Color
)

private val cockpitModules = listOf(
    SafetyModuleSpec(
        id = "FIRE",
        title = "Fire & Explosion Suppression",
        hindiSubtitle = "आग और विस्फोट सुरक्षा प्रोटोकॉल",
        domainTag = "HAZARD: FIRE-01",
        symbol = "🔥",
        accentColor = Color(0xFFEF4444) // Hazard Red
    ),
    SafetyModuleSpec(
        id = "GAS",
        title = "Methane Gas Detection & Venting",
        hindiSubtitle = "मीथेन गैस रिसाव और वेंटिलेशन",
        domainTag = "HAZARD: GAS-02",
        symbol = "⚠",
        accentColor = Color(0xFFF97316) // Hazard Orange
    ),
    SafetyModuleSpec(
        id = "COLLAPSE",
        title = "Roof Strata & Mine Collapse Control",
        hindiSubtitle = "खदान छत समर्थन और धंसाव नियंत्रण",
        domainTag = "STRATA: COL-03",
        symbol = "⛰",
        accentColor = Color(0xFFF59E0B) // Amber Gold
    ),
    SafetyModuleSpec(
        id = "FLOOD",
        title = "Underground Inundation & Dewatering",
        hindiSubtitle = "जल भराव और आपातकालीन निकासी",
        domainTag = "HYDRO: FLD-04",
        symbol = "🌊",
        accentColor = Color(0xFF38BDF8) // Hydro Cyan-Blue
    ),
    SafetyModuleSpec(
        id = "OXYGEN",
        title = "Oxygen Depletion & SCSR Protocol",
        hindiSubtitle = "ऑक्सीजन की कमी और स्व-बचाव उपकरण",
        domainTag = "ATMOS: OXY-05",
        symbol = "O₂",
        accentColor = Color(0xFF06B6D4) // Cyan
    ),
    SafetyModuleSpec(
        id = "MACHINERY",
        title = "Heavy Machinery & LOTO Compliance",
        hindiSubtitle = "भारी मशीनरी और लॉकआउट-टैगआउट",
        domainTag = "MECH: MCH-06",
        symbol = "⚙",
        accentColor = Color(0xFFEAB308) // Safety Yellow
    ),
    SafetyModuleSpec(
        id = "PPE",
        title = "Mandatory PPE & Cap-Lamp Rig",
        hindiSubtitle = "व्यक्तिगत सुरक्षा उपकरण अनुपालन",
        domainTag = "EQUIP: PPE-07",
        symbol = "🛡",
        accentColor = Color(0xFF10B981) // Emerald
    )
)

// Industrial Cockpit Palette
private val CockpitBg = Color(0xFF121418)
private val SlateCard = Color(0xFF1E222B)
private val SlateCardElevated = Color(0xFF252A36)
private val SlateBorder = Color(0xFF374151)
private val SafetyAmber = Color(0xFFFFB300)
private val ActionAmber = Color(0xFFD97706)
private val CertifiedGreen = Color(0xFF10B981)
private val CertifiedGreenBg = Color(0xFF064E3B)
private val AmberBadgeBg = Color(0xFF451A03)
private val MutedText = Color(0xFF9CA3AF)
private val SubtleGrayBadge = Color(0xFF1F2937)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onModuleClick: (String) -> Unit,
    onOpenCertificate: () -> Unit = {},
    onOpenVerifier: () -> Unit = {},
    onLogout: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var currentUser by remember { mutableStateOf<User?>(null) }
    var progressMap by remember { mutableStateOf<Map<String, ModuleProgress>>(emptyMap()) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            val userId = SessionManager(context).getUserId() ?: 1L
            currentUser = db.userDao().findById(userId)
            val list = db.trainingDao().getProgress(userId)
            progressMap = list.associateBy { it.moduleId.uppercase() }
        }
    }

    val passedCount = cockpitModules.count { spec -> progressMap[spec.id]?.quizPassed == true }
    val arReadyCount = cockpitModules.count { spec -> progressMap[spec.id]?.arCompleted == true }
    val totalCount = cockpitModules.size
    val completionRatio = if (totalCount == 0) 0f else passedCount.toFloat() / totalCount.toFloat()
    val isFullyCertified = passedCount == totalCount

    // Sign Out Confirmation Dialog
    if (showSignOutDialog && onLogout != null) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            containerColor = SlateCard,
            titleContentColor = Color.White,
            textContentColor = MutedText,
            title = {
                Text(
                    text = "Confirm Operator Sign Out",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to sign out of the Jharkhand Mine Safety Cockpit? Your offline progress and certificates remain securely stored on this device."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626),
                        contentColor = Color.White
                    )
                ) {
                    Text("Sign Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showSignOutDialog = false },
                    border = BorderStroke(1.dp, SlateBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CockpitBg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Top Industrial Operator Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    border = BorderStroke(1.dp, SlateBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mine Zone Badge
                            Surface(
                                color = SafetyAmber.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, SafetyAmber.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "⛏ DHANBAD COALFIELD DIV-IV",
                                    color = SafetyAmber,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            if (onLogout != null) {
                                OutlinedButton(
                                    onClick = { showSignOutDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, SlateBorder),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = CockpitBg,
                                        contentColor = MutedText
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(
                                        text = "Sign Out",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Operator Monogram Avatar Badge
                            val workerName = currentUser?.name?.takeIf { it.isNotBlank() } ?: "Mine Operator"
                            val initials = workerName
                                .split(" ")
                                .filter { it.isNotBlank() }
                                .take(2)
                                .joinToString("") { it.first().uppercase() }
                                .ifEmpty { "MO" }

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(SlateCardElevated)
                                    .border(1.5.dp, SafetyAmber, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    color = SafetyAmber,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = workerName,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val workerPhone = currentUser?.phone ?: "ID-OFFLINE"
                                val workerRole = currentUser?.role ?: "WORKER"
                                Text(
                                    text = "MINE OPERATOR / $workerRole • 📞 $workerPhone",
                                    color = MutedText,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // 2. Certification Gauge Card
            item {
                val borderAccent = if (isFullyCertified) CertifiedGreen else SafetyAmber
                val trackFillColor = if (isFullyCertified) CertifiedGreen else SafetyAmber

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    border = BorderStroke(1.5.dp, borderAccent.copy(alpha = 0.85f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "SAFETY CERTIFICATION TELEMETRY",
                                    color = borderAccent,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$passedCount / $totalCount Modules Completed",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            // Circular Percentage Readout Pill
                            val percentInt = (completionRatio * 100).toInt()
                            Surface(
                                color = borderAccent.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, borderAccent.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "$percentInt%",
                                    color = borderAccent,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { completionRatio },
                            color = trackFillColor,
                            trackColor = Color(0xFF0F1115),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isFullyCertified) {
                                    "✓ All mandatory mine safety modules verified"
                                } else {
                                    "Complete all 7 modules for master HMAC QR clearance"
                                },
                                color = MutedText,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "AR Ready: $arReadyCount/$totalCount",
                                color = SafetyAmber,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Access Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onOpenCertificate,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ActionAmber,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                            ) {
                                Text(
                                    text = "My Certificates (QR)",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            OutlinedButton(
                                onClick = onOpenVerifier,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, SafetyAmber),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = CockpitBg,
                                    contentColor = SafetyAmber
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                            ) {
                                Text(
                                    text = "Offline Verifier",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Section Header for Modules
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CORE HAZARD & SAFETY DOMAINS",
                        color = MutedText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "TAP TO LAUNCH →",
                        color = SafetyAmber,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // 3. Modernized Module Cards
            items(cockpitModules, key = { it.id }) { spec ->
                val moduleProg = progressMap[spec.id]
                val quizPassed = moduleProg?.quizPassed == true
                val arCompleted = moduleProg?.arCompleted == true

                CockpitModuleCard(
                    spec = spec,
                    quizPassed = quizPassed,
                    arCompleted = arCompleted,
                    onClick = { onModuleClick(spec.id) }
                )
            }
        }
    }
}

@Composable
private fun CockpitModuleCard(
    spec: SafetyModuleSpec,
    quizPassed: Boolean,
    arCompleted: Boolean,
    onClick: () -> Unit
) {
    val badgeText: String
    val badgeTextColor: Color
    val badgeBgColor: Color
    val badgeBorderColor: Color

    when {
        quizPassed -> {
            badgeText = "✓ CERTIFIED"
            badgeTextColor = Color(0xFF34D399)
            badgeBgColor = CertifiedGreenBg.copy(alpha = 0.7f)
            badgeBorderColor = CertifiedGreen
        }
        arCompleted -> {
            badgeText = "AR READY • PENDING EXAM"
            badgeTextColor = SafetyAmber
            badgeBgColor = AmberBadgeBg.copy(alpha = 0.8f)
            badgeBorderColor = SafetyAmber
        }
        else -> {
            badgeText = "NOT STARTED"
            badgeTextColor = MutedText
            badgeBgColor = SubtleGrayBadge
            badgeBorderColor = SlateBorder
        }
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        border = BorderStroke(
            width = 1.dp,
            color = if (quizPassed) CertifiedGreen.copy(alpha = 0.5f) else SlateBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Safety Category Accent Strip
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(spec.accentColor)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Emblem Placeholder
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(spec.accentColor.copy(alpha = 0.15f))
                        .border(
                            width = 1.dp,
                            color = spec.accentColor.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = spec.symbol,
                        color = spec.accentColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Module Title, Hindi Subtitle, Domain Tag, and Status Badge
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Domain Tag Chip
                        Surface(
                            color = CockpitBg,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, spec.accentColor.copy(alpha = 0.45f))
                        ) {
                            Text(
                                text = spec.domainTag,
                                color = spec.accentColor,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Status Badge
                        Surface(
                            color = badgeBgColor,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, badgeBorderColor.copy(alpha = 0.7f))
                        ) {
                            Text(
                                text = badgeText,
                                color = badgeTextColor,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = spec.title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = spec.hindiSubtitle,
                        color = MutedText,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Subtle Forward Chevron Indicator
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(CockpitBg)
                        .border(1.dp, SlateBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "→",
                        color = SafetyAmber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
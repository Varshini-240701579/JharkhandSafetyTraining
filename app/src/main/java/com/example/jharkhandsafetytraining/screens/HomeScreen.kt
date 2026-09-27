package com.example.jharkhandsafetytraining.screens

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jharkhandsafetytraining.common.SessionManager
import com.example.jharkhandsafetytraining.data.AppDatabase
import com.example.jharkhandsafetytraining.data.ModuleProgress
import com.example.jharkhandsafetytraining.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

val safetyModules = listOf(
    "Fire & Explosion" to "FIRE",
    "Gas Leak" to "GAS",
    "Mine Collapse" to "COLLAPSE",
    "Flooding" to "FLOOD",
    "Oxygen Depletion" to "OXYGEN",
    "Machinery Safety" to "MACHINERY",
    "PPE Compliance" to "PPE"
)

private data class AccessibleModuleItem(
    val id: String,
    val pictogram: String,
    val accentColor: Color,
    val titleEn: String,
    val titleHi: String,
    val titleSat: String,
    val audioPromptHi: String,
    val audioPromptEn: String
)

private val accessibleModules = listOf(
    AccessibleModuleItem(
        id = "FIRE",
        pictogram = "🔥",
        accentColor = Color(0xFFEF4444),
        titleEn = "Fire & Explosion Safety",
        titleHi = "आग और विस्फोट से सुरक्षा",
        titleSat = "ᱥᱮᱸᱜᱮᱞ ᱟᱨ ᱚᱴᱮᱡ ᱠᱷᱚᱱ ᱵᱟᱧᱪᱟᱣ",
        audioPromptHi = "आग और विस्फोट से सुरक्षा। अलार्म बजने पर तुरंत सुरक्षित स्थान पर जाएं।",
        audioPromptEn = "Fire and Explosion Safety. Evacuate immediately when the alarm sounds."
    ),
    AccessibleModuleItem(
        id = "GAS",
        pictogram = "⚠",
        accentColor = Color(0xFFF59E0B),
        titleEn = "Gas Leak & Confined Space",
        titleHi = "गैस रिसाव और बंद स्थान",
        titleSat = "ᱜᱮᱥ ᱞᱤᱠ ᱟᱨ ᱵᱚᱸᱫᱽ ᱡᱟᱭᱜᱟ",
        audioPromptHi = "गैस रिसाव और बंद स्थान सुरक्षा। खदान में जाने से पहले गैस की जांच करें।",
        audioPromptEn = "Gas Leak and Confined Space Safety. Always check gas levels before entry."
    ),
    AccessibleModuleItem(
        id = "COLLAPSE",
        pictogram = "⛰",
        accentColor = Color(0xFFD97706),
        titleEn = "Mine Roof Collapse Safety",
        titleHi = "खदान छत धंसने से बचाव",
        titleSat = "ᱠᱷᱟᱫᱟᱱ ᱪᱷᱟᱛ ᱧᱩᱨᱩᱜ ᱠᱷᱚᱱ ᱵᱟᱧᱪᱟᱣ",
        audioPromptHi = "खदान छत धंसने से बचाव। बिना सपोर्ट वाली छत के नीचे कभी न जाएं।",
        audioPromptEn = "Mine Roof Collapse Safety. Never stand under an unsupported mine roof."
    ),
    AccessibleModuleItem(
        id = "FLOOD",
        pictogram = "🌊",
        accentColor = Color(0xFF38BDF8),
        titleEn = "Mine Flooding & Escape",
        titleHi = "खदान में पानी भराव से बचाव",
        titleSat = "ᱠᱷᱟᱫᱟᱱ ᱨᱮ ᱫᱟᱜ ᱯᱮᱨᱮᱡ ᱠᱷᱚᱱ ᱵᱟᱧᱪᱟᱣ",
        audioPromptHi = "खदान में पानी भराव से बचाव। पानी दिखने पर तुरंत ऊंचे रास्ते से बाहर निकलें।",
        audioPromptEn = "Mine Flooding and Escape. Move to higher levels immediately if water rises."
    ),
    AccessibleModuleItem(
        id = "OXYGEN",
        pictogram = "🫁",
        accentColor = Color(0xFF06B6D4),
        titleEn = "Oxygen Depletion & SCSR",
        titleHi = "ऑक्सीजन की कमी और श्वसन यंत्र",
        titleSat = "ᱚᱠᱥᱤᱡᱚᱱ ᱠᱚᱢᱚᱜ ᱟᱨ ᱥᱟᱸᱦᱮᱫ ᱡᱚᱱᱛᱨᱚ",
        audioPromptHi = "ऑक्सीजन की कमी से बचाव। सांस लेने में दिक्कत होने पर सेल्फ रेस्क्यूअर पहनें।",
        audioPromptEn = "Oxygen Depletion Safety. Wear your Self-Rescuer breathing device immediately."
    ),
    AccessibleModuleItem(
        id = "MACHINERY",
        pictogram = "⚙",
        accentColor = Color(0xFFEAB308),
        titleEn = "Heavy Machinery Safety",
        titleHi = "भारी मशीनों से सुरक्षा",
        titleSat = "ᱢᱟᱨᱟᱝ ᱢᱮᱥᱤᱱ ᱠᱷᱚᱱ ᱵᱟᱧᱪᱟᱣ",
        audioPromptHi = "भारी मशीनों से सुरक्षा। मरम्मत से पहले मशीन की बिजली पूरी तरह बंद करें।",
        audioPromptEn = "Heavy Machinery Safety. Lockout and switch off power before any repair."
    ),
    AccessibleModuleItem(
        id = "PPE",
        pictogram = "⛑",
        accentColor = Color(0xFF10B981),
        titleEn = "Helmet, Boots & PPE Gear",
        titleHi = "हेलमेट, जूते और सुरक्षा कवच",
        titleSat = "ᱦᱮᱞᱢᱮᱴ, ᱡᱩᱛᱟᱹ ᱟᱨ ᱥᱮᱯᱷᱴᱤ ᱞᱩᱜᱽᱲᱤ",
        audioPromptHi = "हेलमेट, जूते और सुरक्षा कवच। काम शुरू करने से पहले पूरा पीपीई पहनें।",
        audioPromptEn = "Helmet, Boots and PPE Gear. Wear all protective gear before starting work."
    )
)

// High-Visibility Accessible Color Palette (PS-26041)
private val WarmCharcoalBg = Color(0xFF12141A)
private val CardSurface = Color(0xFF1C2029)
private val CardBorder = Color(0xFF333A48)
private val SafetyWarningYellow = Color(0xFFF59E0B)
private val HighContrastWhite = Color(0xFFFFFFFF)
private val ComplianceGreen = Color(0xFF10B981)
private val ComplianceGreenDarkBg = Color(0xFF064E3B)
private val PendingAmberDarkBg = Color(0xFF451A03)
private val SoftSilverText = Color(0xFFD1D5DB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onModuleClick: (String) -> Unit,
    onOpenCertificate: () -> Unit = {},
    onOpenVerifier: () -> Unit = {},
    onLogout: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE) }

    var selectedLanguage by remember {
        mutableStateOf(prefs.getString("user_language", "hi") ?: "hi")
    }
    var currentUser by remember { mutableStateOf<User?>(null) }
    var progressMap by remember { mutableStateOf<Map<String, ModuleProgress>>(emptyMap()) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    // Optional Android TextToSpeech for Audio-Visual Accessibility ("🔊")
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.language = Locale.forLanguageTag("hi-IN")
            }
        }
        ttsEngine = engine
        onDispose {
            engine?.stop()
            engine?.shutdown()
        }
    }

    fun speakModuleAudio(item: AccessibleModuleItem) {
        val textToSpeak = if (selectedLanguage == "en") item.audioPromptEn else item.audioPromptHi
        val locale = if (selectedLanguage == "en") Locale.US else Locale.forLanguageTag("hi-IN")
        ttsEngine?.language = locale
        ttsEngine?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, item.id)
        Toast.makeText(context, "🔊 $textToSpeak", Toast.LENGTH_SHORT).show()
    }

    fun onSelectLanguage(langCode: String) {
        selectedLanguage = langCode
        prefs.edit().putString("user_language", langCode).apply()
        scope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            val userId = SessionManager(context).getUserId() ?: 1L
            db.userDao().updateLanguage(userId, langCode)
        }
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            val userId = SessionManager(context).getUserId() ?: 1L
            val loadedUser = db.userDao().findById(userId)
            currentUser = loadedUser
            if (loadedUser?.language?.isNotBlank() == true && !prefs.contains("user_language")) {
                selectedLanguage = loadedUser.language
            }
            val list = db.trainingDao().getProgress(userId)
            progressMap = list.associateBy { it.moduleId.uppercase() }
        }
    }

    val passedCount = accessibleModules.count { progressMap[it.id]?.quizPassed == true }
    val totalCount = accessibleModules.size
    val progressRatio = if (totalCount == 0) 0f else passedCount.toFloat() / totalCount.toFloat()

    // Sign Out Confirmation Dialog
    if (showSignOutDialog && onLogout != null) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            containerColor = CardSurface,
            titleContentColor = HighContrastWhite,
            textContentColor = SoftSilverText,
            title = {
                Text(
                    text = "लॉग आउट / Sign Out?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "क्या आप लॉग आउट करना चाहते हैं? / Do you want to sign out?"
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
                        contentColor = HighContrastWhite
                    )
                ) {
                    Text("Sign Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showSignOutDialog = false },
                    border = BorderStroke(1.dp, CardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HighContrastWhite)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = WarmCharcoalBg,
        topBar = {
            Surface(
                color = CardSurface,
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Top Row: Title + OFFLINE VERIFIER Icon-Button + Sign Out
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (selectedLanguage) {
                                    "hi" -> "झारखंड खान सुरक्षा"
                                    "sat" -> "ᱡᱷᱟᱨᱠᱷᱚᱸᱰ ᱠᱷᱟᱫᱟᱱ ᱥᱮᱯᱷᱴᱤ"
                                    else -> "Jharkhand Mine Safety"
                                },
                                color = HighContrastWhite,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            currentUser?.let { worker ->
                                Text(
                                    text = "⛑ ${worker.name}",
                                    color = SafetyWarningYellow,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // "OFFLINE VERIFIER" Icon-Button in Top Header
                            OutlinedButton(
                                onClick = onOpenVerifier,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.5.dp, SafetyWarningYellow),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = WarmCharcoalBg,
                                    contentColor = SafetyWarningYellow
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier.height(42.dp)
                            ) {
                                Text(
                                    text = "🔍 OFFLINE VERIFIER",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            if (onLogout != null) {
                                OutlinedButton(
                                    onClick = { showSignOutDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, CardBorder),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = WarmCharcoalBg,
                                        contentColor = SoftSilverText
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                    modifier = Modifier.height(42.dp)
                                ) {
                                    Text(
                                        text = "⏻",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 1. Persistent Trilingual Bar (Top of Screen)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(WarmCharcoalBg, RoundedCornerShape(12.dp))
                            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val languages = listOf(
                            "en" to "English",
                            "hi" to "हिन्दी (Hindi)",
                            "sat" to "संताली (Santali)"
                        )
                        languages.forEach { (code, label) ->
                            val isSelected = selectedLanguage == code
                            Surface(
                                onClick = { onSelectLanguage(code) },
                                shape = RoundedCornerShape(9.dp),
                                color = if (isSelected) SafetyWarningYellow else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color(0xFF12141A) else HighContrastWhite,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        // 4. Clear Action Footer: Prominent Large DGMS Certificate Button
        bottomBar = {
            Surface(
                color = CardSurface,
                shadowElevation = 12.dp,
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = onOpenCertificate,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (passedCount == totalCount) ComplianceGreen else SafetyWarningYellow,
                            contentColor = Color(0xFF12141A)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                    ) {
                        Text(
                            text = "🎓 MY DGMS CERTIFICATE / मेरा प्रमाणपत्र",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    ) { padding ->
        // 2. De-congested, Card-Based Industrial Cockpit with >= 16dp Vertical Spacing
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(WarmCharcoalBg)
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 18.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Simple, High-Contrast Visual Progress Summary Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = if (passedCount == totalCount) ComplianceGreen else SafetyWarningYellow
                    )
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
                            Text(
                                text = when (selectedLanguage) {
                                    "hi" -> "प्रशिक्षण प्रगति (Training Progress)"
                                    "sat" -> "ᱴᱨᱮᱱᱤᱝ ᱯᱨᱚᱜᱨᱮᱥ (Training Progress)"
                                    else -> "Safety Training Progress"
                                },
                                color = HighContrastWhite,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Surface(
                                color = if (passedCount == totalCount) {
                                    ComplianceGreenDarkBg
                                } else {
                                    PendingAmberDarkBg
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (passedCount == totalCount) ComplianceGreen else SafetyWarningYellow
                                )
                            ) {
                                Text(
                                    text = "$passedCount / $totalCount",
                                    color = if (passedCount == totalCount) ComplianceGreen else SafetyWarningYellow,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { progressRatio },
                            color = if (passedCount == totalCount) ComplianceGreen else SafetyWarningYellow,
                            trackColor = WarmCharcoalBg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                    }
                }
            }

            // 3. Accessible Module Cards (Audio-Visual First, >= 90dp Height)
            items(accessibleModules, key = { it.id }) { item ->
                val isPassed = progressMap[item.id]?.quizPassed == true

                AccessibleSafetyModuleCard(
                    item = item,
                    selectedLanguage = selectedLanguage,
                    isPassed = isPassed,
                    onCardClick = { onModuleClick(item.id) },
                    onSpeakClick = { speakModuleAudio(item) }
                )
            }
        }
    }
}

@Composable
private fun AccessibleSafetyModuleCard(
    item: AccessibleModuleItem,
    selectedLanguage: String,
    isPassed: Boolean,
    onCardClick: () -> Unit,
    onSpeakClick: () -> Unit
) {
    // Primary title in selected language, secondary in English (or Hindi if English is selected)
    val primaryTitle = when (selectedLanguage) {
        "hi" -> item.titleHi
        "sat" -> item.titleSat
        else -> item.titleEn
    }
    val secondaryTitle = when (selectedLanguage) {
        "en" -> item.titleHi
        else -> item.titleEn
    }

    Card(
        onClick = onCardClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 98.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (isPassed) ComplianceGreen else CardBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Large, Clear Hazard Category Pictogram on Left
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(item.accentColor.copy(alpha = 0.18f))
                    .border(
                        width = 1.5.dp,
                        color = item.accentColor,
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.pictogram,
                    fontSize = 30.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Dual-Language Title + Prominent Status Indicator Pill
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = primaryTitle,
                    color = HighContrastWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 23.sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = secondaryTitle,
                    color = SoftSilverText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Prominent, Simplified Status Pill
                Surface(
                    color = if (isPassed) ComplianceGreenDarkBg else PendingAmberDarkBg,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isPassed) ComplianceGreen else SafetyWarningYellow
                    )
                ) {
                    Text(
                        text = if (isPassed) {
                            "✓ PASSED / प्रमाणित"
                        } else {
                            "DRILL PENDING / अभ्यास शेष"
                        },
                        color = if (isPassed) Color(0xFF34D399) else SafetyWarningYellow,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Speaker Button ("🔊") for Audio Narration Playback
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(SafetyWarningYellow.copy(alpha = 0.16f))
                    .border(1.5.dp, SafetyWarningYellow, CircleShape)
                    .clickable(onClick = onSpeakClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔊",
                    fontSize = 22.sp
                )
            }
        }
    }
}
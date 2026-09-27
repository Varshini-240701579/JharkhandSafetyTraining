package com.example.jharkhandsafetytraining.screens

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jharkhandsafetytraining.common.SessionManager
import com.example.jharkhandsafetytraining.data.AppDatabase
import com.example.jharkhandsafetytraining.data.ModuleProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private data class VisualModuleBriefing(
    val id: String,
    val pictogram: String,
    val accentColor: Color,
    val titleEn: String,
    val titleHi: String,
    val titleSat: String,
    val hazardRuleEn: String,
    val hazardRuleHi: String,
    val hazardRuleSat: String,
    val ppeRuleEn: String,
    val ppeRuleHi: String,
    val ppeRuleSat: String,
    val exitRuleEn: String,
    val exitRuleHi: String,
    val exitRuleSat: String,
    val audioScriptHi: String,
    val audioScriptEn: String
)

private val visualBriefings = mapOf(
    "FIRE" to VisualModuleBriefing(
        id = "FIRE",
        pictogram = "🔥",
        accentColor = Color(0xFFEF4444),
        titleEn = "Fire & Explosion Safety",
        titleHi = "आग और विस्फोट से सुरक्षा",
        titleSat = "ᱥᱮᱸᱜᱮᱞ ᱟᱨ ᱚᱴᱮᱡ ᱠᱷᱚᱱ ᱵᱟᱧᱪᱟᱣ",
        hazardRuleEn = "Watch for smoke, sparks, or fire alarms",
        hazardRuleHi = "आग, धुएं और अलार्म से सावधान रहें",
        hazardRuleSat = "ᱥᱮᱸᱜᱮᱞ, ᱫᱷᱩᱸᱣᱟᱹ ᱟᱨ ᱜᱷᱟᱹᱱᱴᱤ ᱧᱮᱞ ᱢᱮ",
        ppeRuleEn = "Wear helmet, boots, and self-rescuer mask",
        ppeRuleHi = "हेलमेट, जूते और बचाव मास्क पहनें",
        ppeRuleSat = "ᱦᱮᱞᱢᱮᱴ, ᱡᱩᱛᱟᱹ ᱟᱨ ᱢᱟᱥᱠ ᱦᱚᱨᱚᱜ ᱢᱮ",
        exitRuleEn = "Use marked emergency exit, never use lift",
        exitRuleHi = "आपातकालीन रास्ते से निकलें, लिफ्ट न लें",
        exitRuleSat = "ᱵᱟᱧᱪᱟᱣ ᱰᱟᱦᱟᱨ ᱛᱮ ᱚᱰᱚᱠᱚᱜ ᱢᱮ",
        audioScriptHi = "आग और विस्फोट सुरक्षा। आग का अलार्म सुनते ही काम रोकें। हेलमेट और बचाव मास्क पहनें। आपातकालीन रास्ते से बाहर निकलें।",
        audioScriptEn = "Fire and Explosion Safety. Stop work when alarm sounds. Wear helmet and self-rescuer mask. Use marked emergency exit."
    ),
    "GAS" to VisualModuleBriefing(
        id = "GAS",
        pictogram = "⚠",
        accentColor = Color(0xFFF59E0B),
        titleEn = "Gas Leak & Confined Space",
        titleHi = "गैस रिसाव और बंद स्थान",
        titleSat = "ᱜᱮᱥ ᱞᱤᱠ ᱟᱨ ᱵᱚᱸᱫᱽ ᱡᱟᱭᱜᱟ",
        hazardRuleEn = "Check methane gas detector before entering",
        hazardRuleHi = "प्रवेश से पहले गैस मीटर जांचें",
        hazardRuleSat = "ᱵᱚᱞᱚᱱ ᱢᱟᱲᱟᱝ ᱜᱮᱥ ᱢᱤᱴᱚᱨ ᱧᱮᱞ ᱢᱮ",
        ppeRuleEn = "Clip on gas monitor and breathing kit",
        ppeRuleHi = "गैस डिटेक्टर और श्वसन किट लगाएं",
        ppeRuleSat = "ᱜᱮᱥ ᱰᱤᱴᱮᱠᱴᱚᱨ ᱟᱨ ᱢᱟᱥᱠ ᱦᱚᱨᱚᱜ ᱢᱮ",
        exitRuleEn = "Move immediately toward fresh intake air",
        exitRuleHi = "तुरंत ताजी हवा की ओर निकलें",
        exitRuleSat = "ᱞᱚᱜᱚᱱ ᱯᱷᱟᱨᱪᱟ ᱦᱚᱭ ᱥᱮᱫ ᱚᱰᱚᱠᱚᱜ ᱢᱮ",
        audioScriptHi = "गैस रिसाव सुरक्षा। प्रवेश से पहले गैस मीटर जांचें। अलार्म बजने पर तुरंत ताजी हवा की ओर बाहर निकलें।",
        audioScriptEn = "Gas Leak Safety. Check gas detector before entering. Move immediately toward fresh air if alarm sounds."
    ),
    "COLLAPSE" to VisualModuleBriefing(
        id = "COLLAPSE",
        pictogram = "⛰",
        accentColor = Color(0xFFD97706),
        titleEn = "Mine Roof Collapse Safety",
        titleHi = "खदान छत धंसने से बचाव",
        titleSat = "ᱠᱷᱟᱫᱟᱱ ᱪᱷᱟᱛ ᱧᱩᱨᱩᱜ ᱠᱷᱚᱱ ᱵᱟᱧᱪᱟᱣ",
        hazardRuleEn = "Never stand under unsupported mine roof",
        hazardRuleHi = "बिना सपोर्ट छत के नीचे न जाएं",
        hazardRuleSat = "ᱵᱤᱱᱟᱹ ᱴᱮᱠᱟᱣ ᱪᱷᱟᱛ ᱞᱟᱛᱟᱨ ᱟᱞᱚᱢ ᱪᱟᱞᱟᱜᱼᱟ",
        ppeRuleEn = "Wear hard hat with chin strap",
        ppeRuleHi = "मजबूत सुरक्षा हेलमेट और जूते पहनें",
        ppeRuleSat = "ᱠᱮᱴᱮᱡ ᱦᱮᱞᱢᱮᱴ ᱟᱨ ᱡᱩᱛᱟᱹ ᱦᱚᱨᱚᱜ ᱢᱮ",
        exitRuleEn = "Retreat to bolted gallery if rock cracks",
        exitRuleHi = "पत्थर गिरने पर सुरक्षित सुरंग में जाएं",
        exitRuleSat = "ᱫᱷᱤᱨᱤ ᱧᱩᱨ ᱞᱮᱱᱠᱷᱟᱱ ᱵᱟᱧᱪᱟᱣ ᱴᱷᱟᱶ ᱪᱟᱞᱟᱜ ᱢᱮ",
        audioScriptHi = "खदान छत सुरक्षा। बिना सपोर्ट छत के नीचे कभी न जाएं। पत्थर गिरने की आवाज पर तुरंत सुरक्षित सुरंग में लौटें।",
        audioScriptEn = "Mine Roof Safety. Never stand under an unsupported roof. Retreat to bolted gallery if rock cracks."
    ),
    "FLOOD" to VisualModuleBriefing(
        id = "FLOOD",
        pictogram = "🌊",
        accentColor = Color(0xFF38BDF8),
        titleEn = "Mine Flooding & Escape",
        titleHi = "खदान में पानी भराव से बचाव",
        titleSat = "ᱠᱷᱟᱫᱟᱱ ᱨᱮ ᱫᱟᱜ ᱯᱮᱨᱮᱡ ᱠᱷᱚᱱ ᱵᱟᱧᱪᱟᱣ",
        hazardRuleEn = "Watch for wet walls and water seepage",
        hazardRuleHi = "दीवार से पानी रिसाव पर ध्यान दें",
        hazardRuleSat = "ᱠᱟᱸᱛ ᱠᱷᱚᱱ ᱫᱟᱜ ᱡᱚᱨᱚᱜ ᱧᱮᱞ ᱢᱮ",
        ppeRuleEn = "Wear high-grip waterproof boots and lamp",
        ppeRuleHi = "वाटरप्रूफ सुरक्षा जूते और लैंप पहनें",
        ppeRuleSat = "ᱫᱟᱜ ᱡᱩᱛᱟᱹ ᱟᱨ ᱵᱟᱹᱛᱤ ᱦᱚᱨᱚᱜ ᱢᱮ",
        exitRuleEn = "Climb immediately to higher mine levels",
        exitRuleHi = "पानी बढ़ने पर तुरंत ऊंचे रास्ते जाएं",
        exitRuleSat = "ᱞᱚᱜᱚᱱ ᱩᱥᱩᱞ ᱰᱟᱦᱟᱨ ᱥᱮᱫ ᱨᱟᱠᱟᱵ ᱢᱮ",
        audioScriptHi = "जल भराव से सुरक्षा। दीवार से पानी रिसाव दिखने पर सतर्क रहें और तुरंत ऊंचे रास्ते से बाहर निकलें।",
        audioScriptEn = "Mine Flooding Safety. Watch for water seepage on walls and climb immediately to higher mine levels."
    ),
    "OXYGEN" to VisualModuleBriefing(
        id = "OXYGEN",
        pictogram = "🫁",
        accentColor = Color(0xFF06B6D4),
        titleEn = "Oxygen Depletion & SCSR",
        titleHi = "ऑक्सीजन की कमी और श्वसन यंत्र",
        titleSat = "ᱚᱠᱥᱤᱡᱚᱱ ᱠᱚᱢᱚᱜ ᱟᱨ ᱥᱟᱸᱦᱮᱫ ᱡᱚᱱᱛᱨᱚ",
        hazardRuleEn = "Danger if oxygen drops below 19.5%",
        hazardRuleHi = "ऑक्सीजन 19.5% से कम होना खतरनाक है",
        hazardRuleSat = "ᱚᱠᱥᱤᱡᱚᱱ 19.5% ᱠᱷᱚᱱ ᱠᱚᱢ ᱞᱮᱱᱠᱷᱟᱱ ᱵᱤᱯᱚᱫᱽ",
        ppeRuleEn = "Carry Self-Rescuer (SCSR) on your belt",
        ppeRuleHi = "बेल्ट पर सेल्फ-रेस्क्यूअर (SCSR) हमेशा रखें",
        ppeRuleSat = "ᱰᱟᱸᱰᱟ ᱨᱮ SCSR ᱡᱚᱱᱛᱨᱚ ᱫᱚᱦᱚᱭ ᱢᱮ",
        exitRuleEn = "Put on SCSR mouthpiece and walk out",
        exitRuleHi = "तुरंत मास्क लगाकर शांत कदमों से निकलें",
        exitRuleSat = "ᱢᱟᱥᱠ ᱞᱟᱜᱟᱣ ᱠᱟᱛᱮ ᱵᱟᱦᱨᱮ ᱚᱰᱚᱠᱚᱜ ᱢᱮ",
        audioScriptHi = "ऑक्सीजन सुरक्षा। हवा में ऑक्सीजन साढ़े उन्नीस प्रतिशत से कम होने पर तुरंत सेल्फ रेस्क्यूअर मास्क पहनें और बाहर निकलें।",
        audioScriptEn = "Oxygen Safety. If oxygen drops below 19.5 percent, put on your Self-Rescuer mouthpiece immediately and walk out."
    ),
    "MACHINERY" to VisualModuleBriefing(
        id = "MACHINERY",
        pictogram = "⚙",
        accentColor = Color(0xFFEAB308),
        titleEn = "Heavy Machinery Safety",
        titleHi = "भारी मशीनों से सुरक्षा",
        titleSat = "ᱢᱟᱨᱟᱝ ᱢᱮᱥᱤᱱ ᱠᱷᱚᱱ ᱵᱟᱧᱪᱟᱣ",
        hazardRuleEn = "Lockout power switch before any repair",
        hazardRuleHi = "मरम्मत से पहले बिजली स्विच लॉक करें",
        hazardRuleSat = "ᱡᱩᱛ ᱢᱟᱲᱟᱝ ᱵᱤᱡᱽᱞᱤ ᱥᱩᱭᱤᱪ ᱵᱚᱸᱫᱽ ᱢᱮ",
        ppeRuleEn = "Wear fitted vest, gloves, and ear plugs",
        ppeRuleHi = "दस्ताने, हेलमेट और चुस्त कपड़े पहनें",
        ppeRuleSat = "ᱛᱤ ᱢोजा, ᱦᱮᱞᱢᱮᱴ ᱟᱨ ᱡᱮᱠᱮᱴ ᱦᱚᱨᱚᱜ ᱢᱮ",
        exitRuleEn = "Stay clear of moving conveyor belts",
        exitRuleHi = "चलती कन्वेयर बेल्ट से दूर रहें",
        exitRuleSat = "ᱪᱟᱹᱞᱩ ᱢᱮᱥᱤᱱ ᱠᱷᱚᱱ ᱥᱟᱺᱜᱤᱧ ᱨᱮ ᱛᱟᱦᱮᱸᱱ ᱢᱮ",
        audioScriptHi = "भारी मशीन सुरक्षा। मरम्मत से पहले बिजली स्विच लॉक करें। चलती कन्वेयर बेल्ट और मशीन गार्ड से दूर रहें।",
        audioScriptEn = "Heavy Machinery Safety. Lockout power switch before any repair. Stay clear of moving conveyor belts."
    ),
    "PPE" to VisualModuleBriefing(
        id = "PPE",
        pictogram = "⛑",
        accentColor = Color(0xFF10B981),
        titleEn = "Helmet, Boots & PPE Gear",
        titleHi = "हेलमेट, जूते और सुरक्षा कवच",
        titleSat = "ᱦᱮᱞᱢᱮᱴ, ᱡᱩᱛᱟᱹ ᱟᱨ ᱥᱮᱯᱷᱴᱤ ᱞᱩᱜᱽᱲᱤ",
        hazardRuleEn = "Never use cracked helmet or torn harness",
        hazardRuleHi = "टूटा हेलमेट या फटी बेल्ट न पहनें",
        hazardRuleSat = "ᱯᱷᱟᱴᱟᱣ ᱦᱮᱞᱢᱮᱴ ᱟᱞᱚᱢ ᱦᱚᱨᱚᱜᱟ",
        ppeRuleEn = "Helmet, cap-lamp, steel boots, reflective vest",
        ppeRuleHi = "हेलमेट, लैंप, स्टील जूते और जैकेट पहनें",
        ppeRuleSat = "ᱦᱮᱞᱢᱮᱴ, ᱵᱟᱹᱛᱤ, ᱡᱩᱛᱟᱹ ᱟᱨ ᱡᱮᱠᱮᱴ ᱦᱚᱨᱚᱜ ᱢᱮ",
        exitRuleEn = "Replace damaged safety gear before shift",
        exitRuleHi = "खराब उपकरण तुरंत स्टोर से बदलें",
        exitRuleSat = "ᱵᱟᱹᱲᱤᱡ ᱥᱟᱢᱟᱱ ᱞᱚᱜᱚᱱ ᱵᱚᱫᱚᱞ ᱢᱮ",
        audioScriptHi = "पीपीई सुरक्षा। खदान में जाने से पहले हेलमेट, लैंप, स्टील जूते और रिफ्लेक्टिव जैकेट पहनें। टूटा हेलमेट तुरंत बदलें।",
        audioScriptEn = "PPE Compliance. Wear helmet, cap-lamp, steel boots, and reflective vest. Replace damaged safety gear immediately."
    )
)

// High-Visibility Accessible Palette
private val WarmCharcoalBg = Color(0xFF12141A)
private val CardSurface = Color(0xFF1C2029)
private val CardInnerSurface = Color(0xFF151820)
private val CardBorder = Color(0xFF333A48)
private val SafetyWarningYellow = Color(0xFFF59E0B)
private val HighContrastWhite = Color(0xFFFFFFFF)
private val ComplianceGreen = Color(0xFF10B981)
private val ComplianceGreenDarkBg = Color(0xFF064E3B)
private val PendingAmberDarkBg = Color(0xFF451A03)
private val SoftSilverText = Color(0xFFD1D5DB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleDetailScreen(
    moduleId: String,
    onStartAR: () -> Unit = {},
    onStartQuiz: () -> Unit = {},
    onLaunchAr: (moduleId: String) -> Unit = { onStartAR() },
    onTakeQuiz: (moduleId: String) -> Unit = { onStartQuiz() },
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val normalizedId = moduleId.uppercase()
    val prefs = remember { context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE) }

    var selectedLanguage by remember {
        mutableStateOf(prefs.getString("user_language", "hi") ?: "hi")
    }
    var progress by remember { mutableStateOf<ModuleProgress?>(null) }

    val briefing = remember(normalizedId) {
        visualBriefings[normalizedId] ?: visualBriefings["FIRE"]!!.copy(
            id = normalizedId,
            titleEn = "Safety Module: $normalizedId"
        )
    }

    // Android TextToSpeech for Audio Narration Bar ("🔊")
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

    fun speakInstructions() {
        val script = if (selectedLanguage == "en") briefing.audioScriptEn else briefing.audioScriptHi
        val locale = if (selectedLanguage == "en") Locale.US else Locale.forLanguageTag("hi-IN")
        ttsEngine?.language = locale
        ttsEngine?.speak(script, TextToSpeech.QUEUE_FLUSH, null, normalizedId)
        Toast.makeText(context, "🔊 $script", Toast.LENGTH_LONG).show()
    }

    suspend fun loadModuleData() {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            val userId = SessionManager(context).getUserId() ?: 1L
            val user = db.userDao().findById(userId)
            if (user?.language?.isNotBlank() == true && !prefs.contains("user_language")) {
                selectedLanguage = user.language
            }
            progress = db.trainingDao().getModuleProgress(userId, normalizedId)
        }
    }

    LaunchedEffect(normalizedId) {
        loadModuleData()
    }

    fun triggerArDrill() {
        scope.launch {
            withContext(Dispatchers.IO) {
                val db = AppDatabase.getInstance(context)
                val userId = SessionManager(context).getUserId() ?: 1L
                db.trainingDao().markArCompleted(userId, normalizedId)
            }
            loadModuleData()
            Toast.makeText(
                context,
                "✓ AR Safety Drill Completed / कैमरा अभ्यास पूर्ण",
                Toast.LENGTH_SHORT
            ).show()
        }
        onLaunchAr(normalizedId)
    }

    val arCompleted = progress?.arCompleted == true
    val quizPassed = progress?.quizPassed == true

    val primaryTitle = when (selectedLanguage) {
        "hi" -> briefing.titleHi
        "sat" -> briefing.titleSat
        else -> briefing.titleEn
    }
    val secondaryTitle = when (selectedLanguage) {
        "en" -> briefing.titleHi
        else -> briefing.titleEn
    }

    Scaffold(
        containerColor = WarmCharcoalBg,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardSurface,
                    titleContentColor = HighContrastWhite,
                    navigationIconContentColor = SafetyWarningYellow
                ),
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(
                            text = "← Back",
                            color = SafetyWarningYellow,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                },
                title = {
                    Text(
                        text = primaryTitle,
                        color = HighContrastWhite,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                actions = {
                    // Quick language toggle chip
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        listOf("en" to "EN", "hi" to "हिं", "sat" to "SAT").forEach { (code, label) ->
                            val isSelected = selectedLanguage == code
                            Surface(
                                onClick = {
                                    selectedLanguage = code
                                    prefs.edit().putString("user_language", code).apply()
                                },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) SafetyWarningYellow else WarmCharcoalBg,
                                border = BorderStroke(1.dp, if (isSelected) SafetyWarningYellow else CardBorder)
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) WarmCharcoalBg else HighContrastWhite,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            )
        },
        // 4. Bottom Action Buttons (56dp Primary AR Button + Secondary Quiz Button)
        bottomBar = {
            Surface(
                color = CardSurface,
                shadowElevation = 12.dp,
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Full-width, high-contrast action button (height 56dp)
                    Button(
                        onClick = { triggerArDrill() },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SafetyWarningYellow,
                            contentColor = WarmCharcoalBg
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            text = "🥽 START AR DRILL / अभ्यास शुरू करें",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Secondary Button: TAKE QUIZ / परीक्षा दें
                    OutlinedButton(
                        onClick = { onTakeQuiz(normalizedId) },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            width = 1.5.dp,
                            color = if (quizPassed) ComplianceGreen else SafetyWarningYellow
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = WarmCharcoalBg,
                            contentColor = if (quizPassed) Color(0xFF34D399) else HighContrastWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = if (quizPassed) {
                                "✓ TAKE QUIZ / परीक्षा दें (PASSED)"
                            } else {
                                "📝 TAKE QUIZ / परीक्षा दें"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WarmCharcoalBg)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. De-congested Header with Prominent Bilingual Hazard Title & Audio Narration Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = BorderStroke(1.5.dp, briefing.accentColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(briefing.accentColor.copy(alpha = 0.18f))
                                .border(1.5.dp, briefing.accentColor, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = briefing.pictogram,
                                fontSize = 32.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = primaryTitle,
                                color = HighContrastWhite,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.ExtraBold,
                                lineHeight = 26.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = secondaryTitle,
                                color = SoftSilverText,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Audio Narration Bar
                    Surface(
                        onClick = { speakInstructions() },
                        color = SafetyWarningYellow.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, SafetyWarningYellow),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "🔊 Tap to listen to instructions / निर्देश सुनने के लिए दबाएं",
                                color = SafetyWarningYellow,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // 2. Visual Action Steps (Numbered Graphic Tiles)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Step 1 Tile: Safety Drill in AR
                VisualStepTile(
                    stepNumber = "1",
                    symbol = "🥽",
                    titlePrimary = "Safety Drill in AR",
                    titleSecondary = "कैमरा अभ्यास",
                    statusText = if (arCompleted) "Completed ✓" else "Start Drill",
                    isComplete = arCompleted,
                    onClick = { triggerArDrill() },
                    modifier = Modifier.weight(1f)
                )

                // Step 2 Tile: Comprehension Exam
                val step2Status = when {
                    quizPassed -> "Passed ✓"
                    arCompleted -> "Start Exam"
                    else -> "Must complete AR first"
                }
                VisualStepTile(
                    stepNumber = "2",
                    symbol = "📝",
                    titlePrimary = "Comprehension Exam",
                    titleSecondary = "जांच परीक्षा",
                    statusText = step2Status,
                    isComplete = quizPassed,
                    onClick = { onTakeQuiz(normalizedId) },
                    modifier = Modifier.weight(1f)
                )
            }

            // 3. Mandatory Safety Rules (SOP) - 3 Simple Visual Bullet Cards (< 8 words per point)
            Text(
                text = when (selectedLanguage) {
                    "hi" -> "अनिवार्य सुरक्षा नियम (Safety Rules)"
                    "sat" -> "ᱡᱚᱨᱩᱨᱤ ᱥᱮᱯᱷᱴᱤ നിയᱚᱢ (Safety Rules)"
                    else -> "Mandatory Safety Rules (SOP)"
                },
                color = HighContrastWhite,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            // Bullet Card 1: ⚠️ Hazard Warning
            VisualSopBulletCard(
                symbol = "⚠️",
                categoryLabel = "HAZARD WARNING / खतरा चेतावनी",
                rulePrimary = when (selectedLanguage) {
                    "hi" -> briefing.hazardRuleHi
                    "sat" -> briefing.hazardRuleSat
                    else -> briefing.hazardRuleEn
                },
                ruleSecondary = if (selectedLanguage == "en") briefing.hazardRuleHi else briefing.hazardRuleEn,
                accentColor = Color(0xFFEF4444)
            )

            // Bullet Card 2: 🦺 Required PPE
            VisualSopBulletCard(
                symbol = "🦺",
                categoryLabel = "REQUIRED PPE / सुरक्षा कवच",
                rulePrimary = when (selectedLanguage) {
                    "hi" -> briefing.ppeRuleHi
                    "sat" -> briefing.ppeRuleSat
                    else -> briefing.ppeRuleEn
                },
                ruleSecondary = if (selectedLanguage == "en") briefing.ppeRuleHi else briefing.ppeRuleEn,
                accentColor = SafetyWarningYellow
            )

            // Bullet Card 3: 🚪 Emergency Route
            VisualSopBulletCard(
                symbol = "🚪",
                categoryLabel = "EMERGENCY ROUTE / बचाव मार्ग",
                rulePrimary = when (selectedLanguage) {
                    "hi" -> briefing.exitRuleHi
                    "sat" -> briefing.exitRuleSat
                    else -> briefing.exitRuleEn
                },
                ruleSecondary = if (selectedLanguage == "en") briefing.exitRuleHi else briefing.exitRuleEn,
                accentColor = ComplianceGreen
            )
        }
    }
}

@Composable
private fun VisualStepTile(
    stepNumber: String,
    symbol: String,
    titlePrimary: String,
    titleSecondary: String,
    statusText: String,
    isComplete: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = 152.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (isComplete) ComplianceGreen else SafetyWarningYellow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Step Number Circle
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (isComplete) ComplianceGreen else SafetyWarningYellow),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stepNumber,
                        color = WarmCharcoalBg,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }

                Text(
                    text = symbol,
                    fontSize = 26.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = titlePrimary,
                color = HighContrastWhite,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = titleSecondary,
                color = SoftSilverText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = if (isComplete) ComplianceGreenDarkBg else PendingAmberDarkBg,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isComplete) ComplianceGreen else SafetyWarningYellow
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = statusText,
                    color = if (isComplete) Color(0xFF34D399) else SafetyWarningYellow,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun VisualSopBulletCard(
    symbol: String,
    categoryLabel: String,
    rulePrimary: String,
    ruleSecondary: String,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.16f))
                    .border(1.5.dp, accentColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = symbol,
                    fontSize = 26.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = categoryLabel,
                    color = accentColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = rulePrimary,
                    color = HighContrastWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = ruleSecondary,
                    color = SoftSilverText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
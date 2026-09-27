package com.example.jharkhandsafetytraining.screens

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jharkhandsafetytraining.common.SessionManager
import com.example.jharkhandsafetytraining.data.AppDatabase
import com.example.jharkhandsafetytraining.data.ModuleProgress
import com.example.jharkhandsafetytraining.data.QuizAttempt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class ModuleBriefingData(
    val id: String,
    val title: String,
    val hindiTitle: String,
    val domainChip: String,
    val riskBadge: String,
    val accentColor: Color,
    val hazardOverview: String,
    val ppeChecklist: List<String>,
    val sopProtocols: List<String>
)

private val moduleBriefings = mapOf(
    "FIRE" to ModuleBriefingData(
        id = "FIRE",
        title = "Fire & Explosion Suppression",
        hindiTitle = "आग और विस्फोट सुरक्षा प्रोटोकॉल",
        domainChip = "HAZARD DOMAIN: FIRE & EXPLOSION",
        riskBadge = "CRITICAL RISK • LEVEL 4",
        accentColor = Color(0xFFEF4444),
        hazardOverview = "Combustible coal dust clouds, spontaneous heating in coal seams, and electrical short-circuits can trigger rapid underground gallery fires and secondary methane-dust explosions.",
        ppeChecklist = listOf(
            "Self-Contained Self-Rescuer (SCSR)",
            "Flame-Retardant Coveralls",
            "Intrinsically Safe Cap-Lamp",
            "Thermal Safety Gloves",
            "Steel-Toe Safety Boots"
        ),
        sopProtocols = listOf(
            "Sound the nearest mine fire alarm immediately and alert the surface control room.",
            "Isolate electrical power supplies to the affected gallery section before suppression.",
            "Evacuate upwind against fresh intake airflow using marked emergency refuge routes.",
            "Never use lifts/cages during an active shaft fire unless directed by rescue marshals."
        )
    ),
    "GAS" to ModuleBriefingData(
        id = "GAS",
        title = "Methane Gas Detection & Venting",
        hindiTitle = "मीथेन गैस रिसाव और वेंटिलेशन",
        domainChip = "HAZARD DOMAIN: GAS LEAK & CONFINED SPACE",
        riskBadge = "CRITICAL RISK • LEVEL 4",
        accentColor = Color(0xFFF97316),
        hazardOverview = "Firedamp (methane) and carbon monoxide accumulate in unventilated pockets and roof cavities. Concentrations between 5% and 15% methane form an explosive atmosphere.",
        ppeChecklist = listOf(
            "Calibrated Multi-Gas Detector",
            "Self-Contained Self-Rescuer (SCSR)",
            "Anti-Static Safety Footwear",
            "Intrinsically Safe Cap-Lamp",
            "Confined Space Rescue Harness"
        ),
        sopProtocols = listOf(
            "Check methane and CO readings continuously before entering any heading or confined space.",
            "Evacuate immediately toward fresh intake air if methane exceeds 1.25% threshold.",
            "De-energize all non-intrinsically safe equipment and halt cutting machinery immediately.",
            "Verify auxiliary ventilation ducting and brattice cloth integrity before resuming work."
        )
    ),
    "COLLAPSE" to ModuleBriefingData(
        id = "COLLAPSE",
        title = "Roof Strata & Mine Collapse Control",
        hindiTitle = "खदान छत समर्थन और धंसाव नियंत्रण",
        domainChip = "HAZARD DOMAIN: STRATA & ROOF COLLAPSE",
        riskBadge = "HIGH RISK • LEVEL 4",
        accentColor = Color(0xFFF59E0B),
        hazardOverview = "Unstable geological faults, blasting vibrations, and weathered shale roofs in underground coal and mica mines can lead to sudden roof falls and sidewall rib spalling.",
        ppeChecklist = listOf(
            "DGMS-Approved Hard Hat",
            "High-Visibility Reflective Vest",
            "Steel-Toe & Metatarsal Boots",
            "Sounding Rod / Scaling Bar",
            "Cap-Lamp with Battery Pack"
        ),
        sopProtocols = listOf(
            "Never enter or work beneath an unsupported roof area under any circumstances.",
            "Watch and listen for strata warning signs: cracking timber/bolts and flaking rock (spalling).",
            "Perform systematic roof dressing and verify roof-bolt torque before shift operations.",
            "Withdraw workers to supported junctions and barricade any loose roof zone immediately."
        )
    ),
    "FLOOD" to ModuleBriefingData(
        id = "FLOOD",
        title = "Underground Inundation & Dewatering",
        hindiTitle = "जल भराव और आपातकालीन निकासी",
        domainChip = "HAZARD DOMAIN: MINE INUNDATION & FLOODING",
        riskBadge = "CRITICAL RISK • LEVEL 4",
        accentColor = Color(0xFF38BDF8),
        hazardOverview = "Accidental breakthrough into waterlogged old workings or heavy monsoon surface inflow can cause rapid inundation of lower mine seams and sump levels.",
        ppeChecklist = listOf(
            "Waterproof High-Grip Safety Boots",
            "Intrinsically Safe Cap-Lamp",
            "Self-Contained Self-Rescuer (SCSR)",
            "Emergency Whistle & Signal Lamp",
            "High-Visibility Safety Vest"
        ),
        sopProtocols = listOf(
            "Report wet coal seams, foul-smelling water seepage, or abnormal wall sweating immediately.",
            "Evacuate immediately to higher levels via designated incline escape roadways.",
            "Maintain advance boreholes when approaching within 60 meters of known waterlogged workings.",
            "Keep dewatering sump pumps and emergency watertight doors clear of obstructions."
        )
    ),
    "OXYGEN" to ModuleBriefingData(
        id = "OXYGEN",
        title = "Oxygen Depletion & SCSR Protocol",
        hindiTitle = "ऑक्सीजन की कमी और स्व-बचाव उपकरण",
        domainTitleFallback = "ATMOS: OXY-05"
    ).let {
        ModuleBriefingData(
            id = "OXYGEN",
            title = "Oxygen Depletion & SCSR Protocol",
            hindiTitle = "ऑक्सीजन की कमी और स्व-बचाव उपकरण",
            domainChip = "HAZARD DOMAIN: OXYGEN DEPLETION (BLACKDAMP)",
            riskBadge = "CRITICAL RISK • LEVEL 4",
            accentColor = Color(0xFF06B6D4),
            hazardOverview = "Blackdamp (CO₂ and nitrogen enrichment) displaces breathable oxygen in sealed goaf areas and poorly ventilated shafts, causing rapid hypoxia below 19.5% O₂.",
            ppeChecklist = listOf(
                "Self-Contained Self-Rescuer (SCSR)",
                "Personal Oxygen Monitor (O₂ Sensor)",
                "DGMS Safety Helmet & Cap-Lamp",
                "Rescue Lifeline & Belt Clip",
                "Steel-Toe Safety Boots"
            ),
            sopProtocols = listOf(
                "Verify oxygen concentration is at least 19.5% before entering any gallery or dip working.",
                "Don your Self-Contained Self-Rescuer (SCSR) within 30 seconds at the first sign of dizziness or alarm.",
                "Breathe calmly through the SCSR mouthpiece and nose clip while walking—not running—to fresh air.",
                "Never attempt an unassisted rescue inside an oxygen-deficient zone without breathing apparatus."
            )
        )
    },
    "MACHINERY" to ModuleBriefingData(
        id = "MACHINERY",
        title = "Heavy Machinery & LOTO Compliance",
        hindiTitle = "भारी मशीनरी और लॉकआउट-टैगआउट",
        domainChip = "HAZARD DOMAIN: HEAVY MACHINERY & CONVEYORS",
        riskBadge = "HIGH RISK • LEVEL 3",
        accentColor = Color(0xFFEAB308),
        hazardOverview = "Moving belt conveyors, shearers, dumpers, and crushers pose severe entanglement and crush hazards when guards are bypassed or maintenance begins without energy isolation.",
        ppeChecklist = listOf(
            "Personal LOTO Padlock & Tag",
            "Snug-Fit High-Vis Workwear",
            "Cut-Resistant Impact Gloves",
            "Acoustic Ear Defenders",
            "Steel-Toe Safety Boots"
        ),
        sopProtocols = listOf(
            "Apply strict Lockout / Tagout (LOTO) and isolate all electrical/hydraulic sources before maintenance.",
            "Test for zero residual energy (Try-Start check) before placing hands near rollers or gears.",
            "Never remove, bypass, or reach across machine safety guards while equipment is running.",
            "Maintain visual contact and audible horn clearance around heavy haulage dumpers."
        )
    ),
    "PPE" to ModuleBriefingData(
        id = "PPE",
        title = "Mandatory PPE & Cap-Lamp Rig",
        hindiTitle = "व्यक्तिगत सुरक्षा उपकरण अनुपालन",
        domainChip = "HAZARD DOMAIN: PPE COMPLIANCE & RIGGING",
        riskBadge = "MANDATORY STANDARD • LEVEL 3",
        accentColor = Color(0xFF10B981),
        hazardOverview = "Damaged, improperly fitted, or missing Personal Protective Equipment dramatically increases fatality and injury rates across coal mines, steel plants, and mica units.",
        ppeChecklist = listOf(
            "DGMS Helmet with Chin Strap",
            "Cordless Cap-Lamp & Belt Battery",
            "Steel-Toe Puncture-Proof Boots",
            "High-Visibility Reflective Vest",
            "Anti-Dust Respirator & Goggles"
        ),
        sopProtocols = listOf(
            "Inspect helmet shell, suspension harness, and cap-lamp charge before every shift.",
            "Never repair cracked helmets or torn fall-arrest harnesses with tape—replace immediately.",
            "Wear respiratory dust masks in high-silica mica and coal crushing zones.",
            "Keep high-visibility reflective strips clean of coal slurry and grease for underground visibility."
        )
    )
)

// Helper overload constructor placeholder removed—using clean factory below
private fun ModuleBriefingData(
    id: String,
    title: String,
    hindiTitle: String,
    domainTitleFallback: String
): ModuleBriefingData = ModuleBriefingData(
    id = id,
    title = title,
    hindiTitle = hindiTitle,
    domainChip = domainTitleFallback,
    riskBadge = "",
    accentColor = Color.Cyan,
    hazardOverview = "",
    ppeChecklist = emptyList(),
    sopProtocols = emptyList()
)

private val CockpitBg = Color(0xFF121418)
private val SlateCard = Color(0xFF1E222B)
private val SlateCardInner = Color(0xFF161920)
private val SlateBorder = Color(0xFF374151)
private val SafetyAmber = Color(0xFFFFB300)
private val ActionAmber = Color(0xFFD97706)
private val CertifiedGreen = Color(0xFF10B981)
private val CertifiedGreenBg = Color(0xFF064E3B)
private val AmberBadgeBg = Color(0xFF451A03)
private val MutedText = Color(0xFF9CA3AF)

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

    var progress by remember { mutableStateOf<ModuleProgress?>(null) }
    var attempts by remember { mutableStateOf<List<QuizAttempt>>(emptyList()) }
    var arStatusNote by remember { mutableStateOf<String?>(null) }

    val briefing = remember(normalizedId) {
        moduleBriefings[normalizedId] ?: ModuleBriefingData(
            id = normalizedId,
            title = safetyModules.find { it.second.equals(normalizedId, ignoreCase = true) }?.first
                ?: "Safety Module: $normalizedId",
            hindiTitle = "औद्योगिक और खदान सुरक्षा मॉड्यूल",
            domainChip = "HAZARD DOMAIN: $normalizedId",
            riskBadge = "CRITICAL RISK • LEVEL 4",
            accentColor = SafetyAmber,
            hazardOverview = "Follow mandatory DGMS underground and industrial safety protocols to mitigate operational hazards before entering the active work zone.",
            ppeChecklist = listOf(
                "Self-Contained Self-Rescuer (SCSR)",
                "Calibrated Gas Detector",
                "DGMS Safety Helmet & Cap-Lamp",
                "Steel-Toe Boots & High-Vis Vest"
            ),
            sopProtocols = listOf(
                "Evacuate upwind toward fresh intake airflow when alarms sound.",
                "Isolate electrical supplies and apply Lockout/Tagout before intervention.",
                "Verify airflow direction and gas readings before entering confined areas.",
                "Report any strata or equipment anomalies to the shift overman immediately."
            )
        )
    }

    suspend fun loadModuleData() {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            val userId = SessionManager(context).getUserId() ?: 1L
            progress = db.trainingDao().getModuleProgress(userId, normalizedId)
            attempts = db.trainingDao().getAttemptsForModule(userId, normalizedId)
        }
    }

    LaunchedEffect(normalizedId) {
        loadModuleData()
    }

    val arCompleted = progress?.arCompleted == true
    val quizPassed = progress?.quizPassed == true

    Scaffold(
        containerColor = CockpitBg,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SlateCard,
                    titleContentColor = Color.White,
                    navigationIconContentColor = SafetyAmber
                ),
                title = {
                    Column {
                        Text(
                            text = "PRE-BRIEFING & SOP HUB",
                            style = MaterialTheme.typography.labelSmall,
                            color = SafetyAmber,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = briefing.title,
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
                            color = SafetyAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CockpitBg)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Industrial Header & Status Banner Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                border = BorderStroke(1.5.dp, briefing.accentColor.copy(alpha = 0.7f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // Domain Code Chip & Risk Level Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = briefing.accentColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, briefing.accentColor.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = briefing.domainChip,
                                color = briefing.accentColor,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = Color(0xFF7F1D1D).copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.7f))
                    ) {
                        Text(
                            text = "⚠ ${briefing.riskBadge}",
                            color = Color(0xFFFCA5A5),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.6.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = briefing.title,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = briefing.hindiTitle,
                        color = MutedText,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Module Progress Overview Sub-Card with 2 Distinct Badges
                    Surface(
                        color = SlateCardInner,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SlateBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "OPERATOR MODULE TELEMETRY",
                                color = MutedText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Badge 1: AR Drill
                                StatusTelemetryBadge(
                                    label = "AR Drill",
                                    statusText = if (arCompleted) "Completed ✓" else "Pending Simulation",
                                    isComplete = arCompleted,
                                    modifier = Modifier.weight(1f)
                                )

                                // Badge 2: Knowledge Exam
                                StatusTelemetryBadge(
                                    label = "Knowledge Exam",
                                    statusText = if (quizPassed) "Passed ✓" else "Assessment Required",
                                    isComplete = quizPassed,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (attempts.isNotEmpty()) {
                                val latest = attempts.first()
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Last Exam Score: ${latest.score}/${latest.total} (${if (latest.passed) "PASSED" else "FAILED"}) • Total Attempts: ${attempts.size}",
                                    color = MutedText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // 2A. Hazard Overview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(briefing.accentColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "UNDERGROUND HAZARD OVERVIEW",
                            color = SafetyAmber,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = briefing.hazardOverview,
                        color = Color.White.copy(alpha = 0.92f),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 21.sp
                    )
                }
            }

            // 2B. Mandatory PPE Checklist Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🛡 MANDATORY PPE CHECKLIST",
                        color = SafetyAmber,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Verify all protective gear before entering the simulation or active shaft:",
                        color = MutedText,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        briefing.ppeChecklist.forEach { item ->
                            Surface(
                                color = SlateCardInner,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, SlateBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = CertifiedGreenBg,
                                        shape = RoundedCornerShape(4.dp),
                                        border = BorderStroke(1.dp, CertifiedGreen)
                                    ) {
                                        Text(
                                            text = "REQ",
                                            color = Color(0xFF34D399),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = item,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2C. Core Safety Protocol (SOP) Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚡ CORE SAFETY PROTOCOL (SOP)",
                        color = SafetyAmber,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        briefing.sopProtocols.forEachIndexed { index, step ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(SafetyAmber.copy(alpha = 0.18f))
                                        .border(1.dp, SafetyAmber, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        color = SafetyAmber,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = step,
                                    color = Color.White.copy(alpha = 0.92f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Action Section Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                border = BorderStroke(1.dp, SafetyAmber.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "FIELD DRILL & CERTIFICATION ACTIONS",
                        color = SafetyAmber,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Primary Button: Launch 3D / AR Simulation
                    Button(
                        onClick = {
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    val db = AppDatabase.getInstance(context)
                                    val userId = SessionManager(context).getUserId() ?: 1L
                                    db.trainingDao().markArCompleted(userId, normalizedId)
                                }
                                loadModuleData()
                                arStatusNote = "✓ 3D / AR Simulation drill recorded as Completed in Room."
                            }
                            onLaunchAr(normalizedId)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ActionAmber,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "3D / AR",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Launch 3D / AR Simulation",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Step into interactive 3D inspection before the written exam.",
                        color = MutedText,
                        style = MaterialTheme.typography.bodySmall
                    )

                    arStatusNote?.let { note ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = note,
                            color = Color(0xFF34D399),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 14.dp),
                        color = SlateBorder
                    )

                    // Secondary Prominent Button: Take Module Quiz / Safety Assessment
                    OutlinedButton(
                        onClick = { onTakeQuiz(normalizedId) },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, if (quizPassed) CertifiedGreen else SafetyAmber),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = SlateCardInner,
                            contentColor = if (quizPassed) Color(0xFF34D399) else SafetyAmber
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = if (quizPassed) {
                                "✓ Retake Module Quiz / Safety Assessment"
                            } else {
                                "Take Module Quiz / Safety Assessment →"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Requires 80% score to certify.",
                        color = MutedText,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusTelemetryBadge(
    label: String,
    statusText: String,
    isComplete: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isComplete) CertifiedGreenBg.copy(alpha = 0.6f) else AmberBadgeBg.copy(alpha = 0.6f)
    val borderColor = if (isComplete) CertifiedGreen else SafetyAmber.copy(alpha = 0.7f)
    val valueColor = if (isComplete) Color(0xFF34D399) else SafetyAmber

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = label.uppercase(),
                color = MutedText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = statusText,
                color = valueColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
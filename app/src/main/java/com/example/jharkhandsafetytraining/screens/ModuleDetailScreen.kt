package com.example.jharkhandsafetytraining.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jharkhandsafetytraining.common.SessionManager
import com.example.jharkhandsafetytraining.data.AppDatabase
import com.example.jharkhandsafetytraining.data.ModuleProgress
import com.example.jharkhandsafetytraining.data.QuizAttempt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleDetailScreen(
    moduleId: String,
    onStartAR: () -> Unit,
    onStartQuiz: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var progress by remember { mutableStateOf<ModuleProgress?>(null) }
    var attempts by remember { mutableStateOf<List<QuizAttempt>>(emptyList()) }
    var arStatusNote by remember { mutableStateOf<String?>(null) }

    val moduleTitle = remember(moduleId) {
        safetyModules.find { it.second.equals(moduleId, ignoreCase = true) }?.first ?: moduleId
    }

    suspend fun loadModuleData() {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            val userId = SessionManager(context).getUserId() ?: 1L
            progress = db.trainingDao().getModuleProgress(userId, moduleId.uppercase())
            attempts = db.trainingDao().getAttemptsForModule(userId, moduleId.uppercase())
        }
    }

    LaunchedEffect(moduleId) {
        loadModuleData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Module: $moduleTitle") },
                navigationIcon = {
                    if (onBack != null) {
                        TextButton(onClick = onBack) {
                            Text("← Back")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = moduleTitle,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Module Code: ${moduleId.uppercase()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Module Progress Status",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• AR Simulation: ${if (progress?.arCompleted == true) "✓ Completed" else "Pending"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Safety Assessment Quiz: ${if (progress?.quizPassed == true) "✓ Passed" else "Not Passed Yet"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (attempts.isNotEmpty()) {
                        val latest = attempts.first()
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Last Quiz Attempt: ${latest.score}/${latest.total} (${if (latest.passed) "PASSED" else "FAILED"}) • Total Attempts: ${attempts.size}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            arStatusNote?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            val db = AppDatabase.getInstance(context)
                            val userId = SessionManager(context).getUserId() ?: 1L
                            db.trainingDao().markArCompleted(userId, moduleId.uppercase())
                        }
                        loadModuleData()
                        arStatusNote = "AR module handshake triggered & AR completion recorded in Room."
                    }
                    onStartAR()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Launch AR Simulation")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onStartQuiz,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (progress?.quizPassed == true) "Retake Module Quiz" else "Take Module Quiz")
            }
        }
    }
}
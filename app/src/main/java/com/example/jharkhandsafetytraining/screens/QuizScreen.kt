package com.example.jharkhandsafetytraining.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.jharkhandsafetytraining.quiz.QuizBank
import com.example.jharkhandsafetytraining.quiz.QuizEngine
import com.example.jharkhandsafetytraining.quiz.QuizRepository
import com.example.jharkhandsafetytraining.quiz.QuizResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    moduleId: String,
    userLanguage: String = "hi", // "en", "hi", or "sat"
    onQuizPassed: (score: Int, total: Int) -> Unit,
    onBackToModule: () -> Unit
) {
    val context = LocalContext.current
    var quizBank by remember { mutableStateOf<QuizBank?>(null) }
    var userAnswers by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var quizResult by remember { mutableStateOf<QuizResult?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    // Load Quiz Bank from assets
    LaunchedEffect(moduleId) {
        try {
            val repo = QuizRepository(context)
            quizBank = repo.load(moduleId)
        } catch (e: Exception) {
            loadError = "Quiz for $moduleId is not yet loaded in assets: ${e.localizedMessage}"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Safety Assessment: $moduleId") },
                navigationIcon = {
                    IconButton(onClick = onBackToModule) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                loadError != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = loadError ?: "Unknown error", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBackToModule) { Text("Back to Module") }
                    }
                }

                quizBank == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                quizResult == null -> {
                    val bank = quizBank!!
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(bank.questions) { index, question ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    val questionText = question.text[userLanguage] ?: question.text["en"] ?: ""
                                    Text(
                                        text = "Q${index + 1}. $questionText",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    question.options.forEachIndexed { optIndex, optMap ->
                                        val optionText = optMap[userLanguage] ?: optMap["en"] ?: ""
                                        val isSelected = userAnswers[question.id] == optIndex

                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clickable {
                                                    userAnswers = userAnswers + (question.id to optIndex)
                                                },
                                            shape = MaterialTheme.shapes.small,
                                            border = BorderStroke(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                                            ),
                                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = isSelected,
                                                    onClick = {
                                                        userAnswers = userAnswers + (question.id to optIndex)
                                                    }
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = optionText, style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    quizResult = QuizEngine.score(bank, userAnswers)
                                },
                                enabled = userAnswers.size == bank.questions.size,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Submit Assessment")
                            }
                        }
                    }
                }

                else -> {
                    // Result & Retry/Review Weak Areas Loop
                    val res = quizResult!!
                    val bank = quizBank!!

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (res.passed) "PASSED!" else "FAILED - COMPREHENSION CHECK",
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (res.passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Score: ${res.score} / ${res.total} (Passing: ${bank.passPercent}%)",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (res.passed) {
                            Text(
                                "Great work! Safety protocols verified.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { onQuizPassed(res.score, res.total) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Proceed to Certificate & Next Step")
                            }
                        } else {
                            // Weak Areas Summary
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Areas Needing Review:",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    res.wrongQuestionIds.forEach { wrongId ->
                                        val q = bank.questions.find { it.id == wrongId }
                                        val qText = q?.text?.get(userLanguage) ?: q?.text?.get("en") ?: ""
                                        Text(
                                            text = "• $qText",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    // Reset answers to allow retake
                                    userAnswers = emptyMap()
                                    quizResult = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Retake Quiz")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = onBackToModule,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Review AR Simulation Again")
                            }
                        }
                    }
                }
            }
        }
    }
}
package com.example.jharkhandsafetytraining.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jharkhandsafetytraining.quiz.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    moduleId: String,
    userLanguage: String = "hi", // "en", "hi", or "sat"
    quizViewModel: QuizViewModel = viewModel(),
    onQuizPassed: (score: Int, total: Int) -> Unit,
    onBackToModule: () -> Unit,
    onOpenCertificate: (() -> Unit)? = null
) {
    var activeLanguage by remember(userLanguage) { mutableStateOf(userLanguage) }
    var userAnswers by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    // Load Quiz Bank and User Progress from Room & Assets via QuizViewModel
    LaunchedEffect(moduleId) {
        userAnswers = emptyMap()
        quizViewModel.loadQuiz(moduleId)
    }

    // Sync language from logged-in user if available
    LaunchedEffect(quizViewModel.currentUser) {
        quizViewModel.currentUser?.language?.takeIf { it.isNotBlank() }?.let {
            activeLanguage = it
        }
    }

    val quizBank = quizViewModel.quizBank
    val quizResult = quizViewModel.quizResult
    val loadError = quizViewModel.loadError

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Safety Assessment: $moduleId") },
                navigationIcon = {
                    TextButton(onClick = onBackToModule) {
                        Text("← Back")
                    }
                },
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        listOf("en" to "EN", "hi" to "हिं", "sat" to "SAT").forEach { (code, label) ->
                            FilterChip(
                                selected = activeLanguage == code,
                                onClick = { activeLanguage = code },
                                label = { Text(label) }
                            )
                        }
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
                        Text(
                            text = loadError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBackToModule) {
                            Text("Back to Module")
                        }
                    }
                }

                quizBank == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                quizResult == null -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "Passing Requirement: ${quizBank.passPercent}% (${userAnswers.size}/${quizBank.questions.size} answered)",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        itemsIndexed(quizBank.questions) { index, question ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    val questionText = question.text[activeLanguage]
                                        ?: question.text["en"]
                                        ?: ""
                                    Text(
                                        text = "Q${index + 1}. $questionText",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    question.options.forEachIndexed { optIndex, optMap ->
                                        val optionText = optMap[activeLanguage]
                                            ?: optMap["en"]
                                            ?: ""
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
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            }
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
                                                Text(
                                                    text = optionText,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
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
                                    quizViewModel.submitQuiz(
                                        moduleId = moduleId,
                                        answers = userAnswers
                                    )
                                },
                                enabled = userAnswers.size == quizBank.questions.size && !quizViewModel.isSaving,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (quizViewModel.isSaving) "Saving Attempt..." else "Submit Assessment")
                            }
                        }
                    }
                }

                else -> {
                    // Result & Retry / Review Weak Areas Screen
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (quizResult.passed) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.errorContainer
                                    }
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (quizResult.passed) {
                                            "PASSED! SAFETY MODULE COMPLETE"
                                        } else {
                                            "FAILED - REVIEW WEAK AREAS"
                                        },
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (quizResult.passed) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onErrorContainer
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val percent = if (quizResult.total == 0) 0 else (quizResult.score * 100 / quizResult.total)
                                    Text(
                                        text = "Score: ${quizResult.score} / ${quizResult.total} ($percent%) • Required: ${quizBank.passPercent}%",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (quizResult.passed) {
                                            "Attempt recorded & ModuleProgress(quizPassed = true) saved offline in Room."
                                        } else {
                                            "Failed attempt recorded in Room. Please review the missed safety questions below before retrying."
                                        },
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        if (quizResult.passed) {
                            item {
                                Text(
                                    text = "Great work! Your safety protocol mastery for $moduleId has been verified.",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                if (onOpenCertificate != null) {
                                    Button(
                                        onClick = onOpenCertificate,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            if (quizViewModel.allModulesCompleted) {
                                                "View Official HMAC-Signed Certificate"
                                            } else {
                                                "View Module Certificate & Progress"
                                            }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                OutlinedButton(
                                    onClick = { onQuizPassed(quizResult.score, quizResult.total) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Return to Safety Hub")
                                }
                            }
                        } else {
                            // Review Weak Areas Screen listing missed safety questions
                            item {
                                Text(
                                    text = "Review Weak Areas (${quizResult.wrongQuestionIds.size} Missed Question${if (quizResult.wrongQuestionIds.size == 1) "" else "s"})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            itemsIndexed(quizResult.wrongQuestionIds) { idx, wrongId ->
                                val q = quizBank.questions.find { it.id == wrongId }
                                if (q != null) {
                                    val qText = q.text[activeLanguage] ?: q.text["en"] ?: ""
                                    val pickedIdx = userAnswers[wrongId]
                                    val pickedText = pickedIdx?.let {
                                        q.options.getOrNull(it)?.let { opt -> opt[activeLanguage] ?: opt["en"] }
                                    } ?: "No answer"
                                    val correctText = q.options.getOrNull(q.correctIndex)?.let { opt ->
                                        opt[activeLanguage] ?: opt["en"]
                                    } ?: ""

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = "Missed #${idx + 1}: $qText",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "✗ Your Answer: $pickedText",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "✓ Correct Safety Protocol: $correctText",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        userAnswers = emptyMap()
                                        quizViewModel.resetForRetry()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Retry Quiz")
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = onBackToModule,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Back to Module / Review AR Simulation")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
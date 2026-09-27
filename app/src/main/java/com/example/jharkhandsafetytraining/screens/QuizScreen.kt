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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jharkhandsafetytraining.quiz.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    moduleId: String,
    userLanguage: String = "hi",
    quizViewModel: QuizViewModel = viewModel(),
    onQuizPassed: (score: Int, total: Int) -> Unit,
    onBackToModule: () -> Unit,
    onOpenCertificate: (() -> Unit)? = null
) {
    var userAnswers by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    LaunchedEffect(moduleId) {
        quizViewModel.loadQuiz(moduleId)
    }

    val bank = quizViewModel.quizBank
    val quizResult = quizViewModel.quizResult
    val loadError = quizViewModel.loadError
    val isSaving = quizViewModel.isSaving

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Safety Assessment: $moduleId") },
                navigationIcon = {
                    IconButton(onClick = onBackToModule) {
                        Text(
                            text = "←",
                            style = MaterialTheme.typography.titleLarge
                        )
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
                        Text(text = loadError, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBackToModule) { Text("Back to Module") }
                    }
                }

                bank == null || isSaving -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                quizResult == null -> {
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
                                    val qText = question.text[userLanguage]?.takeIf { it.isNotBlank() }
                                        ?: question.text["en"] ?: ""
                                    Text(
                                        text = "Q${index + 1}. $qText",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    question.options.forEachIndexed { optIndex, optMap ->
                                        val optText = optMap[userLanguage]?.takeIf { it.isNotBlank() }
                                            ?: optMap["en"] ?: ""
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
                                                Text(text = optText, style = MaterialTheme.typography.bodyMedium)
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
                                    quizViewModel.submitQuiz(moduleId, userAnswers)
                                },
                                enabled = userAnswers.size == bank.questions.size && !isSaving,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Submit Assessment")
                            }
                        }
                    }
                }

                else -> {
                    val res = quizResult

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
                                "Safety protocols verified successfully.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { onQuizPassed(res.score, res.total) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Continue")
                            }

                            if (onOpenCertificate != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = onOpenCertificate,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("View Certificates")
                                }
                            }
                        } else {
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
                                        val qText = q?.text?.get(userLanguage)?.takeIf { it.isNotBlank() }
                                            ?: q?.text?.get("en") ?: ""
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
                                    userAnswers = emptyMap()
                                    quizViewModel.resetForRetry()
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
                                Text("Review Safety Material")
                            }
                        }
                    }
                }
            }
        }
    }
}
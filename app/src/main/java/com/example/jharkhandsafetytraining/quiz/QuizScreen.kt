package com.example.jharkhandsafetytraining.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.jharkhandsafetytraining.data.ModuleProgress
import com.example.jharkhandsafetytraining.data.QuizAttempt
import com.example.jharkhandsafetytraining.data.TrainingDao
import kotlinx.coroutines.launch

@Composable
fun QuizScreen(
    moduleId: String,
    userId: Long,
    dao: TrainingDao,
    lang: String = "en",
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val bank = remember(moduleId) { QuizRepository(context).load(moduleId) }

    var current by remember { mutableStateOf(0) }
    var answers by remember { mutableStateOf(mapOf<String, Int>()) }
    var result by remember { mutableStateOf<QuizResult?>(null) }

    // Use the chosen language, fall back to English if the translation is empty
    fun pick(m: Map<String, String>) = m[lang]?.takeIf { it.isNotBlank() } ?: m["en"] ?: ""

    val res = result
    Column(
        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        if (res == null) {
            val q = bank.questions[current]
            Text(
                "Question ${current + 1} of ${bank.questions.size}",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.height(12.dp))
            Text(pick(q.text), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            q.options.forEachIndexed { index, option ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = answers[q.id] == index,
                            onClick = { answers = answers + (q.id to index) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = answers[q.id] == index, onClick = null)
                    Spacer(Modifier.width(12.dp))
                    Text(pick(option))
                }
            }

            Spacer(Modifier.height(24.dp))
            val isLast = current == bank.questions.size - 1
            Button(
                enabled = answers.containsKey(q.id),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (!isLast) {
                        current++
                    } else {
                        val r = QuizEngine.score(bank, answers)
                        result = r
                        scope.launch {
                            dao.insertAttempt(
                                QuizAttempt(
                                    userId = userId, moduleId = moduleId,
                                    score = r.score, total = r.total, passed = r.passed
                                )
                            )
                            if (r.passed) {
                                dao.upsertProgress(
                                    ModuleProgress(userId = userId, moduleId = moduleId, quizPassed = true)
                                )
                            }
                        }
                    }
                }
            ) { Text(if (isLast) "Submit" else "Next") }

        } else {
            Text(
                if (res.passed) "Passed" else "Not passed yet",
                style = MaterialTheme.typography.headlineMedium
            )
            Text("Score: ${res.score} / ${res.total}", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))

            if (res.passed) {
                Button(onClick = onFinished, modifier = Modifier.fillMaxWidth()) { Text("Continue") }
            } else {
                Text("Review these questions:", style = MaterialTheme.typography.titleMedium)
                bank.questions.filter { it.id in res.wrongQuestionIds }.forEach { q ->
                    Spacer(Modifier.height(12.dp))
                    Text(pick(q.text), style = MaterialTheme.typography.bodyLarge)
                    Text("Correct answer: ${pick(q.options[q.correctIndex])}")
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { current = 0; answers = emptyMap(); result = null }
                ) { Text("Retry quiz") }
            }
        }
    }
}
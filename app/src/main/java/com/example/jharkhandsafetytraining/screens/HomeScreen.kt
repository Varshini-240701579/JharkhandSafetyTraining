package com.example.jharkhandsafetytraining.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            val userId = SessionManager(context).getUserId() ?: 1L
            currentUser = db.userDao().findById(userId)
            val list = db.trainingDao().getProgress(userId)
            progressMap = list.associateBy { it.moduleId.uppercase() }
        }
    }

    val passedCount = safetyModules.count { (_, id) -> progressMap[id]?.quizPassed == true }
    val totalCount = safetyModules.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Jharkhand Mining Safety Hub")
                        currentUser?.let {
                            Text(
                                text = "Worker: ${it.name} (${it.phone})",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                },
                actions = {
                    if (onLogout != null) {
                        TextButton(onClick = onLogout) {
                            Text("Logout")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Certification Progress: $passedCount / $totalCount Modules Passed",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { passedCount.toFloat() / totalCount.toFloat() },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onOpenCertificate,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Certificate & QR")
                            }
                            OutlinedButton(
                                onClick = onOpenVerifier,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("QR Verifier")
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Core Vocational Safety Modules",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            items(safetyModules) { (name, id) ->
                val moduleProg = progressMap[id]
                val isPassed = moduleProg?.quizPassed == true

                Card(
                    onClick = { onModuleClick(id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Domain ID: $id",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = if (isPassed) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = if (isPassed) "✓ PASSED" else "PENDING",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
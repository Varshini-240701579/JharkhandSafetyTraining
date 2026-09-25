package com.example.jharkhandsafetytraining.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ModuleDetailScreen(
    moduleId: String,
    onStartAR: () -> Unit,
    onStartQuiz: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Module: $moduleId", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onStartAR, modifier = Modifier.fillMaxWidth()) {
            Text("Launch AR Simulation")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onStartQuiz, modifier = Modifier.fillMaxWidth()) {
            Text("Take Module Quiz")
        }
    }
}
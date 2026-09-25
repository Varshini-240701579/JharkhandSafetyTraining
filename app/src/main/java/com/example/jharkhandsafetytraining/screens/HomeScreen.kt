package com.example.jharkhandsafetytraining.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
fun HomeScreen(onModuleClick: (String) -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Jharkhand Mining Safety Hub") }) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            items(safetyModules) { (name, id) ->
                Card(
                    onClick = { onModuleClick(id) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                ) {
                    Text(text = name, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
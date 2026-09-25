package com.example.jharkhandsafetytraining.certificate

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.jharkhandsafetytraining.data.Certificate
import com.example.jharkhandsafetytraining.data.TrainingDao
import kotlinx.coroutines.launch

@Composable
fun CertificateScreen(
    moduleId: String,
    userId: Long,
    dao: TrainingDao,
    onDone: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var certificate by remember { mutableStateOf<Certificate?>(null) }

    LaunchedEffect(moduleId, userId) {
        val existing = dao.getCertificates(userId).find { it.moduleId == moduleId }
        if (existing != null) {
            certificate = existing
        } else {
            val newCert = Certificate(userId = userId, moduleId = moduleId)
            dao.insertCertificate(newCert)
            certificate = dao.getCertificates(userId).find { it.moduleId == moduleId }
        }
    }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val cert = certificate
        if (cert == null) {
            CircularProgressIndicator()
        } else {
            Text("Certificate Issued", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("Module: $moduleId", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(24.dp))
            Text("QR code and signed verification coming soon.")
            Spacer(Modifier.height(24.dp))
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                Text("Done")
            }
        }
    }
}
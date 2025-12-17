package com.filamentmate.app.ui.screens.calibration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrationHubScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWizard: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kalibrierung") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Kalibrier-Assistent",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Text(
                text = "Wähle eine Kalibrierung:",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = { onNavigateToWizard("TEMP") }) {
                Text("🌡️ Temperatur-Turm")
            }
            
            Button(onClick = { onNavigateToWizard("MAX_FLOW") }) {
                Text("💨 Max. Flow Test")
            }
            
            Button(onClick = { onNavigateToWizard("PRESSURE_ADVANCE") }) {
                Text("📐 Pressure Advance")
            }
            
            Button(onClick = { onNavigateToWizard("FLOW") }) {
                Text("💧 Flow-Kalibrierung")
            }
        }
    }
}

package com.filamentmate.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen(
    onNavigateToCalibration: () -> Unit,
    onNavigateToSpoolDetail: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "FilamentMate",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Willkommen bei FilamentMate!",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Text(
            text = "Deine Filament- und Druckerverwaltung.",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(onClick = onNavigateToCalibration) {
            Text("Kalibrierung starten")
        }
    }
}

package com.filamentmate.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SpoolsScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAdd: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Spulen Übersicht",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        )
        
        Text(
            text = "Hier werden deine Spulen angezeigt.",
            modifier = Modifier.align(Alignment.Center)
        )
        
        FloatingActionButton(
            onClick = onNavigateToAdd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Spule hinzufügen")
        }
    }
}

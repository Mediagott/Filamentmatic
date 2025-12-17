package com.filamentmate.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.filamentmate.app.data.database.entity.SpoolEntity
import com.filamentmate.app.ui.viewmodel.SpoolsViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpoolsScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAdd: () -> Unit,
    viewModel: SpoolsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Text(
                text = "Meine Spulen",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
            
            // Filter Chips
            if (uiState.availableMaterials.isNotEmpty() || 
                uiState.availableBrands.isNotEmpty() ||
                uiState.availableColors.isNotEmpty()) {
                
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    // Material Filter
                    if (uiState.availableMaterials.isNotEmpty()) {
                        Text(
                            text = "Material",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            uiState.availableMaterials.forEach { material ->
                                FilterChip(
                                    selected = uiState.selectedMaterial == material,
                                    onClick = {
                                        if (uiState.selectedMaterial == material) {
                                            viewModel.setMaterialFilter(null)
                                        } else {
                                            viewModel.setMaterialFilter(material)
                                        }
                                    },
                                    label = { Text(material) }
                                )
                            }
                        }
                    }
                    
                    // Brand Filter
                    if (uiState.availableBrands.isNotEmpty()) {
                        Text(
                            text = "Marke",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            uiState.availableBrands.forEach { brand ->
                                FilterChip(
                                    selected = uiState.selectedBrand == brand,
                                    onClick = {
                                        if (uiState.selectedBrand == brand) {
                                            viewModel.setBrandFilter(null)
                                        } else {
                                            viewModel.setBrandFilter(brand)
                                        }
                                    },
                                    label = { Text(brand) }
                                )
                            }
                        }
                    }
                    
                    // Clear Filters Button
                    if (uiState.selectedMaterial != null || 
                        uiState.selectedBrand != null || 
                        uiState.selectedColor != null) {
                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.clearFilters() }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Filter löschen",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = "Filter zurücksetzen",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Spools List
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.spools.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Keine Spulen gefunden",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Tippe auf + um eine neue Spule hinzuzufügen",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.spools, key = { it.id }) { spool ->
                        SpoolCard(
                            spool = spool,
                            onClick = { onNavigateToDetail(spool.id) }
                        )
                    }
                    // Bottom padding for FAB
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
        
        // FAB
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

@Composable
fun SpoolCard(
    spool: SpoolEntity,
    onClick: () -> Unit
) {
    val lowWeight = spool.remainingWeightG < 100f
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color Circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        try {
                            Color(android.graphics.Color.parseColor(spool.colorHex ?: "#808080"))
                        } catch (e: Exception) {
                            Color.Gray
                        }
                    )
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Spool Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = spool.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${spool.material} • ${spool.brand}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = spool.color,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Weight
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (lowWeight) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Niedriges Gewicht",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = "${spool.remainingWeightG.toInt()}g",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (lowWeight) MaterialTheme.colorScheme.error 
                               else MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "übrig",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

package com.filamentmate.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.filamentmate.app.ui.viewmodel.FilamentProfileDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilamentProfileDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: FilamentProfileDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val editableProfile by viewModel.editableProfile.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Profil gespeichert")
            viewModel.resetSaveSuccess()
            onNavigateBack()
        }
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (uiState.isNewProfile) "Neues Profil" else "Profil bearbeiten")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.saveProfile() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = "Speichern")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Basic Info Section
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Grunddaten",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Material Dropdown
                        var materialExpanded by remember { mutableStateOf(false) }
                        val materials = listOf("PLA", "PETG", "ABS", "ASA", "TPU", "HIPS", "PVA", "Nylon", "PC", "Andere")
                        
                        ExposedDropdownMenuBox(
                            expanded = materialExpanded,
                            onExpandedChange = { materialExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = editableProfile.material,
                                onValueChange = {},
                                label = { Text("Material") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = materialExpanded) }
                            )
                            ExposedDropdownMenu(
                                expanded = materialExpanded,
                                onDismissRequest = { materialExpanded = false }
                            ) {
                                materials.forEach { material ->
                                    DropdownMenuItem(
                                        text = { Text(material) },
                                        onClick = {
                                            viewModel.updateProfile(editableProfile.copy(material = material))
                                            materialExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = editableProfile.brand,
                            onValueChange = { viewModel.updateProfile(editableProfile.copy(brand = it)) },
                            label = { Text("Marke") },
                            placeholder = { Text("z.B. Bambu Lab") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = editableProfile.color ?: "",
                            onValueChange = { viewModel.updateProfile(editableProfile.copy(color = it.ifEmpty { null })) },
                            label = { Text("Farbe (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
            
            // Temperature Section
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Temperatur",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = editableProfile.recommendedTempC?.toString() ?: "",
                                onValueChange = { 
                                    viewModel.updateProfile(editableProfile.copy(
                                        recommendedTempC = it.toIntOrNull()
                                    ))
                                },
                                label = { Text("Empfohlen (°C)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = editableProfile.tempMinC?.toString() ?: "",
                                onValueChange = { 
                                    viewModel.updateProfile(editableProfile.copy(
                                        tempMinC = it.toIntOrNull()
                                    ))
                                },
                                label = { Text("Min (°C)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = editableProfile.tempMaxC?.toString() ?: "",
                                onValueChange = { 
                                    viewModel.updateProfile(editableProfile.copy(
                                        tempMaxC = it.toIntOrNull()
                                    ))
                                },
                                label = { Text("Max (°C)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                }
            }
            
            // Flow & Pressure Advance Section
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Kalibrierungswerte",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Nutze Bambu Studio für die Kalibrierung",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = editableProfile.maxVolumetricFlowMm3s?.toString() ?: "",
                            onValueChange = { 
                                viewModel.updateProfile(editableProfile.copy(
                                    maxVolumetricFlowMm3s = it.toFloatOrNull()
                                ))
                            },
                            label = { Text("Max. volumetrischer Flow (mm³/s)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = editableProfile.pressureAdvanceValue?.toString() ?: "",
                                onValueChange = { 
                                    viewModel.updateProfile(editableProfile.copy(
                                        pressureAdvanceValue = it.toFloatOrNull()
                                    ))
                                },
                                label = { Text("Pressure Advance") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(2f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = editableProfile.pressureAdvanceLabel ?: "PA",
                                onValueChange = { 
                                    viewModel.updateProfile(editableProfile.copy(
                                        pressureAdvanceLabel = it.ifEmpty { null }
                                    ))
                                },
                                label = { Text("Label") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = editableProfile.flowRatio?.let { "${(it * 100).toInt()}" } ?: "",
                            onValueChange = { 
                                val ratio = it.toFloatOrNull()?.div(100)
                                viewModel.updateProfile(editableProfile.copy(flowRatio = ratio))
                            },
                            label = { Text("Flow Ratio (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
            
            // Notes
            item {
                OutlinedTextField(
                    value = editableProfile.notes ?: "",
                    onValueChange = { viewModel.updateProfile(editableProfile.copy(notes = it.ifEmpty { null })) },
                    label = { Text("Notizen") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

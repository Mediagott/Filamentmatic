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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.filamentmate.app.ui.viewmodel.SpoolDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpoolDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: SpoolDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val editableSpool by viewModel.editableSpool.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showConsumptionDialog by remember { mutableStateOf(false) }
    var showSlotLinkDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Handle save success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Spule gespeichert")
            viewModel.resetSaveSuccess()
            onNavigateBack()
        }
    }
    
    // Handle errors
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
                    Text(if (uiState.isNewSpool) "Neue Spule" else "Spule bearbeiten")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    if (!uiState.isNewSpool) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Löschen")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.saveSpool() },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name
            OutlinedTextField(
                value = editableSpool.name,
                onValueChange = { viewModel.updateSpool(editableSpool.copy(name = it)) },
                label = { Text("Name") },
                placeholder = { Text("z.B. PLA Schwarz") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Material Dropdown
            var materialExpanded by remember { mutableStateOf(false) }
            val materials = listOf("PLA", "PETG", "ABS", "ASA", "TPU", "HIPS", "PVA", "Nylon", "PC", "Andere")
            
            ExposedDropdownMenuBox(
                expanded = materialExpanded,
                onExpandedChange = { materialExpanded = it }
            ) {
                OutlinedTextField(
                    value = editableSpool.material,
                    onValueChange = { viewModel.updateSpool(editableSpool.copy(material = it)) },
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
                                viewModel.updateSpool(editableSpool.copy(material = material))
                                materialExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Brand
            OutlinedTextField(
                value = editableSpool.brand,
                onValueChange = { viewModel.updateSpool(editableSpool.copy(brand = it)) },
                label = { Text("Marke") },
                placeholder = { Text("z.B. Bambu Lab") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Color
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = editableSpool.color,
                    onValueChange = { viewModel.updateSpool(editableSpool.copy(color = it)) },
                    label = { Text("Farbe") },
                    placeholder = { Text("z.B. Schwarz") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = editableSpool.colorHex ?: "",
                    onValueChange = { viewModel.updateSpool(editableSpool.copy(colorHex = it.ifEmpty { null })) },
                    label = { Text("Hex") },
                    placeholder = { Text("#000000") },
                    modifier = Modifier.width(120.dp),
                    singleLine = true
                )
            }
            
            // Weight Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Gewicht",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editableSpool.remainingWeightG.toString(),
                            onValueChange = { 
                                it.toFloatOrNull()?.let { weight ->
                                    viewModel.updateSpool(editableSpool.copy(remainingWeightG = weight))
                                }
                            },
                            label = { Text("Restgewicht (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = editableSpool.startWeightG?.toString() ?: "",
                            onValueChange = { 
                                viewModel.updateSpool(
                                    editableSpool.copy(startWeightG = it.toFloatOrNull())
                                )
                            },
                            label = { Text("Startgewicht (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = editableSpool.emptySpoolWeightG?.toString() ?: "",
                        onValueChange = { 
                            viewModel.updateSpool(
                                editableSpool.copy(emptySpoolWeightG = it.toFloatOrNull())
                            )
                        },
                        label = { Text("Leerspulengewicht / Tara (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
            
            // Spool Link Section (only for existing spools)
            if (!uiState.isNewSpool) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Drucker-Slot",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (uiState.trayLink != null) {
                            Text(
                                text = "Verknüpft mit: ${uiState.trayLink?.slotGroup} Slot ${(uiState.trayLink?.slotIndex ?: 0) + 1}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { viewModel.unlinkFromSlot() }
                            ) {
                                Text("Verknüpfung lösen")
                            }
                        } else {
                            Text(
                                text = "Nicht mit einem Slot verknüpft",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { showSlotLinkDialog = true }
                            ) {
                                Icon(Icons.Default.Link, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mit Slot verknüpfen")
                            }
                        }
                    }
                }
                
                // Consumption Button
                Button(
                    onClick = { showConsumptionDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Verbrauch eintragen")
                }
            }
            
            // Notes
            OutlinedTextField(
                value = editableSpool.note ?: "",
                onValueChange = { viewModel.updateSpool(editableSpool.copy(note = it.ifEmpty { null })) },
                label = { Text("Notizen") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
        }
    }
    
    // Consumption Dialog
    if (showConsumptionDialog) {
        ConsumptionDialog(
            onDismiss = { showConsumptionDialog = false },
            onConfirm = { weight, jobName ->
                viewModel.recordConsumption(weight, jobName)
                showConsumptionDialog = false
            }
        )
    }
    
    // Slot Link Dialog
    if (showSlotLinkDialog) {
        SlotLinkDialog(
            onDismiss = { showSlotLinkDialog = false },
            onConfirm = { slotGroup, slotIndex ->
                viewModel.linkToSlot(slotGroup, slotIndex)
                showSlotLinkDialog = false
            }
        )
    }
    
    // Delete Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Spule löschen?") },
            text = { Text("Möchtest du diese Spule wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Delete handled by parent screen
                        showDeleteDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Löschen", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@Composable
fun ConsumptionDialog(
    onDismiss: () -> Unit,
    onConfirm: (Float, String) -> Unit
) {
    var weight by remember { mutableFloatStateOf(0f) }
    var jobName by remember { mutableStateOf("Manueller Eintrag") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Verbrauch eintragen") },
        text = {
            Column {
                OutlinedTextField(
                    value = if (weight == 0f) "" else weight.toString(),
                    onValueChange = { weight = it.toFloatOrNull() ?: 0f },
                    label = { Text("Verbrauchtes Gewicht (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = jobName,
                    onValueChange = { jobName = it },
                    label = { Text("Druckauftrag (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(weight, jobName) },
                enabled = weight > 0f
            ) {
                Text("Eintragen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

@Composable
fun SlotLinkDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var slotGroup by remember { mutableStateOf("AMS") }
    var slotIndex by remember { mutableIntStateOf(0) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mit Slot verknüpfen") },
        text = {
            Column {
                Text("Slot-Gruppe: $slotGroup")
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 0..3) {
                        Button(
                            onClick = { slotIndex = i },
                            modifier = Modifier.weight(1f),
                            colors = if (slotIndex == i) {
                                androidx.compose.material3.ButtonDefaults.buttonColors()
                            } else {
                                androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
                            }
                        ) {
                            Text("${i + 1}")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(slotGroup, slotIndex) }) {
                Text("Verknüpfen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

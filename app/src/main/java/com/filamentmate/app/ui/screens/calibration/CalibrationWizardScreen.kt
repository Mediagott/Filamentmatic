package com.filamentmate.app.ui.screens.calibration

import android.content.Intent
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.filamentmate.app.data.calibration.CalibrationParams
import com.filamentmate.app.data.database.entity.CalibrationTestType
import com.filamentmate.app.ui.viewmodel.CalibrationStep
import com.filamentmate.app.ui.viewmodel.CalibrationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrationWizardScreen(
    testType: String,
    onNavigateBack: () -> Unit,
    viewModel: CalibrationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    
    val testTitle = when (uiState.testType) {
        CalibrationTestType.TEMP -> "Temperatur-Turm"
        CalibrationTestType.MAX_FLOW -> "Max. Flow Test"
        CalibrationTestType.PRESSURE_ADVANCE -> "Pressure Advance"
        CalibrationTestType.FLOW -> "Flow Ratio"
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Ergebnis gespeichert!")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(testTitle) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.step == CalibrationStep.INTRO) {
                            onNavigateBack()
                        } else {
                            viewModel.previousStep()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Progress indicator
            val progress = when (uiState.step) {
                CalibrationStep.INTRO -> 0f
                CalibrationStep.PARAMETERS -> 0.2f
                CalibrationStep.GENERATING -> 0.4f
                CalibrationStep.PRINTING -> 0.6f
                CalibrationStep.RESULT -> 0.8f
                CalibrationStep.COMPLETE -> 1f
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (uiState.step) {
                    CalibrationStep.INTRO -> IntroStep(uiState.testType, viewModel::nextStep)
                    CalibrationStep.PARAMETERS -> ParametersStep(uiState.params, viewModel)
                    CalibrationStep.GENERATING -> GeneratingStep()
                    CalibrationStep.PRINTING -> PrintingStep(
                        gCode = uiState.generatedGCode,
                        onExport = {
                            viewModel.exportGCode()?.let { intent ->
                                context.startActivity(Intent.createChooser(intent, "GCode exportieren"))
                            }
                        },
                        onNext = viewModel::nextStep
                    )
                    CalibrationStep.RESULT -> ResultStep(
                        testType = uiState.testType,
                        params = uiState.params,
                        resultValue = uiState.resultValue,
                        notes = uiState.notes,
                        onResultChange = viewModel::updateResultValue,
                        onNotesChange = viewModel::updateNotes,
                        onSave = viewModel::nextStep
                    )
                    CalibrationStep.COMPLETE -> CompleteStep(onNavigateBack)
                }
            }
        }
    }
}

@Composable
private fun IntroStep(testType: CalibrationTestType, onNext: () -> Unit) {
    val (title, description) = when (testType) {
        CalibrationTestType.TEMP -> "Temperatur-Kalibrierung" to
            "Dieser Test druckt einen Turm mit verschiedenen Temperaturen. " +
            "Nach dem Druck wählst du die Schicht aus, die am besten aussieht."
        CalibrationTestType.MAX_FLOW -> "Maximaler Flow" to
            "Druckt Linien mit steigendem volumetrischen Flow. " +
            "Finde die höchste Geschwindigkeit, bei der keine Qualitätsverluste auftreten."
        CalibrationTestType.PRESSURE_ADVANCE -> "Pressure Advance" to
            "Druckt Linien mit wechselnder Geschwindigkeit. " +
            "Finde den PA-Wert, bei dem die Übergänge am saubersten sind."
        CalibrationTestType.FLOW -> "Flow Ratio" to
            "Druckt einen Würfel mit genau 2 Perimetern. " +
            "Miss die Wandstärke und berechne das korrekte Flow-Verhältnis."
    }
    
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Starten")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

@Composable
private fun ParametersStep(params: CalibrationParams, viewModel: CalibrationViewModel) {
    Column {
        Text(
            text = "Parameter einstellen",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (params) {
            is CalibrationParams.TempTower -> TempTowerParams(params, viewModel)
            is CalibrationParams.MaxFlow -> MaxFlowParams(params, viewModel)
            is CalibrationParams.PressureAdvance -> PAParams(params, viewModel)
            is CalibrationParams.FlowRatio -> FlowRatioParams(params, viewModel)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { viewModel.nextStep() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("GCode generieren")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

@Composable
private fun TempTowerParams(params: CalibrationParams.TempTower, viewModel: CalibrationViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = params.startTemp.toString(),
                    onValueChange = { 
                        it.toIntOrNull()?.let { v -> 
                            viewModel.updateParams(params.copy(startTemp = v))
                        }
                    },
                    label = { Text("Start (°C)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = params.endTemp.toString(),
                    onValueChange = { 
                        it.toIntOrNull()?.let { v -> 
                            viewModel.updateParams(params.copy(endTemp = v))
                        }
                    },
                    label = { Text("Ende (°C)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = params.tempStep.toString(),
                    onValueChange = { 
                        it.toIntOrNull()?.let { v -> 
                            viewModel.updateParams(params.copy(tempStep = v))
                        }
                    },
                    label = { Text("Schritt (°C)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = params.bedTemp.toString(),
                    onValueChange = { 
                        it.toIntOrNull()?.let { v -> 
                            viewModel.updateParams(params.copy(bedTemp = v))
                        }
                    },
                    label = { Text("Bett (°C)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MaxFlowParams(params: CalibrationParams.MaxFlow, viewModel: CalibrationViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = params.startFlow.toString(),
                    onValueChange = { 
                        it.toFloatOrNull()?.let { v -> 
                            viewModel.updateParams(params.copy(startFlow = v))
                        }
                    },
                    label = { Text("Start (mm³/s)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = params.endFlow.toString(),
                    onValueChange = { 
                        it.toFloatOrNull()?.let { v -> 
                            viewModel.updateParams(params.copy(endFlow = v))
                        }
                    },
                    label = { Text("Ende (mm³/s)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = params.nozzleTemp.toString(),
                onValueChange = { 
                    it.toIntOrNull()?.let { v -> 
                        viewModel.updateParams(params.copy(nozzleTemp = v))
                    }
                },
                label = { Text("Düsentemperatur (°C)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PAParams(params: CalibrationParams.PressureAdvance, viewModel: CalibrationViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = params.startPA.toString(),
                    onValueChange = { 
                        it.toFloatOrNull()?.let { v -> 
                            viewModel.updateParams(params.copy(startPA = v))
                        }
                    },
                    label = { Text("Start PA") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = params.endPA.toString(),
                    onValueChange = { 
                        it.toFloatOrNull()?.let { v -> 
                            viewModel.updateParams(params.copy(endPA = v))
                        }
                    },
                    label = { Text("Ende PA") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = params.paStep.toString(),
                onValueChange = { 
                    it.toFloatOrNull()?.let { v -> 
                        viewModel.updateParams(params.copy(paStep = v))
                    }
                },
                label = { Text("Schritt") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FlowRatioParams(params: CalibrationParams.FlowRatio, viewModel: CalibrationViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = params.cubeSize.toString(),
                onValueChange = { 
                    it.toFloatOrNull()?.let { v -> 
                        viewModel.updateParams(params.copy(cubeSize = v))
                    }
                },
                label = { Text("Würfelgröße (mm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = params.lineWidth.toString(),
                onValueChange = { 
                    it.toFloatOrNull()?.let { v -> 
                        viewModel.updateParams(params.copy(lineWidth = v))
                    }
                },
                label = { Text("Linienbreite (mm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ziel-Wandstärke: ${params.lineWidth * 2}mm (2 Perimeter)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GeneratingStep() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text("Generiere GCode...")
    }
}

@Composable
private fun PrintingStep(gCode: String?, onExport: () -> Unit, onNext: () -> Unit) {
    Column {
        Text(
            text = "GCode bereit",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Der GCode wurde generiert. Exportiere ihn und drucke ihn auf deinem Drucker.")
        
        if (gCode != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${gCode.lines().size} Zeilen generiert",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onExport,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("GCode exportieren")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Druck abgeschlossen")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

@Composable
private fun ResultStep(
    testType: CalibrationTestType,
    params: CalibrationParams,
    resultValue: String,
    notes: String,
    onResultChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit
) {
    val resultLabel = when (testType) {
        CalibrationTestType.TEMP -> "Beste Temperatur (°C)"
        CalibrationTestType.MAX_FLOW -> "Max. Flow (mm³/s)"
        CalibrationTestType.PRESSURE_ADVANCE -> "Bester PA-Wert"
        CalibrationTestType.FLOW -> "Gemessene Wandstärke (mm)"
    }
    
    Column {
        Text(
            text = "Ergebnis eingeben",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = resultValue,
            onValueChange = onResultChange,
            label = { Text(resultLabel) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Notizen (optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = resultValue.isNotBlank()
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ergebnis speichern")
        }
    }
}

@Composable
private fun CompleteStep(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.padding(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Kalibrierung abgeschlossen!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Das Ergebnis wurde im Profil gespeichert.",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(onClick = onNavigateBack) {
            Text("Fertig")
        }
    }
}

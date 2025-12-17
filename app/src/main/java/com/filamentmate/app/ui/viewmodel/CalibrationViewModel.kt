package com.filamentmate.app.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filamentmate.app.data.calibration.CalibrationParams
import com.filamentmate.app.data.calibration.GCodeGenerator
import com.filamentmate.app.data.database.entity.CalibrationRunEntity
import com.filamentmate.app.data.database.entity.CalibrationTestType
import com.filamentmate.app.data.database.entity.PrinterType
import com.filamentmate.app.data.repository.CalibrationRepository
import com.filamentmate.app.data.repository.FilamentRepository
import com.filamentmate.app.data.repository.PrinterRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

enum class CalibrationStep {
    INTRO,
    PARAMETERS,
    GENERATING,
    PRINTING,
    RESULT,
    COMPLETE
}

data class CalibrationUiState(
    val testType: CalibrationTestType = CalibrationTestType.TEMP,
    val step: CalibrationStep = CalibrationStep.INTRO,
    val params: CalibrationParams = CalibrationParams.TempTower(),
    val generatedGCode: String? = null,
    val runId: Long? = null,
    val resultValue: String = "",
    val notes: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class CalibrationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val gCodeGenerator: GCodeGenerator,
    private val calibrationRepository: CalibrationRepository,
    private val printerRepository: PrinterRepository,
    private val filamentRepository: FilamentRepository
) : ViewModel() {
    
    private val testTypeString: String = savedStateHandle.get<String>("testType") ?: "TEMP"
    private val testType = CalibrationTestType.valueOf(testTypeString)
    
    private val _uiState = MutableStateFlow(CalibrationUiState(testType = testType))
    val uiState: StateFlow<CalibrationUiState> = _uiState.asStateFlow()
    
    init {
        // Setze initiale Parameter basierend auf Test-Typ
        updateParams(getDefaultParams(testType))
    }
    
    private fun getDefaultParams(type: CalibrationTestType): CalibrationParams {
        return when (type) {
            CalibrationTestType.TEMP -> CalibrationParams.TempTower()
            CalibrationTestType.MAX_FLOW -> CalibrationParams.MaxFlow()
            CalibrationTestType.PRESSURE_ADVANCE -> CalibrationParams.PressureAdvance()
            CalibrationTestType.FLOW -> CalibrationParams.FlowRatio()
        }
    }
    
    fun updateParams(params: CalibrationParams) {
        _uiState.value = _uiState.value.copy(params = params)
    }
    
    fun nextStep() {
        val currentStep = _uiState.value.step
        val nextStep = when (currentStep) {
            CalibrationStep.INTRO -> CalibrationStep.PARAMETERS
            CalibrationStep.PARAMETERS -> {
                generateGCode()
                CalibrationStep.GENERATING
            }
            CalibrationStep.GENERATING -> CalibrationStep.PRINTING
            CalibrationStep.PRINTING -> CalibrationStep.RESULT
            CalibrationStep.RESULT -> {
                saveResult()
                CalibrationStep.COMPLETE
            }
            CalibrationStep.COMPLETE -> CalibrationStep.COMPLETE
        }
        _uiState.value = _uiState.value.copy(step = nextStep)
    }
    
    fun previousStep() {
        val currentStep = _uiState.value.step
        val prevStep = when (currentStep) {
            CalibrationStep.INTRO -> CalibrationStep.INTRO
            CalibrationStep.PARAMETERS -> CalibrationStep.INTRO
            CalibrationStep.GENERATING -> CalibrationStep.PARAMETERS
            CalibrationStep.PRINTING -> CalibrationStep.PARAMETERS
            CalibrationStep.RESULT -> CalibrationStep.PRINTING
            CalibrationStep.COMPLETE -> CalibrationStep.RESULT
        }
        _uiState.value = _uiState.value.copy(step = prevStep)
    }
    
    private fun generateGCode() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val gcode = when (val params = _uiState.value.params) {
                    is CalibrationParams.TempTower -> gCodeGenerator.generateTempTower(params)
                    is CalibrationParams.MaxFlow -> gCodeGenerator.generateMaxFlowTest(params)
                    is CalibrationParams.PressureAdvance -> gCodeGenerator.generatePressureAdvanceTest(params)
                    is CalibrationParams.FlowRatio -> gCodeGenerator.generateFlowRatioTest(params)
                }
                
                // Speichere Kalibrierungs-Run
                val printerConfig = printerRepository.getPrinterConfigOnce()
                val run = CalibrationRunEntity(
                    filamentProfileId = 1, // TODO: Aus Navigation oder Selection holen
                    printerId = printerConfig?.id ?: 1,
                    printerType = printerConfig?.printerType ?: PrinterType.MOCK,
                    testType = testType,
                    paramsJson = Gson().toJson(_uiState.value.params)
                )
                val runId = calibrationRepository.insertRun(run)
                
                _uiState.value = _uiState.value.copy(
                    generatedGCode = gcode,
                    runId = runId,
                    isLoading = false,
                    step = CalibrationStep.PRINTING
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Fehler bei GCode-Generierung: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun exportGCode(): Intent? {
        val gcode = _uiState.value.generatedGCode ?: return null
        
        return try {
            val fileName = "${testType.name.lowercase()}_calibration.gcode"
            val file = File(context.cacheDir, fileName)
            file.writeText(gcode)
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = "Export-Fehler: ${e.message}")
            null
        }
    }
    
    fun updateResultValue(value: String) {
        _uiState.value = _uiState.value.copy(resultValue = value)
    }
    
    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }
    
    private fun saveResult() {
        viewModelScope.launch {
            val runId = _uiState.value.runId ?: return@launch
            val resultJson = Gson().toJson(mapOf(
                "selectedValue" to _uiState.value.resultValue,
                "testType" to testType.name
            ))
            
            try {
                calibrationRepository.updateResult(runId, resultJson)
                calibrationRepository.updateNotesAndPhotos(
                    runId,
                    _uiState.value.notes.ifEmpty { null },
                    null
                )
                _uiState.value = _uiState.value.copy(saveSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Speichern fehlgeschlagen: ${e.message}")
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

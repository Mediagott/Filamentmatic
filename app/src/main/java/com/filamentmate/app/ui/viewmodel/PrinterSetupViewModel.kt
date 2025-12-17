package com.filamentmate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filamentmate.app.data.database.entity.PrinterConfigEntity
import com.filamentmate.app.data.database.entity.PrinterType
import com.filamentmate.app.data.printer.PrinterProviderFactory
import com.filamentmate.app.data.repository.PrinterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PrinterSetupUiState(
    val config: PrinterConfigEntity? = null,
    val availableTypes: List<PrinterTypeInfo> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

data class PrinterTypeInfo(
    val type: PrinterType,
    val name: String,
    val description: String,
    val isImplemented: Boolean
)

@HiltViewModel
class PrinterSetupViewModel @Inject constructor(
    private val printerRepository: PrinterRepository,
    private val providerFactory: PrinterProviderFactory
) : ViewModel() {
    
    private val _editableConfig = MutableStateFlow<PrinterConfigEntity?>(null)
    val editableConfig: StateFlow<PrinterConfigEntity?> = _editableConfig
    
    private val _isSaving = MutableStateFlow(false)
    private val _saveSuccess = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    
    val uiState: StateFlow<PrinterSetupUiState> = combine(
        printerRepository.getPrinterConfig(),
        _isSaving,
        _saveSuccess,
        _error
    ) { config, isSaving, saveSuccess, error ->
        if (config != null && _editableConfig.value == null) {
            _editableConfig.value = config
        }
        
        val typeInfos = PrinterType.entries.map { type ->
            PrinterTypeInfo(
                type = type,
                name = when (type) {
                    PrinterType.MOCK -> "Test/Demo"
                    PrinterType.MANUAL -> "Manuell"
                    PrinterType.BAMBU -> "Bambu Lab"
                    PrinterType.OCTOPRINT -> "OctoPrint"
                    PrinterType.MOONRAKER -> "Moonraker"
                    PrinterType.PRUSALINK -> "PrusaLink"
                },
                description = providerFactory.getTypeDescription(type),
                isImplemented = providerFactory.isImplemented(type)
            )
        }
        
        PrinterSetupUiState(
            config = config,
            availableTypes = typeInfos,
            isLoading = config == null && !isSaving,
            isSaving = isSaving,
            saveSuccess = saveSuccess,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PrinterSetupUiState()
    )
    
    fun updateConfig(config: PrinterConfigEntity) {
        _editableConfig.value = config
    }
    
    fun saveConfig() {
        val config = _editableConfig.value ?: return
        
        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null
            
            try {
                printerRepository.savePrinterConfig(config)
                _saveSuccess.value = true
            } catch (e: Exception) {
                _error.value = "Fehler beim Speichern: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }
    
    fun setMockMode(enabled: Boolean) {
        _editableConfig.value = _editableConfig.value?.copy(mockModeEnabled = enabled)
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }
}

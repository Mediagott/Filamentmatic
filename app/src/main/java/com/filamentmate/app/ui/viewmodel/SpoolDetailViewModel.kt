package com.filamentmate.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filamentmate.app.data.database.entity.PrintJobEntity
import com.filamentmate.app.data.database.entity.SpoolEntity
import com.filamentmate.app.data.database.entity.SpoolOverrideEntity
import com.filamentmate.app.data.database.entity.TrayLinkEntity
import com.filamentmate.app.data.repository.FilamentRepository
import com.filamentmate.app.data.repository.PrintJobRepository
import com.filamentmate.app.data.repository.PrinterRepository
import com.filamentmate.app.data.repository.SpoolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SpoolDetailUiState(
    val spool: SpoolEntity? = null,
    val trayLink: TrayLinkEntity? = null,
    val override: SpoolOverrideEntity? = null,
    val printJobs: List<PrintJobEntity> = emptyList(),
    val isNewSpool: Boolean = true,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SpoolDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val spoolRepository: SpoolRepository,
    private val printerRepository: PrinterRepository,
    private val filamentRepository: FilamentRepository,
    private val printJobRepository: PrintJobRepository
) : ViewModel() {
    
    private val spoolId: Long = savedStateHandle.get<Long>("spoolId") ?: -1L
    private val isNewSpool = spoolId == -1L
    
    private val _editableSpool = MutableStateFlow(
        SpoolEntity(
            name = "",
            material = "PLA",
            brand = "",
            color = "",
            diameterMm = 1.75f,
            remainingWeightG = 1000f
        )
    )
    val editableSpool: StateFlow<SpoolEntity> = _editableSpool.asStateFlow()
    
    private val _isSaving = MutableStateFlow(false)
    private val _saveSuccess = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    
    val uiState: StateFlow<SpoolDetailUiState> = if (isNewSpool) {
        combine(
            _editableSpool,
            _isSaving,
            _saveSuccess,
            _error
        ) { spool, isSaving, saveSuccess, error ->
            SpoolDetailUiState(
                spool = spool,
                isNewSpool = true,
                isLoading = false,
                isSaving = isSaving,
                saveSuccess = saveSuccess,
                error = error
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SpoolDetailUiState(isNewSpool = true, isLoading = false)
        )
    } else {
        combine(
            spoolRepository.getSpoolById(spoolId),
            printerRepository.getTrayLinkBySpoolId(spoolId),
            filamentRepository.getOverrideBySpoolId(spoolId),
            printJobRepository.getPrintJobsBySpoolId(spoolId),
            _isSaving,
            _saveSuccess,
            _error
        ) { values ->
            val spool = values[0] as SpoolEntity?
            val trayLink = values[1] as TrayLinkEntity?
            val override = values[2] as SpoolOverrideEntity?
            @Suppress("UNCHECKED_CAST")
            val printJobs = values[3] as List<PrintJobEntity>
            val isSaving = values[4] as Boolean
            val saveSuccess = values[5] as Boolean
            val error = values[6] as String?
            
            if (spool != null && _editableSpool.value.id == 0L) {
                _editableSpool.value = spool
            }
            
            SpoolDetailUiState(
                spool = spool,
                trayLink = trayLink,
                override = override,
                printJobs = printJobs,
                isNewSpool = false,
                isLoading = spool == null,
                isSaving = isSaving,
                saveSuccess = saveSuccess,
                error = error
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SpoolDetailUiState(isNewSpool = false)
        )
    }
    
    fun updateSpool(spool: SpoolEntity) {
        _editableSpool.value = spool
    }
    
    fun saveSpool() {
        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null
            
            try {
                val spool = _editableSpool.value
                if (isNewSpool) {
                    spoolRepository.insertSpool(spool)
                } else {
                    spoolRepository.updateSpool(spool)
                }
                _saveSuccess.value = true
            } catch (e: Exception) {
                _error.value = "Fehler beim Speichern: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }
    
    fun linkToSlot(slotGroup: String, slotIndex: Int) {
        viewModelScope.launch {
            try {
                val config = printerRepository.getPrinterConfigOnce()
                if (config != null) {
                    val trayLink = TrayLinkEntity(
                        printerId = config.id,
                        slotGroup = slotGroup,
                        slotIndex = slotIndex,
                        spoolId = if (isNewSpool) null else spoolId
                    )
                    printerRepository.linkSpoolToSlot(trayLink)
                }
            } catch (e: Exception) {
                _error.value = "Fehler beim Verknüpfen: ${e.message}"
            }
        }
    }
    
    fun unlinkFromSlot() {
        viewModelScope.launch {
            try {
                if (!isNewSpool) {
                    printerRepository.unlinkSpool(spoolId)
                }
            } catch (e: Exception) {
                _error.value = "Fehler beim Trennen: ${e.message}"
            }
        }
    }
    
    fun recordConsumption(usedWeightG: Float, jobName: String = "Manueller Eintrag") {
        viewModelScope.launch {
            try {
                val config = printerRepository.getPrinterConfigOnce()
                if (config != null && !isNewSpool) {
                    // PrintJob erstellen
                    val printJob = PrintJobEntity(
                        printerId = config.id,
                        jobName = jobName,
                        spoolId = spoolId,
                        usedWeightG = usedWeightG,
                        source = "MANUAL"
                    )
                    printJobRepository.insertPrintJob(printJob)
                    
                    // Restgewicht reduzieren
                    spoolRepository.decrementRemainingWeight(spoolId, usedWeightG)
                }
            } catch (e: Exception) {
                _error.value = "Fehler beim Eintragen: ${e.message}"
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }
}

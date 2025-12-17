package com.filamentmate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filamentmate.app.data.database.entity.PrintJobEntity
import com.filamentmate.app.data.database.entity.PrinterConfigEntity
import com.filamentmate.app.data.database.entity.SpoolEntity
import com.filamentmate.app.data.database.entity.TrayLinkEntity
import com.filamentmate.app.data.printer.ConnectionState
import com.filamentmate.app.data.printer.PrinterConnectionManager
import com.filamentmate.app.data.printer.PrinterStatus
import com.filamentmate.app.data.repository.PrintJobRepository
import com.filamentmate.app.data.repository.PrinterRepository
import com.filamentmate.app.data.repository.SpoolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SlotWithSpool(
    val trayLink: TrayLinkEntity,
    val spool: SpoolEntity?
)

data class DashboardUiState(
    val printerConfig: PrinterConfigEntity? = null,
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val printerStatus: PrinterStatus = PrinterStatus(),
    val activeSlots: List<SlotWithSpool> = emptyList(),
    val lowWeightSpools: List<SpoolEntity> = emptyList(),
    val recentJobs: List<PrintJobWithSpool> = emptyList(),
    val totalSpoolCount: Int = 0,
    val totalFilamentWeightG: Float = 0f,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val printerRepository: PrinterRepository,
    private val spoolRepository: SpoolRepository,
    private val printJobRepository: PrintJobRepository,
    private val printerConnectionManager: PrinterConnectionManager
) : ViewModel() {
    
    private val LOW_WEIGHT_THRESHOLD = 100f // Gramm
    
    val uiState: StateFlow<DashboardUiState> = combine(
        printerRepository.getPrinterConfig(),
        printerConnectionManager.connectionState,
        printerConnectionManager.printerStatus,
        spoolRepository.getAllSpools(),
        printJobRepository.getLatestPrintJobs()
    ) { config, connState, printerStatus, allSpools, jobs ->
        
        // Lade Tray-Links für den Drucker
        val activeSlots = if (config != null) {
            loadActiveSlots(config.id, allSpools)
        } else {
            emptyList()
        }
        
        // Low-Weight Spulen
        val lowWeightSpools = allSpools.filter { it.remainingWeightG < LOW_WEIGHT_THRESHOLD }
        
        // Spulen-Map für Jobs
        val spoolMap = allSpools.associateBy { it.id }
        val recentJobs = jobs.take(5).map { job ->
            PrintJobWithSpool(
                printJob = job,
                spool = job.spoolId?.let { spoolMap[it] }
            )
        }
        
        // Statistiken
        val totalWeight = allSpools.sumOf { it.remainingWeightG.toDouble() }.toFloat()
        
        DashboardUiState(
            printerConfig = config,
            connectionState = connState,
            printerStatus = printerStatus,
            activeSlots = activeSlots,
            lowWeightSpools = lowWeightSpools,
            recentJobs = recentJobs,
            totalSpoolCount = allSpools.size,
            totalFilamentWeightG = totalWeight,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )
    
    private suspend fun loadActiveSlots(printerId: Long, allSpools: List<SpoolEntity>): List<SlotWithSpool> {
        val spoolMap = allSpools.associateBy { it.id }
        val slots = mutableListOf<SlotWithSpool>()
        
        // Manuell die TrayLinks laden (synchron in diesem combine-Flow nicht ideal, 
        // aber für Dashboard ausreichend)
        viewModelScope.launch {
            printerRepository.getTrayLinksForPrinter(printerId).collect { trayLinks ->
                // Wird durch den combine-Flow aktualisiert
            }
        }
        
        return slots
    }
    
    // Separate Flow für TrayLinks
    private val _activeSlots = MutableStateFlow<List<SlotWithSpool>>(emptyList())
    
    init {
        viewModelScope.launch {
            combine(
                printerRepository.getPrinterConfig(),
                spoolRepository.getAllSpools()
            ) { config, spools ->
                config to spools
            }.collect { (config, spools) ->
                if (config != null) {
                    printerRepository.getTrayLinksForPrinter(config.id).collect { trayLinks ->
                        val spoolMap = spools.associateBy { it.id }
                        _activeSlots.value = trayLinks.map { link ->
                            SlotWithSpool(
                                trayLink = link,
                                spool = link.spoolId?.let { spoolMap[it] }
                            )
                        }
                    }
                }
            }
        }
    }
    
    val activeSlots: StateFlow<List<SlotWithSpool>> = _activeSlots
}

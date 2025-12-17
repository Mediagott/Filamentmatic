package com.filamentmate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filamentmate.app.data.database.entity.PrintJobEntity
import com.filamentmate.app.data.database.entity.SpoolEntity
import com.filamentmate.app.data.repository.PrintJobRepository
import com.filamentmate.app.data.repository.SpoolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PrintHistoryUiState(
    val printJobs: List<PrintJobWithSpool> = emptyList(),
    val isLoading: Boolean = true
)

data class PrintJobWithSpool(
    val printJob: PrintJobEntity,
    val spool: SpoolEntity?
)

@HiltViewModel
class PrintHistoryViewModel @Inject constructor(
    private val printJobRepository: PrintJobRepository,
    private val spoolRepository: SpoolRepository
) : ViewModel() {
    
    val uiState: StateFlow<PrintHistoryUiState> = combine(
        printJobRepository.getLatestPrintJobs(),
        spoolRepository.getAllSpools()
    ) { printJobs, spools ->
        val spoolMap = spools.associateBy { it.id }
        val jobsWithSpools = printJobs.map { job ->
            PrintJobWithSpool(
                printJob = job,
                spool = job.spoolId?.let { spoolMap[it] }
            )
        }
        PrintHistoryUiState(
            printJobs = jobsWithSpools,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PrintHistoryUiState()
    )
    
    fun deletePrintJob(id: Long) {
        viewModelScope.launch {
            printJobRepository.deletePrintJob(id)
        }
    }
}

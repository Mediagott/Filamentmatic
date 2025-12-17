package com.filamentmate.app.data.printer.provider

import com.filamentmate.app.data.database.entity.PrinterConfigEntity
import com.filamentmate.app.data.database.entity.PrinterType
import com.filamentmate.app.data.printer.JobState
import com.filamentmate.app.data.printer.PrinterCapabilities
import com.filamentmate.app.data.printer.PrinterProvider
import com.filamentmate.app.data.printer.PrinterStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.random.Random

/**
 * Mock-Provider für Tests und Demos.
 * Simuliert einen Drucker mit AMS und wechselnden Status.
 */
class MockPrinterProvider @Inject constructor() : PrinterProvider {
    
    override val type: PrinterType = PrinterType.MOCK
    
    override val capabilities = PrinterCapabilities(
        canUploadGcode = true,
        canStartPrint = true,
        canUploadAndStart = true,
        canObserveStatus = true,
        canReadSlots = true
    )
    
    private var config: PrinterConfigEntity? = null
    private var isRunning = false
    private var currentJobName: String? = null
    private var currentJobState = JobState.IDLE
    private var currentProgress = 0f
    private var simulatedSlot = 0
    
    override fun observeStatus(): Flow<PrinterStatus> = flow {
        isRunning = true
        while (isRunning) {
            // Simuliere Status-Updates
            val status = PrinterStatus(
                connected = true,
                activeSlotGroup = "AMS",
                activeSlotIndex = simulatedSlot,
                jobName = currentJobName,
                jobState = currentJobState,
                jobProgress = currentProgress,
                usedWeightG = if (currentJobState == JobState.PRINTING) {
                    currentProgress * 0.5f // Simuliert ~50g Verbrauch bei 100%
                } else null
            )
            emit(status)
            
            // Simuliere Fortschritt bei laufendem Druck
            if (currentJobState == JobState.PRINTING) {
                currentProgress += Random.nextFloat() * 5f
                if (currentProgress >= 100f) {
                    currentProgress = 100f
                    currentJobState = JobState.COMPLETED
                }
            }
            
            delay(config?.pollIntervalSec?.times(1000L) ?: 5000L)
        }
    }
    
    override suspend fun testConnection(config: PrinterConfigEntity): Result<Unit> {
        // Mock: Verbindung immer erfolgreich
        delay(500) // Simuliere Netzwerk-Latenz
        return Result.success(Unit)
    }
    
    override suspend fun uploadGcode(fileName: String, bytes: ByteArray): Result<String> {
        delay(1000) // Simuliere Upload
        val remotePath = "/mock/uploads/$fileName"
        return Result.success(remotePath)
    }
    
    override suspend fun startPrint(remotePathOrId: String): Result<Unit> {
        delay(500) // Simuliere Start
        currentJobName = remotePathOrId.substringAfterLast("/")
        currentJobState = JobState.PRINTING
        currentProgress = 0f
        simulatedSlot = Random.nextInt(0, 4) // Zufälliger AMS-Slot
        return Result.success(Unit)
    }
    
    override suspend fun initialize(config: PrinterConfigEntity) {
        this.config = config
    }
    
    override suspend fun shutdown() {
        isRunning = false
        currentJobName = null
        currentJobState = JobState.IDLE
        currentProgress = 0f
    }
    
    /**
     * Setzt den simulierten Status (für Tests).
     */
    fun setSimulatedStatus(slotIndex: Int, jobState: JobState, progress: Float = 0f) {
        simulatedSlot = slotIndex
        currentJobState = jobState
        currentProgress = progress
    }
}

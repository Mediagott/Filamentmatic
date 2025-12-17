package com.filamentmate.app.data.printer.provider

import com.filamentmate.app.data.database.entity.PrinterConfigEntity
import com.filamentmate.app.data.database.entity.PrinterType
import com.filamentmate.app.data.printer.JobState
import com.filamentmate.app.data.printer.PrinterCapabilities
import com.filamentmate.app.data.printer.PrinterProvider
import com.filamentmate.app.data.printer.PrinterStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * Stub-Provider für Moonraker (Klipper).
 * TODO: Echte Implementierung mit REST/WebSocket-API.
 */
class MoonrakerPrinterProvider @Inject constructor() : PrinterProvider {
    
    override val type: PrinterType = PrinterType.MOONRAKER
    
    override val capabilities = PrinterCapabilities(
        canUploadGcode = true,
        canStartPrint = true,
        canUploadAndStart = true,
        canObserveStatus = true,
        canReadSlots = false
    )
    
    private var config: PrinterConfigEntity? = null
    
    override fun observeStatus(): Flow<PrinterStatus> = flowOf(
        PrinterStatus(
            connected = false,
            jobState = JobState.IDLE,
            errorMessage = "Moonraker Provider noch nicht implementiert"
        )
    )
    
    override suspend fun testConnection(config: PrinterConfigEntity): Result<Unit> {
        return Result.failure(NotImplementedError("Moonraker Provider noch nicht implementiert"))
    }
    
    override suspend fun uploadGcode(fileName: String, bytes: ByteArray): Result<String> {
        return Result.failure(NotImplementedError("Moonraker Provider noch nicht implementiert"))
    }
    
    override suspend fun startPrint(remotePathOrId: String): Result<Unit> {
        return Result.failure(NotImplementedError("Moonraker Provider noch nicht implementiert"))
    }
    
    override suspend fun initialize(config: PrinterConfigEntity) {
        this.config = config
    }
    
    override suspend fun shutdown() {
        // Stub
    }
}

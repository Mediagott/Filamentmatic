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
 * Manual-Provider für manuelle Eingabe.
 * Keine automatische Kommunikation mit dem Drucker.
 * Status wird manuell vom Benutzer eingegeben.
 */
class ManualPrinterProvider @Inject constructor() : PrinterProvider {
    
    override val type: PrinterType = PrinterType.MANUAL
    
    override val capabilities = PrinterCapabilities(
        canUploadGcode = false,
        canStartPrint = false,
        canUploadAndStart = false,
        canObserveStatus = false,
        canReadSlots = false
    )
    
    private var config: PrinterConfigEntity? = null
    
    override fun observeStatus(): Flow<PrinterStatus> = flowOf(
        PrinterStatus(
            connected = false,
            jobState = JobState.IDLE,
            errorMessage = "Manueller Modus - keine automatische Status-Abfrage"
        )
    )
    
    override suspend fun testConnection(config: PrinterConfigEntity): Result<Unit> {
        // Manual: Keine Verbindung möglich
        return Result.failure(
            UnsupportedOperationException("Manueller Modus unterstützt keine automatische Verbindung")
        )
    }
    
    override suspend fun uploadGcode(fileName: String, bytes: ByteArray): Result<String> {
        // Manual: Kein Upload möglich - User muss exportieren
        return Result.failure(
            UnsupportedOperationException(
                "Manueller Modus unterstützt keinen GCode-Upload. " +
                "Bitte exportieren Sie die Datei und übertragen Sie sie manuell."
            )
        )
    }
    
    override suspend fun startPrint(remotePathOrId: String): Result<Unit> {
        return Result.failure(
            UnsupportedOperationException("Manueller Modus unterstützt keinen Druck-Start")
        )
    }
    
    override suspend fun initialize(config: PrinterConfigEntity) {
        this.config = config
    }
    
    override suspend fun shutdown() {
        // Nichts zu tun
    }
}

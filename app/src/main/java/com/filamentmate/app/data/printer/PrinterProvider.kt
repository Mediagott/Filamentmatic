package com.filamentmate.app.data.printer

import com.filamentmate.app.data.database.entity.PrinterConfigEntity
import com.filamentmate.app.data.database.entity.PrinterType
import kotlinx.coroutines.flow.Flow

/**
 * Interface für Drucker-Provider.
 * Jeder Provider implementiert die Kommunikation mit einem bestimmten Drucker-Typ.
 */
interface PrinterProvider {
    
    /**
     * Der Drucker-Typ, den dieser Provider unterstützt.
     */
    val type: PrinterType
    
    /**
     * Fähigkeiten dieses Providers.
     */
    val capabilities: PrinterCapabilities
    
    /**
     * Beobachtet den Status des Druckers als Flow.
     */
    fun observeStatus(): Flow<PrinterStatus>
    
    /**
     * Testet die Verbindung zum Drucker.
     */
    suspend fun testConnection(config: PrinterConfigEntity): Result<Unit>
    
    /**
     * Lädt eine GCode-Datei auf den Drucker hoch.
     * @return Remote-Pfad oder ID der hochgeladenen Datei
     */
    suspend fun uploadGcode(fileName: String, bytes: ByteArray): Result<String>
    
    /**
     * Startet einen Druck mit der angegebenen Datei.
     * @param remotePathOrId Remote-Pfad oder ID der Datei
     */
    suspend fun startPrint(remotePathOrId: String): Result<Unit>
    
    /**
     * Kombiniert Upload und Start in einem Schritt (falls unterstützt).
     */
    suspend fun uploadAndStartPrint(fileName: String, bytes: ByteArray): Result<Unit> {
        return if (capabilities.canUploadAndStart) {
            val uploadResult = uploadGcode(fileName, bytes)
            uploadResult.fold(
                onSuccess = { path -> startPrint(path) },
                onFailure = { Result.failure(it) }
            )
        } else {
            Result.failure(UnsupportedOperationException("Upload and start not supported"))
        }
    }
    
    /**
     * Initialisiert den Provider mit der Konfiguration.
     */
    suspend fun initialize(config: PrinterConfigEntity)
    
    /**
     * Stoppt den Provider und gibt Ressourcen frei.
     */
    suspend fun shutdown()
}

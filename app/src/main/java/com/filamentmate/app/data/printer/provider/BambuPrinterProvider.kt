package com.filamentmate.app.data.printer.provider

import android.util.Log
import com.filamentmate.app.data.database.entity.PrinterConfigEntity
import com.filamentmate.app.data.database.entity.PrinterType
import com.filamentmate.app.data.printer.JobState
import com.filamentmate.app.data.printer.PrinterCapabilities
import com.filamentmate.app.data.printer.PrinterProvider
import com.filamentmate.app.data.printer.PrinterStatus
import com.filamentmate.app.data.printer.bambu.BambuMqttClient
import com.filamentmate.app.data.printer.bambu.BambuReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * PrinterProvider für Bambu Lab X1C/P1S/P1P Drucker.
 * Verbindet sich über lokales MQTT mit TLS.
 */
class BambuPrinterProvider @Inject constructor(
    private val mqttClient: BambuMqttClient
) : PrinterProvider {
    
    companion object {
        private const val TAG = "BambuProvider"
    }
    
    override val type: PrinterType = PrinterType.BAMBU
    
    override val capabilities = PrinterCapabilities(
        canUploadGcode = false,  // Bambu verwendet eigenes SD-Card/Cloud System
        canStartPrint = false,   // Kann nur pausieren/stoppen
        canUploadAndStart = false,
        canObserveStatus = true,
        canReadSlots = true      // AMS Slots auslesen
    )
    
    private var config: PrinterConfigEntity? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _status = MutableStateFlow(PrinterStatus())
    
    init {
        // Listen to MQTT reports
        scope.launch {
            mqttClient.reports.collect { report ->
                updateStatusFromReport(report)
            }
        }
        
        // Listen to connection state
        scope.launch {
            mqttClient.isConnected.collect { connected ->
                _status.value = _status.value.copy(connected = connected)
            }
        }
    }
    
    override fun observeStatus(): Flow<PrinterStatus> = _status.asStateFlow()
    
    override suspend fun testConnection(config: PrinterConfigEntity): Result<Unit> {
        return try {
            val serialNumber = config.accessToken?.substringBefore(":") 
                ?: return Result.failure(IllegalArgumentException("Seriennummer fehlt"))
            val accessCode = config.accessToken?.substringAfter(":") 
                ?: return Result.failure(IllegalArgumentException("Access-Code fehlt"))
            
            mqttClient.connect(config.ip, serialNumber, accessCode)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Connection test failed: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun uploadGcode(fileName: String, bytes: ByteArray): Result<String> {
        // Bambu Lab verwendet SD-Karte/Cloud, kein direkter Upload über MQTT
        return Result.failure(UnsupportedOperationException(
            "Bambu Lab unterstützt keinen direkten GCode-Upload. " +
            "Bitte verwende die SD-Karte oder Bambu Studio."
        ))
    }
    
    override suspend fun startPrint(remotePathOrId: String): Result<Unit> {
        // Kann keine Drucke starten, nur pausieren/stoppen
        return Result.failure(UnsupportedOperationException(
            "Drucke müssen über Bambu Studio gestartet werden."
        ))
    }
    
    /**
     * Pausiert den aktuellen Druck.
     */
    fun pausePrint() {
        mqttClient.pausePrint()
    }
    
    /**
     * Setzt den pausierten Druck fort.
     */
    fun resumePrint() {
        mqttClient.resumePrint()
    }
    
    /**
     * Stoppt den aktuellen Druck.
     */
    fun stopPrint() {
        mqttClient.stopPrint()
    }
    
    override suspend fun initialize(config: PrinterConfigEntity) {
        this.config = config
        
        if (config.ip.isNotBlank() && !config.accessToken.isNullOrBlank()) {
            try {
                // Access Token Format: "SERIAL:ACCESSCODE"
                val parts = config.accessToken.split(":")
                if (parts.size == 2) {
                    mqttClient.connect(config.ip, parts[0], parts[1])
                } else {
                    Log.e(TAG, "Invalid access token format. Expected: SERIAL:ACCESSCODE")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize: ${e.message}", e)
            }
        }
    }
    
    override suspend fun shutdown() {
        mqttClient.disconnect()
    }
    
    private fun updateStatusFromReport(report: BambuReport) {
        val print = report.print ?: return
        
        val jobState = when (print.gcodeState?.uppercase()) {
            "IDLE" -> JobState.IDLE
            "PREPARE" -> JobState.STARTING
            "RUNNING" -> JobState.PRINTING
            "PAUSE" -> JobState.PAUSED
            "FINISH" -> JobState.COMPLETED
            "FAILED" -> JobState.FAILED
            else -> JobState.IDLE
        }
        
        // Parse AMS active slot
        val activeSlot = print.ams?.trayNow?.toIntOrNull()
        
        // Build slot weights from AMS data
        val slotWeights = mutableMapOf<Int, Float>()
        print.ams?.units?.forEach { unit ->
            unit.trays?.forEach { tray ->
                val slotIndex = tray.id?.toIntOrNull() ?: return@forEach
                val remainPercent = tray.remainPercent ?: 0
                // Bambu gibt nur Prozent an, wir schätzen das Gewicht
                // Standard-Spule = 1000g
                slotWeights[slotIndex] = remainPercent * 10f
            }
        }
        
        _status.value = PrinterStatus(
            connected = true,
            jobState = jobState,
            jobName = print.gcodeFile,
            jobProgress = print.progressPercent?.toFloat() ?: 0f,
            nozzleTempC = print.nozzleTemp,
            bedTempC = print.bedTemp,
            chamberTempC = print.chamberTemp,
            activeSlotIndex = activeSlot,
            slotRemainingWeightG = slotWeights
        )
    }
}

package com.filamentmate.app.data.printer

import com.filamentmate.app.data.database.entity.PrinterConfigEntity
import com.filamentmate.app.data.database.entity.PrinterType
import com.filamentmate.app.data.repository.PrinterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager für die aktive Drucker-Verbindung.
 * Verwaltet den aktuellen Provider und Status.
 */
@Singleton
class PrinterConnectionManager @Inject constructor(
    private val printerRepository: PrinterRepository,
    private val providerFactory: PrinterProviderFactory
) {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private var currentProvider: PrinterProvider? = null
    private var currentConfig: PrinterConfigEntity? = null
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _printerStatus = MutableStateFlow(PrinterStatus())
    val printerStatus: StateFlow<PrinterStatus> = _printerStatus.asStateFlow()
    
    init {
        // Beobachte Konfigurationsänderungen
        scope.launch {
            printerRepository.getPrinterConfig().collectLatest { config ->
                if (config != null) {
                    handleConfigChange(config)
                }
            }
        }
    }
    
    private suspend fun handleConfigChange(config: PrinterConfigEntity) {
        // Wenn sich der Provider-Typ geändert hat, neu verbinden
        if (currentConfig?.printerType != config.printerType || currentProvider == null) {
            disconnect()
            connect(config)
        }
        currentConfig = config
    }
    
    private suspend fun connect(config: PrinterConfigEntity) {
        _connectionState.value = ConnectionState.Connecting
        
        val provider = providerFactory.getProvider(
            if (config.mockModeEnabled) PrinterType.MOCK else config.printerType
        )
        
        try {
            provider.initialize(config)
            currentProvider = provider
            
            // Starte Status-Beobachtung
            scope.launch {
                provider.observeStatus().collectLatest { status ->
                    _printerStatus.value = status
                    _connectionState.value = if (status.connected) {
                        ConnectionState.Connected
                    } else {
                        ConnectionState.Disconnected
                    }
                }
            }
            
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error(e.message ?: "Verbindungsfehler")
        }
    }
    
    private suspend fun disconnect() {
        currentProvider?.shutdown()
        currentProvider = null
        _connectionState.value = ConnectionState.Disconnected
        _printerStatus.value = PrinterStatus()
    }
    
    suspend fun testConnection(): Result<Unit> {
        val config = currentConfig ?: return Result.failure(
            IllegalStateException("Keine Drucker-Konfiguration vorhanden")
        )
        val provider = currentProvider ?: return Result.failure(
            IllegalStateException("Kein Provider aktiv")
        )
        return provider.testConnection(config)
    }
    
    suspend fun uploadGcode(fileName: String, bytes: ByteArray): Result<String> {
        val provider = currentProvider ?: return Result.failure(
            IllegalStateException("Kein Provider aktiv")
        )
        return provider.uploadGcode(fileName, bytes)
    }
    
    suspend fun startPrint(remotePathOrId: String): Result<Unit> {
        val provider = currentProvider ?: return Result.failure(
            IllegalStateException("Kein Provider aktiv")
        )
        return provider.startPrint(remotePathOrId)
    }
    
    suspend fun uploadAndStartPrint(fileName: String, bytes: ByteArray): Result<Unit> {
        val provider = currentProvider ?: return Result.failure(
            IllegalStateException("Kein Provider aktiv")
        )
        return provider.uploadAndStartPrint(fileName, bytes)
    }
    
    fun getCurrentCapabilities(): PrinterCapabilities {
        return currentProvider?.capabilities ?: PrinterCapabilities()
    }
    
    fun getCurrentProviderType(): PrinterType? {
        return currentProvider?.type
    }
}

sealed class ConnectionState {
    data object Disconnected : ConnectionState()
    data object Connecting : ConnectionState()
    data object Connected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

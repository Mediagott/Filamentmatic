package com.filamentmate.app.data.printer

/**
 * Fähigkeiten eines Drucker-Providers.
 */
data class PrinterCapabilities(
    val canUploadGcode: Boolean = false,
    val canStartPrint: Boolean = false,
    val canUploadAndStart: Boolean = false,
    val canObserveStatus: Boolean = false,
    val canReadSlots: Boolean = false
)

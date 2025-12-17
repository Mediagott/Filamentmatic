package com.filamentmate.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Drucker-Typ Enum für verschiedene Provider.
 */
enum class PrinterType {
    BAMBU,
    OCTOPRINT,
    MOONRAKER,
    PRUSALINK,
    MANUAL,
    MOCK
}

/**
 * Drucker-Konfiguration in der Datenbank.
 * Es gibt nur einen aktiven Drucker (id = 1).
 */
@Entity(tableName = "printer_config")
data class PrinterConfigEntity(
    @PrimaryKey
    val id: Long = 1,                        // Immer 1, nur ein Drucker
    val printerName: String = "Mein Drucker",
    val printerType: PrinterType = PrinterType.MANUAL,
    val ip: String = "",
    val port: Int = 80,
    val accessToken: String? = null,
    val enabled: Boolean = true,
    val pollIntervalSec: Int = 5,
    val mockModeEnabled: Boolean = false
)

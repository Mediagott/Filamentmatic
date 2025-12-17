package com.filamentmate.app.data.database

import com.filamentmate.app.data.database.dao.FilamentProfileDao
import com.filamentmate.app.data.database.dao.PrinterConfigDao
import com.filamentmate.app.data.database.dao.SpoolDao
import com.filamentmate.app.data.database.dao.TrayLinkDao
import com.filamentmate.app.data.database.entity.FilamentProfileEntity
import com.filamentmate.app.data.database.entity.PrinterConfigEntity
import com.filamentmate.app.data.database.entity.PrinterType
import com.filamentmate.app.data.database.entity.SpoolEntity
import com.filamentmate.app.data.database.entity.TrayLinkEntity
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Füllt die Datenbank mit Beispiel-Daten beim ersten Start.
 */
@Singleton
class DatabaseSeeder @Inject constructor(
    private val spoolDao: SpoolDao,
    private val filamentProfileDao: FilamentProfileDao,
    private val printerConfigDao: PrinterConfigDao,
    private val trayLinkDao: TrayLinkDao
) {
    
    suspend fun seedIfEmpty() {
        // Prüfe ob bereits Daten vorhanden sind
        val existingSpools = spoolDao.getAllSpools().first()
        if (existingSpools.isNotEmpty()) {
            return // Bereits geseeded
        }
        
        // Seed Drucker-Konfiguration
        seedPrinterConfig()
        
        // Seed Filament-Profile
        seedFilamentProfiles()
        
        // Seed Beispiel-Spulen
        seedSpools()
        
        // Seed Tray-Links (AMS Slots)
        seedTrayLinks()
    }
    
    private suspend fun seedPrinterConfig() {
        val config = PrinterConfigEntity(
            id = 1,
            printerName = "Mein 3D-Drucker",
            printerType = PrinterType.MOCK,
            ip = "",
            port = 80,
            enabled = true,
            pollIntervalSec = 5,
            mockModeEnabled = true
        )
        printerConfigDao.insert(config)
    }
    
    private suspend fun seedFilamentProfiles() {
        val profiles = listOf(
            FilamentProfileEntity(
                material = "PLA",
                brand = "Bambu Lab",
                color = null,
                diameterMm = 1.75f,
                recommendedTempC = 220,
                tempMinC = 190,
                tempMaxC = 230,
                maxVolumetricFlowMm3s = 21f,
                pressureAdvanceValue = 0.04f,
                pressureAdvanceLabel = "PA",
                flowRatio = 0.98f,
                notes = "Standard PLA Profil"
            ),
            FilamentProfileEntity(
                material = "PETG",
                brand = "Polymaker",
                color = null,
                diameterMm = 1.75f,
                recommendedTempC = 250,
                tempMinC = 230,
                tempMaxC = 260,
                maxVolumetricFlowMm3s = 15f,
                pressureAdvanceValue = 0.06f,
                pressureAdvanceLabel = "PA",
                flowRatio = 0.95f,
                notes = "PETG benötigt höhere Temperaturen"
            )
        )
        profiles.forEach { filamentProfileDao.insert(it) }
    }
    
    private suspend fun seedSpools() {
        val spools = listOf(
            SpoolEntity(
                name = "PLA Schwarz",
                material = "PLA",
                brand = "Bambu Lab",
                color = "Schwarz",
                colorHex = "#000000",
                diameterMm = 1.75f,
                emptySpoolWeightG = 250f,
                startWeightG = 1250f,
                remainingWeightG = 850f,
                densityGCm3 = 1.24f,
                note = "Beispiel-Spule"
            ),
            SpoolEntity(
                name = "PLA Weiß",
                material = "PLA",
                brand = "Bambu Lab",
                color = "Weiß",
                colorHex = "#FFFFFF",
                diameterMm = 1.75f,
                emptySpoolWeightG = 250f,
                startWeightG = 1250f,
                remainingWeightG = 1000f,
                densityGCm3 = 1.24f,
                note = "Neue Spule"
            ),
            SpoolEntity(
                name = "PETG Blau",
                material = "PETG",
                brand = "Polymaker",
                color = "Blau",
                colorHex = "#1E88E5",
                diameterMm = 1.75f,
                emptySpoolWeightG = 230f,
                startWeightG = 1230f,
                remainingWeightG = 600f,
                densityGCm3 = 1.27f,
                note = "PolyLite PETG"
            )
        )
        spools.forEach { spoolDao.insert(it) }
    }
    
    private suspend fun seedTrayLinks() {
        // Erstelle 4 AMS Slots
        for (i in 0..3) {
            val trayLink = TrayLinkEntity(
                printerId = 1,
                slotGroup = "AMS",
                slotIndex = i,
                spoolId = if (i < 3) (i + 1).toLong() else null // Erste 3 Slots belegt
            )
            trayLinkDao.upsert(trayLink)
        }
    }
}

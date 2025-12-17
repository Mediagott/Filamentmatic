package com.filamentmate.app.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Filament-Profil für einen bestimmten Filament-Typ.
 * Speichert Kalibrierungsergebnisse und empfohlene Einstellungen.
 */
@Entity(
    tableName = "filament_profiles",
    indices = [
        Index(value = ["material", "brand", "color"])
    ]
)
data class FilamentProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val material: String,                    // z.B. "PLA", "PETG"
    val brand: String,                       // z.B. "Bambu Lab"
    val color: String? = null,               // Optional: spezifische Farbe
    val diameterMm: Float = 1.75f,
    
    // Temperatur-Einstellungen
    val recommendedTempC: Int? = null,
    val tempMinC: Int? = null,
    val tempMaxC: Int? = null,
    
    // Flow-Einstellungen
    val maxVolumetricFlowMm3s: Float? = null,
    
    // Pressure Advance
    val pressureAdvanceValue: Float? = null,
    val pressureAdvanceLabel: String? = null, // z.B. "K" für Klipper, "PA" für Marlin
    
    // Flow Ratio
    val flowRatio: Float? = null,            // z.B. 0.98 für 98%
    
    val notes: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

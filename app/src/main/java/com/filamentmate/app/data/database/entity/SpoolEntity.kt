package com.filamentmate.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Repräsentiert eine Filamentspule in der Datenbank.
 */
@Entity(tableName = "spools")
data class SpoolEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val material: String,                    // z.B. "PLA", "PETG", "ABS"
    val brand: String,                       // z.B. "Bambu Lab", "Polymaker"
    val color: String,                       // z.B. "Schwarz", "Weiß"
    val colorHex: String? = null,            // z.B. "#FF0000"
    val diameterMm: Float = 1.75f,           // Standard 1.75mm
    val emptySpoolWeightG: Float? = null,    // Tara-Gewicht der leeren Spule
    val startWeightG: Float? = null,         // Gesamtgewicht beim Kauf
    val remainingWeightG: Float = 1000f,     // Aktuelles Restgewicht
    val densityGCm3: Float? = null,          // Dichte (g/cm³)
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

package com.filamentmate.app.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Kalibrierungs-Test-Typen.
 */
enum class CalibrationTestType {
    TEMP,              // Temperatur-Turm
    MAX_FLOW,          // Maximaler volumetrischer Flow
    PRESSURE_ADVANCE,  // Pressure Advance / Linear Advance
    FLOW               // Flow-Kalibrierung
}

/**
 * Einzelner Kalibrierungs-Durchlauf.
 */
@Entity(
    tableName = "calibration_runs",
    foreignKeys = [
        ForeignKey(
            entity = FilamentProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["filamentProfileId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SpoolEntity::class,
            parentColumns = ["id"],
            childColumns = ["spoolId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = PrinterConfigEntity::class,
            parentColumns = ["id"],
            childColumns = ["printerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["filamentProfileId"]),
        Index(value = ["spoolId"]),
        Index(value = ["printerId"]),
        Index(value = ["startedAt"])
    ]
)
data class CalibrationRunEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filamentProfileId: Long,
    val spoolId: Long? = null,               // Optional: spezifische Spule
    val printerId: Long,
    val printerType: PrinterType,
    val testType: CalibrationTestType,
    
    // Parameter und Ergebnisse als JSON
    val paramsJson: String? = null,          // z.B. {"startTemp": 190, "endTemp": 230, "step": 5}
    val resultJson: String? = null,          // z.B. {"bestTemp": 215, "notes": "..."}
    
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val notes: String? = null,
    val photoUrisJson: String? = null        // JSON Array von Foto-URIs
)

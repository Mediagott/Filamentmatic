package com.filamentmate.app.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Druckauftrag-Historie.
 */
@Entity(
    tableName = "print_jobs",
    foreignKeys = [
        ForeignKey(
            entity = PrinterConfigEntity::class,
            parentColumns = ["id"],
            childColumns = ["printerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SpoolEntity::class,
            parentColumns = ["id"],
            childColumns = ["spoolId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["printerId"]),
        Index(value = ["spoolId"]),
        Index(value = ["startedAt"])
    ]
)
data class PrintJobEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val printerId: Long,
    val jobName: String,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val slotGroup: String? = null,
    val slotIndex: Int? = null,
    val spoolId: Long? = null,
    val usedWeightG: Float = 0f,             // Verbrauchtes Gewicht in Gramm
    val rawUsedMm: Float? = null,            // Verbrauchte Länge in mm (falls verfügbar)
    val source: String = "MANUAL"            // "MANUAL", "AUTO", "CALIBRATION"
)

package com.filamentmate.app.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Verknüpfung zwischen Drucker-Slot und Spule.
 * z.B. AMS Slot 1 -> Spule X
 */
@Entity(
    tableName = "tray_links",
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
        Index(value = ["printerId", "slotGroup", "slotIndex"], unique = true),
        Index(value = ["spoolId"])
    ]
)
data class TrayLinkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val printerId: Long,
    val slotGroup: String,                   // z.B. "AMS", "External"
    val slotIndex: Int,                      // 0-3 für AMS
    val spoolId: Long? = null,               // Verknüpfte Spule
    val updatedAt: Long = System.currentTimeMillis()
)

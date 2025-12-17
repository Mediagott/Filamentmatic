package com.filamentmate.app.data.printer

/**
 * Status eines Druckers.
 */
data class PrinterStatus(
    val connected: Boolean = false,
    val activeSlotGroup: String? = null,      // z.B. "AMS"
    val activeSlotIndex: Int? = null,         // z.B. 0-3
    val jobName: String? = null,              // Aktueller Druckauftrag
    val jobState: JobState = JobState.IDLE,
    val jobProgress: Float = 0f,              // 0-100%
    val usedWeightG: Float? = null,           // Verbrauchtes Gewicht (wenn verfügbar)
    val usedLengthMm: Float? = null,          // Verbrauchte Länge (wenn verfügbar)
    val errorMessage: String? = null,
    
    // Temperaturen (Bambu Lab)
    val nozzleTempC: Float? = null,
    val bedTempC: Float? = null,
    val chamberTempC: Float? = null,
    
    // AMS Slot-Gewichte (Slot-Index -> Gewicht in Gramm)
    val slotRemainingWeightG: Map<Int, Float> = emptyMap()
)

/**
 * Zustand eines Druckauftrags.
 */
enum class JobState {
    IDLE,
    STARTING,     // Neu: Vorbereitung
    PREPARING,
    PRINTING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

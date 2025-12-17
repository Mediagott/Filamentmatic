package com.filamentmate.app.data.calibration

import com.filamentmate.app.data.database.entity.CalibrationTestType

/**
 * Parameter für Kalibrierungstests.
 */
sealed class CalibrationParams {
    
    /**
     * Parameter für Temperatur-Turm.
     */
    data class TempTower(
        val startTemp: Int = 230,
        val endTemp: Int = 190,
        val tempStep: Int = 5,
        val layerHeight: Float = 0.2f,
        val layersPerStep: Int = 25,      // 5mm pro Temperatur-Schritt
        val bedTemp: Int = 60,
        val printSpeed: Int = 60          // mm/s
    ) : CalibrationParams()
    
    /**
     * Parameter für Max-Flow-Test.
     */
    data class MaxFlow(
        val startFlow: Float = 5f,        // mm³/s
        val endFlow: Float = 30f,
        val flowStep: Float = 2.5f,
        val layerHeight: Float = 0.2f,
        val lineWidth: Float = 0.4f,
        val nozzleTemp: Int = 220,
        val bedTemp: Int = 60
    ) : CalibrationParams()
    
    /**
     * Parameter für Pressure Advance Test.
     */
    data class PressureAdvance(
        val startPA: Float = 0f,
        val endPA: Float = 0.1f,
        val paStep: Float = 0.005f,
        val layerHeight: Float = 0.2f,
        val printSpeed: Int = 100,        // mm/s für schnelle Linien
        val slowSpeed: Int = 20,          // mm/s für langsame Linien
        val nozzleTemp: Int = 220,
        val bedTemp: Int = 60
    ) : CalibrationParams()
    
    /**
     * Parameter für Flow-Ratio-Test (Würfel).
     */
    data class FlowRatio(
        val targetWallThickness: Float = 0.8f,  // 2x Linienbreite
        val cubeSize: Float = 20f,              // 20mm Würfel
        val lineWidth: Float = 0.4f,
        val layerHeight: Float = 0.2f,
        val nozzleTemp: Int = 220,
        val bedTemp: Int = 60
    ) : CalibrationParams()
    
    fun getTestType(): CalibrationTestType = when (this) {
        is TempTower -> CalibrationTestType.TEMP
        is MaxFlow -> CalibrationTestType.MAX_FLOW
        is PressureAdvance -> CalibrationTestType.PRESSURE_ADVANCE
        is FlowRatio -> CalibrationTestType.FLOW
    }
}

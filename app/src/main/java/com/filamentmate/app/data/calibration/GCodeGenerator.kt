package com.filamentmate.app.data.calibration

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI

/**
 * Generiert GCode für Kalibrierungstests.
 * Einfache Implementierung ohne externes Slicer-Tool.
 */
@Singleton
class GCodeGenerator @Inject constructor() {
    
    /**
     * Generiert GCode für einen Temperatur-Turm.
     */
    fun generateTempTower(params: CalibrationParams.TempTower): String {
        val sb = StringBuilder()
        
        // Header
        sb.appendLine("; FilamentMate Temperature Tower")
        sb.appendLine("; Start: ${params.startTemp}°C, End: ${params.endTemp}°C, Step: ${params.tempStep}°C")
        sb.appendLine()
        sb.appendLine(startGCode(params.startTemp, params.bedTemp))
        
        var currentTemp = params.startTemp
        var currentZ = params.layerHeight
        val tempDirection = if (params.endTemp < params.startTemp) -1 else 1
        
        while ((tempDirection < 0 && currentTemp >= params.endTemp) ||
               (tempDirection > 0 && currentTemp <= params.endTemp)) {
            
            // Temperatur setzen
            sb.appendLine("; Temperature: ${currentTemp}°C")
            sb.appendLine("M104 S$currentTemp ; set nozzle temp")
            
            // Schichten für diese Temperatur
            repeat(params.layersPerStep) {
                sb.appendLine(generateSquareLayer(
                    z = currentZ,
                    size = 20f,
                    lineWidth = 0.4f,
                    layerHeight = params.layerHeight,
                    speed = params.printSpeed,
                    centerX = 100f,
                    centerY = 100f
                ))
                currentZ += params.layerHeight
            }
            
            currentTemp += tempDirection * params.tempStep
        }
        
        sb.appendLine(endGCode())
        return sb.toString()
    }
    
    /**
     * Generiert GCode für Max-Flow-Test.
     */
    fun generateMaxFlowTest(params: CalibrationParams.MaxFlow): String {
        val sb = StringBuilder()
        
        sb.appendLine("; FilamentMate Max Flow Test")
        sb.appendLine("; Start: ${params.startFlow}mm³/s, End: ${params.endFlow}mm³/s")
        sb.appendLine()
        sb.appendLine(startGCode(params.nozzleTemp, params.bedTemp))
        
        var currentFlow = params.startFlow
        var yOffset = 10f
        
        while (currentFlow <= params.endFlow) {
            // Berechne Geschwindigkeit für gewünschten Flow
            // Flow = lineWidth * layerHeight * speed
            val speed = currentFlow / (params.lineWidth * params.layerHeight)
            
            sb.appendLine("; Flow: ${currentFlow}mm³/s (Speed: ${speed.toInt()}mm/s)")
            sb.appendLine("G1 Z${params.layerHeight} F300")
            sb.appendLine("G1 X10 Y$yOffset F6000")
            sb.appendLine("G1 X190 Y$yOffset F${(speed * 60).toInt()} E${calculateExtrusion(180f, params.lineWidth, params.layerHeight)}")
            
            yOffset += 5f
            currentFlow += params.flowStep
        }
        
        sb.appendLine(endGCode())
        return sb.toString()
    }
    
    /**
     * Generiert GCode für Pressure Advance Test (Linien-Muster).
     */
    fun generatePressureAdvanceTest(params: CalibrationParams.PressureAdvance): String {
        val sb = StringBuilder()
        
        sb.appendLine("; FilamentMate Pressure Advance Test")
        sb.appendLine("; Start: ${params.startPA}, End: ${params.endPA}, Step: ${params.paStep}")
        sb.appendLine()
        sb.appendLine(startGCode(params.nozzleTemp, params.bedTemp))
        
        var currentPA = params.startPA
        var yOffset = 10f
        
        while (currentPA <= params.endPA) {
            // PA-Wert setzen (Klipper Syntax)
            sb.appendLine("; PA: $currentPA")
            sb.appendLine("SET_PRESSURE_ADVANCE ADVANCE=$currentPA")
            
            // Langsam - Schnell - Langsam Muster
            sb.appendLine("G1 Z${params.layerHeight} F300")
            sb.appendLine("G1 X10 Y$yOffset F6000")
            
            // Langsam
            sb.appendLine("G1 X40 Y$yOffset F${params.slowSpeed * 60} E${calculateExtrusion(30f, 0.4f, params.layerHeight)}")
            // Schnell
            sb.appendLine("G1 X160 Y$yOffset F${params.printSpeed * 60} E${calculateExtrusion(120f, 0.4f, params.layerHeight)}")
            // Langsam
            sb.appendLine("G1 X190 Y$yOffset F${params.slowSpeed * 60} E${calculateExtrusion(30f, 0.4f, params.layerHeight)}")
            
            yOffset += 3f
            currentPA += params.paStep
        }
        
        // PA zurücksetzen
        sb.appendLine("SET_PRESSURE_ADVANCE ADVANCE=0")
        sb.appendLine(endGCode())
        return sb.toString()
    }
    
    /**
     * Generiert GCode für Flow-Ratio-Test (Würfel mit Perimetern).
     */
    fun generateFlowRatioTest(params: CalibrationParams.FlowRatio): String {
        val sb = StringBuilder()
        
        sb.appendLine("; FilamentMate Flow Ratio Test Cube")
        sb.appendLine("; Size: ${params.cubeSize}mm, Target Wall: ${params.targetWallThickness}mm")
        sb.appendLine()
        sb.appendLine(startGCode(params.nozzleTemp, params.bedTemp))
        
        val layers = (params.cubeSize / params.layerHeight).toInt()
        val centerX = 100f
        val centerY = 100f
        
        repeat(layers) { layer ->
            val z = (layer + 1) * params.layerHeight
            
            // 2 Perimeter für Wandstärke
            val outerSize = params.cubeSize
            val innerSize = params.cubeSize - 2 * params.lineWidth
            
            sb.appendLine("; Layer ${layer + 1}")
            sb.appendLine(generateSquarePerimeter(z, outerSize, params.lineWidth, params.layerHeight, 40, centerX, centerY))
            sb.appendLine(generateSquarePerimeter(z, innerSize, params.lineWidth, params.layerHeight, 40, centerX, centerY))
        }
        
        sb.appendLine(endGCode())
        return sb.toString()
    }
    
    private fun startGCode(nozzleTemp: Int, bedTemp: Int): String = """
        ; Start GCode
        G28 ; Home all axes
        M140 S$bedTemp ; Set bed temp
        M109 S$nozzleTemp ; Wait for nozzle temp
        M190 S$bedTemp ; Wait for bed temp
        G92 E0 ; Reset extruder
        G1 Z5 F3000 ; Lift nozzle
        ; Prime line
        G1 X5 Y5 F3000
        G1 Z0.3 F300
        G1 X5 Y100 E10 F1500
        G1 X5.4 Y100 F3000
        G1 X5.4 Y5 E20 F1500
        G92 E0 ; Reset extruder
        G1 Z2 F300
    """.trimIndent()
    
    private fun endGCode(): String = """
        ; End GCode
        G91 ; Relative positioning
        G1 E-2 F2700 ; Retract
        G1 Z10 F3000 ; Raise Z
        G90 ; Absolute positioning
        G1 X0 Y200 F3000 ; Present print
        M104 S0 ; Turn off nozzle
        M140 S0 ; Turn off bed
        M84 ; Disable motors
    """.trimIndent()
    
    private fun generateSquareLayer(
        z: Float, size: Float, lineWidth: Float, layerHeight: Float,
        speed: Int, centerX: Float, centerY: Float
    ): String {
        val halfSize = size / 2
        val e = calculateExtrusion(size, lineWidth, layerHeight)
        
        return """
            G1 Z$z F300
            G1 X${centerX - halfSize} Y${centerY - halfSize} F6000
            G1 X${centerX + halfSize} Y${centerY - halfSize} F${speed * 60} E$e
            G1 X${centerX + halfSize} Y${centerY + halfSize} F${speed * 60} E$e
            G1 X${centerX - halfSize} Y${centerY + halfSize} F${speed * 60} E$e
            G1 X${centerX - halfSize} Y${centerY - halfSize} F${speed * 60} E$e
        """.trimIndent()
    }
    
    private fun generateSquarePerimeter(
        z: Float, size: Float, lineWidth: Float, layerHeight: Float,
        speed: Int, centerX: Float, centerY: Float
    ): String {
        return generateSquareLayer(z, size, lineWidth, layerHeight, speed, centerX, centerY)
    }
    
    private fun calculateExtrusion(distance: Float, lineWidth: Float, layerHeight: Float): Float {
        // E = (lineWidth * layerHeight * distance) / (π * (filamentDiameter/2)²)
        val filamentDiameter = 1.75f
        val crossSection = lineWidth * layerHeight
        val filamentArea = PI.toFloat() * (filamentDiameter / 2f) * (filamentDiameter / 2f)
        return (crossSection * distance) / filamentArea
    }
}

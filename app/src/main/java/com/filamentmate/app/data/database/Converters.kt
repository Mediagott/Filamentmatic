package com.filamentmate.app.data.database

import androidx.room.TypeConverter
import com.filamentmate.app.data.database.entity.CalibrationTestType
import com.filamentmate.app.data.database.entity.PrinterType

/**
 * Room TypeConverters für Enums.
 */
class Converters {
    
    // PrinterType
    @TypeConverter
    fun fromPrinterType(value: PrinterType): String = value.name
    
    @TypeConverter
    fun toPrinterType(value: String): PrinterType = PrinterType.valueOf(value)
    
    // CalibrationTestType
    @TypeConverter
    fun fromCalibrationTestType(value: CalibrationTestType): String = value.name
    
    @TypeConverter
    fun toCalibrationTestType(value: String): CalibrationTestType = CalibrationTestType.valueOf(value)
}

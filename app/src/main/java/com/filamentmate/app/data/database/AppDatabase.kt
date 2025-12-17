package com.filamentmate.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.filamentmate.app.data.database.dao.CalibrationRunDao
import com.filamentmate.app.data.database.dao.FilamentProfileDao
import com.filamentmate.app.data.database.dao.PrintJobDao
import com.filamentmate.app.data.database.dao.PrinterConfigDao
import com.filamentmate.app.data.database.dao.SpoolDao
import com.filamentmate.app.data.database.dao.SpoolOverrideDao
import com.filamentmate.app.data.database.dao.TrayLinkDao
import com.filamentmate.app.data.database.entity.CalibrationRunEntity
import com.filamentmate.app.data.database.entity.FilamentProfileEntity
import com.filamentmate.app.data.database.entity.PrintJobEntity
import com.filamentmate.app.data.database.entity.PrinterConfigEntity
import com.filamentmate.app.data.database.entity.PrinterType
import com.filamentmate.app.data.database.entity.SpoolEntity
import com.filamentmate.app.data.database.entity.SpoolOverrideEntity
import com.filamentmate.app.data.database.entity.TrayLinkEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SpoolEntity::class,
        PrinterConfigEntity::class,
        TrayLinkEntity::class,
        PrintJobEntity::class,
        FilamentProfileEntity::class,
        SpoolOverrideEntity::class,
        CalibrationRunEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun spoolDao(): SpoolDao
    abstract fun printerConfigDao(): PrinterConfigDao
    abstract fun trayLinkDao(): TrayLinkDao
    abstract fun printJobDao(): PrintJobDao
    abstract fun filamentProfileDao(): FilamentProfileDao
    abstract fun spoolOverrideDao(): SpoolOverrideDao
    abstract fun calibrationRunDao(): CalibrationRunDao
    
    companion object {
        const val DATABASE_NAME = "filamentmate_db"
        
        /**
         * Callback für Datenbank-Seed beim ersten Start.
         */
        fun createCallback(scope: CoroutineScope): Callback {
            return object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    scope.launch(Dispatchers.IO) {
                        // Seed wird vom DatabaseSeeder durchgeführt
                    }
                }
            }
        }
    }
}

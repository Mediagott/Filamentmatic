package com.filamentmate.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.filamentmate.app.data.database.entity.PrinterConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrinterConfigDao {
    
    @Query("SELECT * FROM printer_config WHERE id = 1")
    fun getPrinterConfig(): Flow<PrinterConfigEntity?>
    
    @Query("SELECT * FROM printer_config WHERE id = 1")
    suspend fun getPrinterConfigOnce(): PrinterConfigEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: PrinterConfigEntity)
    
    @Update
    suspend fun update(config: PrinterConfigEntity)
    
    @Query("UPDATE printer_config SET mockModeEnabled = :enabled WHERE id = 1")
    suspend fun setMockMode(enabled: Boolean)
    
    @Query("UPDATE printer_config SET enabled = :enabled WHERE id = 1")
    suspend fun setEnabled(enabled: Boolean)
}

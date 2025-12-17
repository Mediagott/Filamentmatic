package com.filamentmate.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.filamentmate.app.data.database.entity.TrayLinkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrayLinkDao {
    
    @Query("SELECT * FROM tray_links WHERE printerId = :printerId")
    fun getTrayLinksForPrinter(printerId: Long): Flow<List<TrayLinkEntity>>
    
    @Query("""
        SELECT * FROM tray_links 
        WHERE printerId = :printerId 
        AND slotGroup = :slotGroup 
        AND slotIndex = :slotIndex
    """)
    fun getTrayLinkBySlot(
        printerId: Long,
        slotGroup: String,
        slotIndex: Int
    ): Flow<TrayLinkEntity?>
    
    @Query("""
        SELECT * FROM tray_links 
        WHERE printerId = :printerId 
        AND slotGroup = :slotGroup 
        AND slotIndex = :slotIndex
    """)
    suspend fun getTrayLinkBySlotOnce(
        printerId: Long,
        slotGroup: String,
        slotIndex: Int
    ): TrayLinkEntity?
    
    @Query("SELECT * FROM tray_links WHERE spoolId = :spoolId")
    fun getTrayLinkBySpoolId(spoolId: Long): Flow<TrayLinkEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(trayLink: TrayLinkEntity): Long
    
    @Query("DELETE FROM tray_links WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM tray_links WHERE spoolId = :spoolId")
    suspend fun deleteBySpoolId(spoolId: Long)
    
    @Query("""
        UPDATE tray_links 
        SET spoolId = :spoolId, updatedAt = :updatedAt 
        WHERE printerId = :printerId AND slotGroup = :slotGroup AND slotIndex = :slotIndex
    """)
    suspend fun updateSpoolForSlot(
        printerId: Long,
        slotGroup: String,
        slotIndex: Int,
        spoolId: Long?,
        updatedAt: Long = System.currentTimeMillis()
    )
}

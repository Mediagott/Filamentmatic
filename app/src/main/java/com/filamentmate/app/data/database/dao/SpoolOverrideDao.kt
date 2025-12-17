package com.filamentmate.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.filamentmate.app.data.database.entity.SpoolOverrideEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpoolOverrideDao {
    
    @Query("SELECT * FROM spool_overrides WHERE spoolId = :spoolId")
    fun getOverrideBySpoolId(spoolId: Long): Flow<SpoolOverrideEntity?>
    
    @Query("SELECT * FROM spool_overrides WHERE spoolId = :spoolId")
    suspend fun getOverrideBySpoolIdOnce(spoolId: Long): SpoolOverrideEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(override: SpoolOverrideEntity): Long
    
    @Update
    suspend fun update(override: SpoolOverrideEntity)
    
    @Query("DELETE FROM spool_overrides WHERE spoolId = :spoolId")
    suspend fun deleteBySpoolId(spoolId: Long)
    
    @Query("DELETE FROM spool_overrides WHERE id = :id")
    suspend fun deleteById(id: Long)
}

package com.filamentmate.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.filamentmate.app.data.database.entity.CalibrationRunEntity
import com.filamentmate.app.data.database.entity.CalibrationTestType
import kotlinx.coroutines.flow.Flow

@Dao
interface CalibrationRunDao {
    
    @Query("SELECT * FROM calibration_runs WHERE filamentProfileId = :profileId ORDER BY startedAt DESC")
    fun getRunsByProfileId(profileId: Long): Flow<List<CalibrationRunEntity>>
    
    @Query("SELECT * FROM calibration_runs WHERE spoolId = :spoolId ORDER BY startedAt DESC")
    fun getRunsBySpoolId(spoolId: Long): Flow<List<CalibrationRunEntity>>
    
    @Query("""
        SELECT * FROM calibration_runs 
        WHERE filamentProfileId = :profileId 
        AND testType = :testType 
        ORDER BY startedAt DESC 
        LIMIT 1
    """)
    fun getLatestRunByType(
        profileId: Long,
        testType: CalibrationTestType
    ): Flow<CalibrationRunEntity?>
    
    @Query("SELECT * FROM calibration_runs WHERE id = :id")
    suspend fun getRunById(id: Long): CalibrationRunEntity?
    
    @Query("SELECT * FROM calibration_runs ORDER BY startedAt DESC LIMIT 50")
    fun getLatestRuns(): Flow<List<CalibrationRunEntity>>
    
    @Insert
    suspend fun insert(run: CalibrationRunEntity): Long
    
    @Update
    suspend fun update(run: CalibrationRunEntity)
    
    @Query("UPDATE calibration_runs SET resultJson = :resultJson, endedAt = :endedAt WHERE id = :id")
    suspend fun updateResult(
        id: Long,
        resultJson: String?,
        endedAt: Long = System.currentTimeMillis()
    )
    
    @Query("UPDATE calibration_runs SET notes = :notes, photoUrisJson = :photoUrisJson WHERE id = :id")
    suspend fun updateNotesAndPhotos(
        id: Long,
        notes: String?,
        photoUrisJson: String?
    )
    
    @Query("DELETE FROM calibration_runs WHERE id = :id")
    suspend fun deleteById(id: Long)
}

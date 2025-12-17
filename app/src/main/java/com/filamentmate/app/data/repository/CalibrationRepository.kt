package com.filamentmate.app.data.repository

import com.filamentmate.app.data.database.dao.CalibrationRunDao
import com.filamentmate.app.data.database.entity.CalibrationRunEntity
import com.filamentmate.app.data.database.entity.CalibrationTestType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalibrationRepository @Inject constructor(
    private val calibrationRunDao: CalibrationRunDao
) {
    
    fun getRunsByProfileId(profileId: Long): Flow<List<CalibrationRunEntity>> =
        calibrationRunDao.getRunsByProfileId(profileId)
    
    fun getRunsBySpoolId(spoolId: Long): Flow<List<CalibrationRunEntity>> =
        calibrationRunDao.getRunsBySpoolId(spoolId)
    
    fun getLatestRunByType(profileId: Long, testType: CalibrationTestType): Flow<CalibrationRunEntity?> =
        calibrationRunDao.getLatestRunByType(profileId, testType)
    
    suspend fun getRunById(id: Long): CalibrationRunEntity? = calibrationRunDao.getRunById(id)
    
    fun getLatestRuns(): Flow<List<CalibrationRunEntity>> = calibrationRunDao.getLatestRuns()
    
    suspend fun insertRun(run: CalibrationRunEntity): Long = calibrationRunDao.insert(run)
    
    suspend fun updateRun(run: CalibrationRunEntity) = calibrationRunDao.update(run)
    
    suspend fun updateResult(id: Long, resultJson: String?) =
        calibrationRunDao.updateResult(id, resultJson)
    
    suspend fun updateNotesAndPhotos(id: Long, notes: String?, photoUrisJson: String?) =
        calibrationRunDao.updateNotesAndPhotos(id, notes, photoUrisJson)
    
    suspend fun deleteRun(id: Long) = calibrationRunDao.deleteById(id)
}

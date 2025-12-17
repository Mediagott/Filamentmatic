package com.filamentmate.app.data.repository

import com.filamentmate.app.data.database.dao.SpoolDao
import com.filamentmate.app.data.database.entity.SpoolEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpoolRepository @Inject constructor(
    private val spoolDao: SpoolDao
) {
    
    fun getAllSpools(): Flow<List<SpoolEntity>> = spoolDao.getAllSpools()
    
    fun getSpoolById(id: Long): Flow<SpoolEntity?> = spoolDao.getSpoolById(id)
    
    suspend fun getSpoolByIdOnce(id: Long): SpoolEntity? = spoolDao.getSpoolByIdOnce(id)
    
    fun getSpoolsFiltered(
        material: String? = null,
        brand: String? = null,
        color: String? = null
    ): Flow<List<SpoolEntity>> = spoolDao.getSpoolsFiltered(material, brand, color)
    
    fun getAllMaterials(): Flow<List<String>> = spoolDao.getAllMaterials()
    
    fun getAllBrands(): Flow<List<String>> = spoolDao.getAllBrands()
    
    fun getAllColors(): Flow<List<String>> = spoolDao.getAllColors()
    
    suspend fun insertSpool(spool: SpoolEntity): Long = spoolDao.insert(spool)
    
    suspend fun updateSpool(spool: SpoolEntity) = spoolDao.update(spool)
    
    suspend fun deleteSpool(spool: SpoolEntity) = spoolDao.delete(spool)
    
    suspend fun deleteSpoolById(id: Long) = spoolDao.deleteById(id)
    
    suspend fun updateRemainingWeight(id: Long, remainingWeightG: Float) = 
        spoolDao.updateRemainingWeight(id, remainingWeightG)
    
    suspend fun decrementRemainingWeight(id: Long, usedWeightG: Float) = 
        spoolDao.decrementRemainingWeight(id, usedWeightG)
}

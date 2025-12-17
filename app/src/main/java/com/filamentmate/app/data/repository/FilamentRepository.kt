package com.filamentmate.app.data.repository

import com.filamentmate.app.data.database.dao.FilamentProfileDao
import com.filamentmate.app.data.database.dao.SpoolOverrideDao
import com.filamentmate.app.data.database.entity.FilamentProfileEntity
import com.filamentmate.app.data.database.entity.SpoolOverrideEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilamentRepository @Inject constructor(
    private val profileDao: FilamentProfileDao,
    private val overrideDao: SpoolOverrideDao
) {
    
    // Profiles
    fun getAllProfiles(): Flow<List<FilamentProfileEntity>> = profileDao.getAllProfiles()
    
    fun getProfileById(id: Long): Flow<FilamentProfileEntity?> = profileDao.getProfileById(id)
    
    suspend fun getProfileByIdOnce(id: Long): FilamentProfileEntity? = profileDao.getProfileByIdOnce(id)
    
    fun findProfile(material: String, brand: String, color: String? = null): Flow<FilamentProfileEntity?> =
        profileDao.findProfile(material, brand, color)
    
    suspend fun findProfileOnce(material: String, brand: String, color: String? = null): FilamentProfileEntity? =
        profileDao.findProfileOnce(material, brand, color)
    
    fun getProfilesByMaterial(material: String): Flow<List<FilamentProfileEntity>> =
        profileDao.getProfilesByMaterial(material)
    
    fun getAllMaterials(): Flow<List<String>> = profileDao.getAllMaterials()
    
    suspend fun insertProfile(profile: FilamentProfileEntity): Long = profileDao.insert(profile)
    
    suspend fun updateProfile(profile: FilamentProfileEntity) = profileDao.update(profile)
    
    suspend fun deleteProfile(profile: FilamentProfileEntity) = profileDao.delete(profile)
    
    suspend fun deleteProfileById(id: Long) = profileDao.deleteById(id)
    
    // Overrides
    fun getOverrideBySpoolId(spoolId: Long): Flow<SpoolOverrideEntity?> =
        overrideDao.getOverrideBySpoolId(spoolId)
    
    suspend fun getOverrideBySpoolIdOnce(spoolId: Long): SpoolOverrideEntity? =
        overrideDao.getOverrideBySpoolIdOnce(spoolId)
    
    suspend fun upsertOverride(override: SpoolOverrideEntity): Long =
        overrideDao.upsert(override)
    
    suspend fun deleteOverrideBySpoolId(spoolId: Long) = overrideDao.deleteBySpoolId(spoolId)
}

package com.filamentmate.app.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.filamentmate.app.data.database.entity.FilamentProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FilamentProfileDao {
    
    @Query("SELECT * FROM filament_profiles ORDER BY material, brand, color")
    fun getAllProfiles(): Flow<List<FilamentProfileEntity>>
    
    @Query("SELECT * FROM filament_profiles WHERE id = :id")
    fun getProfileById(id: Long): Flow<FilamentProfileEntity?>
    
    @Query("SELECT * FROM filament_profiles WHERE id = :id")
    suspend fun getProfileByIdOnce(id: Long): FilamentProfileEntity?
    
    @Query("""
        SELECT * FROM filament_profiles 
        WHERE material = :material 
        AND brand = :brand 
        AND (color = :color OR (:color IS NULL AND color IS NULL))
    """)
    fun findProfile(
        material: String,
        brand: String,
        color: String? = null
    ): Flow<FilamentProfileEntity?>
    
    @Query("""
        SELECT * FROM filament_profiles 
        WHERE material = :material 
        AND brand = :brand 
        AND (color = :color OR (:color IS NULL AND color IS NULL))
    """)
    suspend fun findProfileOnce(
        material: String,
        brand: String,
        color: String? = null
    ): FilamentProfileEntity?
    
    @Query("SELECT * FROM filament_profiles WHERE material = :material ORDER BY brand")
    fun getProfilesByMaterial(material: String): Flow<List<FilamentProfileEntity>>
    
    @Query("SELECT DISTINCT material FROM filament_profiles ORDER BY material")
    fun getAllMaterials(): Flow<List<String>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: FilamentProfileEntity): Long
    
    @Update
    suspend fun update(profile: FilamentProfileEntity)
    
    @Delete
    suspend fun delete(profile: FilamentProfileEntity)
    
    @Query("DELETE FROM filament_profiles WHERE id = :id")
    suspend fun deleteById(id: Long)
}

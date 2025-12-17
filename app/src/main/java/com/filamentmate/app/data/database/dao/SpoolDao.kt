package com.filamentmate.app.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.filamentmate.app.data.database.entity.SpoolEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpoolDao {
    
    @Query("SELECT * FROM spools ORDER BY createdAt DESC")
    fun getAllSpools(): Flow<List<SpoolEntity>>
    
    @Query("SELECT * FROM spools WHERE id = :id")
    fun getSpoolById(id: Long): Flow<SpoolEntity?>
    
    @Query("SELECT * FROM spools WHERE id = :id")
    suspend fun getSpoolByIdOnce(id: Long): SpoolEntity?
    
    @Query("SELECT * FROM spools WHERE material = :material ORDER BY createdAt DESC")
    fun getSpoolsByMaterial(material: String): Flow<List<SpoolEntity>>
    
    @Query("SELECT * FROM spools WHERE brand = :brand ORDER BY createdAt DESC")
    fun getSpoolsByBrand(brand: String): Flow<List<SpoolEntity>>
    
    @Query("SELECT * FROM spools WHERE color = :color ORDER BY createdAt DESC")
    fun getSpoolsByColor(color: String): Flow<List<SpoolEntity>>
    
    @Query("""
        SELECT * FROM spools 
        WHERE (:material IS NULL OR material = :material)
        AND (:brand IS NULL OR brand = :brand)
        AND (:color IS NULL OR color = :color)
        ORDER BY createdAt DESC
    """)
    fun getSpoolsFiltered(
        material: String? = null,
        brand: String? = null,
        color: String? = null
    ): Flow<List<SpoolEntity>>
    
    @Query("SELECT DISTINCT material FROM spools ORDER BY material")
    fun getAllMaterials(): Flow<List<String>>
    
    @Query("SELECT DISTINCT brand FROM spools ORDER BY brand")
    fun getAllBrands(): Flow<List<String>>
    
    @Query("SELECT DISTINCT color FROM spools ORDER BY color")
    fun getAllColors(): Flow<List<String>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(spool: SpoolEntity): Long
    
    @Update
    suspend fun update(spool: SpoolEntity)
    
    @Delete
    suspend fun delete(spool: SpoolEntity)
    
    @Query("DELETE FROM spools WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("UPDATE spools SET remainingWeightG = :remainingWeightG WHERE id = :id")
    suspend fun updateRemainingWeight(id: Long, remainingWeightG: Float)
    
    @Query("UPDATE spools SET remainingWeightG = MAX(0, remainingWeightG - :usedWeightG) WHERE id = :id")
    suspend fun decrementRemainingWeight(id: Long, usedWeightG: Float)
}

package com.filamentmate.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.filamentmate.app.data.database.entity.PrintJobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrintJobDao {
    
    @Query("SELECT * FROM print_jobs ORDER BY startedAt DESC LIMIT 100")
    fun getLatestPrintJobs(): Flow<List<PrintJobEntity>>
    
    @Query("SELECT * FROM print_jobs WHERE spoolId = :spoolId ORDER BY startedAt DESC")
    fun getPrintJobsBySpoolId(spoolId: Long): Flow<List<PrintJobEntity>>
    
    @Query("SELECT * FROM print_jobs WHERE printerId = :printerId ORDER BY startedAt DESC LIMIT 100")
    fun getPrintJobsByPrinterId(printerId: Long): Flow<List<PrintJobEntity>>
    
    @Query("SELECT * FROM print_jobs WHERE id = :id")
    suspend fun getPrintJobById(id: Long): PrintJobEntity?
    
    @Insert
    suspend fun insert(printJob: PrintJobEntity): Long
    
    @Query("UPDATE print_jobs SET endedAt = :endedAt WHERE id = :id")
    suspend fun markCompleted(id: Long, endedAt: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM print_jobs WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("SELECT SUM(usedWeightG) FROM print_jobs WHERE spoolId = :spoolId")
    suspend fun getTotalUsedWeightForSpool(spoolId: Long): Float?
}

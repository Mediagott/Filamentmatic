package com.filamentmate.app.data.repository

import com.filamentmate.app.data.database.dao.PrintJobDao
import com.filamentmate.app.data.database.entity.PrintJobEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrintJobRepository @Inject constructor(
    private val printJobDao: PrintJobDao
) {
    
    fun getLatestPrintJobs(): Flow<List<PrintJobEntity>> = printJobDao.getLatestPrintJobs()
    
    fun getPrintJobsBySpoolId(spoolId: Long): Flow<List<PrintJobEntity>> =
        printJobDao.getPrintJobsBySpoolId(spoolId)
    
    fun getPrintJobsByPrinterId(printerId: Long): Flow<List<PrintJobEntity>> =
        printJobDao.getPrintJobsByPrinterId(printerId)
    
    suspend fun getPrintJobById(id: Long): PrintJobEntity? = printJobDao.getPrintJobById(id)
    
    suspend fun insertPrintJob(printJob: PrintJobEntity): Long = printJobDao.insert(printJob)
    
    suspend fun markCompleted(id: Long) = printJobDao.markCompleted(id)
    
    suspend fun deletePrintJob(id: Long) = printJobDao.deleteById(id)
    
    suspend fun getTotalUsedWeightForSpool(spoolId: Long): Float =
        printJobDao.getTotalUsedWeightForSpool(spoolId) ?: 0f
}

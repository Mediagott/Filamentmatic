package com.filamentmate.app.data.repository

import com.filamentmate.app.data.database.dao.PrinterConfigDao
import com.filamentmate.app.data.database.dao.TrayLinkDao
import com.filamentmate.app.data.database.entity.PrinterConfigEntity
import com.filamentmate.app.data.database.entity.TrayLinkEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrinterRepository @Inject constructor(
    private val printerConfigDao: PrinterConfigDao,
    private val trayLinkDao: TrayLinkDao
) {
    
    // Printer Config
    fun getPrinterConfig(): Flow<PrinterConfigEntity?> = printerConfigDao.getPrinterConfig()
    
    suspend fun getPrinterConfigOnce(): PrinterConfigEntity? = printerConfigDao.getPrinterConfigOnce()
    
    suspend fun savePrinterConfig(config: PrinterConfigEntity) = printerConfigDao.insert(config)
    
    suspend fun updatePrinterConfig(config: PrinterConfigEntity) = printerConfigDao.update(config)
    
    suspend fun setMockMode(enabled: Boolean) = printerConfigDao.setMockMode(enabled)
    
    suspend fun setEnabled(enabled: Boolean) = printerConfigDao.setEnabled(enabled)
    
    // Tray Links
    fun getTrayLinksForPrinter(printerId: Long): Flow<List<TrayLinkEntity>> =
        trayLinkDao.getTrayLinksForPrinter(printerId)
    
    fun getTrayLinkBySlot(printerId: Long, slotGroup: String, slotIndex: Int): Flow<TrayLinkEntity?> =
        trayLinkDao.getTrayLinkBySlot(printerId, slotGroup, slotIndex)
    
    suspend fun getTrayLinkBySlotOnce(printerId: Long, slotGroup: String, slotIndex: Int): TrayLinkEntity? =
        trayLinkDao.getTrayLinkBySlotOnce(printerId, slotGroup, slotIndex)
    
    fun getTrayLinkBySpoolId(spoolId: Long): Flow<TrayLinkEntity?> =
        trayLinkDao.getTrayLinkBySpoolId(spoolId)
    
    suspend fun linkSpoolToSlot(trayLink: TrayLinkEntity): Long = trayLinkDao.upsert(trayLink)
    
    suspend fun updateSpoolForSlot(
        printerId: Long,
        slotGroup: String,
        slotIndex: Int,
        spoolId: Long?
    ) = trayLinkDao.updateSpoolForSlot(printerId, slotGroup, slotIndex, spoolId)
    
    suspend fun unlinkSpool(spoolId: Long) = trayLinkDao.deleteBySpoolId(spoolId)
}

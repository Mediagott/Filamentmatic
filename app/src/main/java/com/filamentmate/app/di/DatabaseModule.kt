package com.filamentmate.app.di

import android.content.Context
import androidx.room.Room
import com.filamentmate.app.data.database.AppDatabase
import com.filamentmate.app.data.database.dao.CalibrationRunDao
import com.filamentmate.app.data.database.dao.FilamentProfileDao
import com.filamentmate.app.data.database.dao.PrintJobDao
import com.filamentmate.app.data.database.dao.PrinterConfigDao
import com.filamentmate.app.data.database.dao.SpoolDao
import com.filamentmate.app.data.database.dao.SpoolOverrideDao
import com.filamentmate.app.data.database.dao.TrayLinkDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideSpoolDao(database: AppDatabase): SpoolDao = database.spoolDao()
    
    @Provides
    fun providePrinterConfigDao(database: AppDatabase): PrinterConfigDao = database.printerConfigDao()
    
    @Provides
    fun provideTrayLinkDao(database: AppDatabase): TrayLinkDao = database.trayLinkDao()
    
    @Provides
    fun providePrintJobDao(database: AppDatabase): PrintJobDao = database.printJobDao()
    
    @Provides
    fun provideFilamentProfileDao(database: AppDatabase): FilamentProfileDao = database.filamentProfileDao()
    
    @Provides
    fun provideSpoolOverrideDao(database: AppDatabase): SpoolOverrideDao = database.spoolOverrideDao()
    
    @Provides
    fun provideCalibrationRunDao(database: AppDatabase): CalibrationRunDao = database.calibrationRunDao()
}

package com.filamentmate.app.di

import com.filamentmate.app.data.printer.PrinterConnectionManager
import com.filamentmate.app.data.printer.PrinterProviderFactory
import com.filamentmate.app.data.printer.provider.BambuPrinterProvider
import com.filamentmate.app.data.printer.provider.ManualPrinterProvider
import com.filamentmate.app.data.printer.provider.MockPrinterProvider
import com.filamentmate.app.data.printer.provider.MoonrakerPrinterProvider
import com.filamentmate.app.data.printer.provider.OctoPrintProvider
import com.filamentmate.app.data.printer.provider.PrusaLinkPrinterProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PrinterModule {
    
    @Provides
    @Singleton
    fun provideMockPrinterProvider(): MockPrinterProvider = MockPrinterProvider()
    
    @Provides
    @Singleton
    fun provideManualPrinterProvider(): ManualPrinterProvider = ManualPrinterProvider()
    
    @Provides
    @Singleton
    fun provideBambuPrinterProvider(): BambuPrinterProvider = BambuPrinterProvider()
    
    @Provides
    @Singleton
    fun provideOctoPrintProvider(): OctoPrintProvider = OctoPrintProvider()
    
    @Provides
    @Singleton
    fun provideMoonrakerPrinterProvider(): MoonrakerPrinterProvider = MoonrakerPrinterProvider()
    
    @Provides
    @Singleton
    fun providePrusaLinkPrinterProvider(): PrusaLinkPrinterProvider = PrusaLinkPrinterProvider()
}

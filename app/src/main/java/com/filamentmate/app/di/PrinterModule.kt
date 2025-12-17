package com.filamentmate.app.di

import com.filamentmate.app.data.printer.PrinterConnectionManager
import com.filamentmate.app.data.printer.PrinterProviderFactory
import com.filamentmate.app.data.printer.bambu.BambuMqttClient
import com.filamentmate.app.data.printer.provider.BambuPrinterProvider
import com.filamentmate.app.data.printer.provider.ManualPrinterProvider
import com.filamentmate.app.data.printer.provider.MockPrinterProvider
import com.filamentmate.app.data.printer.provider.MoonrakerPrinterProvider
import com.filamentmate.app.data.printer.provider.OctoPrintProvider
import com.filamentmate.app.data.printer.provider.PrusaLinkPrinterProvider
import com.google.gson.Gson
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
    fun provideGson(): Gson = Gson()
    
    @Provides
    @Singleton
    fun provideMockPrinterProvider(): MockPrinterProvider = MockPrinterProvider()
    
    @Provides
    @Singleton
    fun provideManualPrinterProvider(): ManualPrinterProvider = ManualPrinterProvider()
    
    @Provides
    @Singleton
    fun provideBambuMqttClient(gson: Gson): BambuMqttClient = BambuMqttClient(gson)
    
    @Provides
    @Singleton
    fun provideBambuPrinterProvider(mqttClient: BambuMqttClient): BambuPrinterProvider = 
        BambuPrinterProvider(mqttClient)
    
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

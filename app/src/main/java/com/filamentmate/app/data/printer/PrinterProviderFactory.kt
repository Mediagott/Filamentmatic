package com.filamentmate.app.data.printer

import com.filamentmate.app.data.database.entity.PrinterType
import com.filamentmate.app.data.printer.provider.BambuPrinterProvider
import com.filamentmate.app.data.printer.provider.ManualPrinterProvider
import com.filamentmate.app.data.printer.provider.MockPrinterProvider
import com.filamentmate.app.data.printer.provider.MoonrakerPrinterProvider
import com.filamentmate.app.data.printer.provider.OctoPrintProvider
import com.filamentmate.app.data.printer.provider.PrusaLinkPrinterProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Factory für Drucker-Provider.
 * Erstellt den passenden Provider basierend auf dem PrinterType.
 */
@Singleton
class PrinterProviderFactory @Inject constructor(
    private val mockProvider: MockPrinterProvider,
    private val manualProvider: ManualPrinterProvider,
    private val bambuProvider: BambuPrinterProvider,
    private val octoPrintProvider: OctoPrintProvider,
    private val moonrakerProvider: MoonrakerPrinterProvider,
    private val prusaLinkProvider: PrusaLinkPrinterProvider
) {
    
    /**
     * Gibt den Provider für den angegebenen Drucker-Typ zurück.
     */
    fun getProvider(type: PrinterType): PrinterProvider {
        return when (type) {
            PrinterType.MOCK -> mockProvider
            PrinterType.MANUAL -> manualProvider
            PrinterType.BAMBU -> bambuProvider
            PrinterType.OCTOPRINT -> octoPrintProvider
            PrinterType.MOONRAKER -> moonrakerProvider
            PrinterType.PRUSALINK -> prusaLinkProvider
        }
    }
    
    /**
     * Gibt alle verfügbaren Provider-Typen zurück.
     */
    fun getAvailableTypes(): List<PrinterType> = PrinterType.entries
    
    /**
     * Prüft, ob ein Provider-Typ implementiert ist (nicht nur Stub).
     */
    fun isImplemented(type: PrinterType): Boolean {
        return when (type) {
            PrinterType.MOCK -> true
            PrinterType.MANUAL -> true
            PrinterType.BAMBU -> true  // Jetzt implementiert!
            PrinterType.OCTOPRINT -> false
            PrinterType.MOONRAKER -> false
            PrinterType.PRUSALINK -> false
        }
    }
    
    /**
     * Gibt eine Beschreibung für den Drucker-Typ zurück.
     */
    fun getTypeDescription(type: PrinterType): String {
        return when (type) {
            PrinterType.MOCK -> "Simulierter Drucker (für Tests)"
            PrinterType.MANUAL -> "Manuelle Eingabe (kein Netzwerk)"
            PrinterType.BAMBU -> "Bambu Lab (X1, P1, A1 Serie)"
            PrinterType.OCTOPRINT -> "OctoPrint (REST API)"
            PrinterType.MOONRAKER -> "Moonraker/Klipper (REST/WS)"
            PrinterType.PRUSALINK -> "PrusaLink (MK3S+, MK4, XL)"
        }
    }
}

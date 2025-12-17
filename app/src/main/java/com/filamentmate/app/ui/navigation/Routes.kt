package com.filamentmate.app.ui.navigation

/**
 * Navigation routes for FilamentMate app.
 */
object Routes {
    const val DASHBOARD = "dashboard"
    const val SPOOLS = "spools"
    const val SPOOL_DETAIL = "spool_detail/{spoolId}"
    const val SPOOL_ADD = "spool_add"
    const val FILAMENT_PROFILES = "filament_profiles"
    const val FILAMENT_PROFILE_DETAIL = "filament_profile_detail/{profileId}"
    const val FILAMENT_PROFILE_ADD = "filament_profile_add"
    const val PRINTER_SETUP = "printer_setup"
    const val PRINT_HISTORY = "print_history"
    
    fun spoolDetail(spoolId: Long) = "spool_detail/$spoolId"
    fun filamentProfileDetail(profileId: Long) = "filament_profile_detail/$profileId"
}

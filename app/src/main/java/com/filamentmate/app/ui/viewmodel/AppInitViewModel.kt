package com.filamentmate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filamentmate.app.data.database.DatabaseSeeder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel für die App-Initialisierung.
 * Führt den Database Seed beim ersten Start durch.
 */
@HiltViewModel
class AppInitViewModel @Inject constructor(
    private val databaseSeeder: DatabaseSeeder
) : ViewModel() {
    
    init {
        viewModelScope.launch {
            databaseSeeder.seedIfEmpty()
        }
    }
}

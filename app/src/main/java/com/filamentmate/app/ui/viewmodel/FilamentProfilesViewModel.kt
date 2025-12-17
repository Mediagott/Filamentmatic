package com.filamentmate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filamentmate.app.data.database.entity.FilamentProfileEntity
import com.filamentmate.app.data.repository.FilamentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FilamentProfilesUiState(
    val profiles: List<FilamentProfileEntity> = emptyList(),
    val availableMaterials: List<String> = emptyList(),
    val selectedMaterial: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class FilamentProfilesViewModel @Inject constructor(
    private val filamentRepository: FilamentRepository
) : ViewModel() {
    
    private val _selectedMaterial = MutableStateFlow<String?>(null)
    private val _error = MutableStateFlow<String?>(null)
    
    val uiState: StateFlow<FilamentProfilesUiState> = combine(
        filamentRepository.getAllProfiles(),
        filamentRepository.getAllMaterials(),
        _selectedMaterial,
        _error
    ) { profiles, materials, selectedMaterial, error ->
        val filteredProfiles = if (selectedMaterial != null) {
            profiles.filter { it.material == selectedMaterial }
        } else {
            profiles
        }
        
        FilamentProfilesUiState(
            profiles = filteredProfiles,
            availableMaterials = materials,
            selectedMaterial = selectedMaterial,
            isLoading = false,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FilamentProfilesUiState()
    )
    
    fun setMaterialFilter(material: String?) {
        _selectedMaterial.value = material
    }
    
    fun deleteProfile(profileId: Long) {
        viewModelScope.launch {
            try {
                filamentRepository.deleteProfileById(profileId)
            } catch (e: Exception) {
                _error.value = "Fehler beim Löschen: ${e.message}"
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}

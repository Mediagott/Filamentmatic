package com.filamentmate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filamentmate.app.data.database.entity.SpoolEntity
import com.filamentmate.app.data.repository.SpoolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SpoolsUiState(
    val spools: List<SpoolEntity> = emptyList(),
    val availableMaterials: List<String> = emptyList(),
    val availableBrands: List<String> = emptyList(),
    val availableColors: List<String> = emptyList(),
    val selectedMaterial: String? = null,
    val selectedBrand: String? = null,
    val selectedColor: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class SpoolsViewModel @Inject constructor(
    private val spoolRepository: SpoolRepository
) : ViewModel() {
    
    private val _selectedMaterial = MutableStateFlow<String?>(null)
    private val _selectedBrand = MutableStateFlow<String?>(null)
    private val _selectedColor = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)
    
    val uiState: StateFlow<SpoolsUiState> = combine(
        spoolRepository.getSpoolsFiltered(null, null, null),
        spoolRepository.getAllMaterials(),
        spoolRepository.getAllBrands(),
        spoolRepository.getAllColors(),
        _selectedMaterial,
        _selectedBrand,
        _selectedColor
    ) { values ->
        val allSpools = values[0] as List<SpoolEntity>
        val materials = values[1] as List<String>
        val brands = values[2] as List<String>
        val colors = values[3] as List<String>
        val selMaterial = values[4] as String?
        val selBrand = values[5] as String?
        val selColor = values[6] as String?
        
        // Filter anwenden
        val filteredSpools = allSpools.filter { spool ->
            (selMaterial == null || spool.material == selMaterial) &&
            (selBrand == null || spool.brand == selBrand) &&
            (selColor == null || spool.color == selColor)
        }
        
        SpoolsUiState(
            spools = filteredSpools,
            availableMaterials = materials,
            availableBrands = brands,
            availableColors = colors,
            selectedMaterial = selMaterial,
            selectedBrand = selBrand,
            selectedColor = selColor,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SpoolsUiState()
    )
    
    fun setMaterialFilter(material: String?) {
        _selectedMaterial.value = material
    }
    
    fun setBrandFilter(brand: String?) {
        _selectedBrand.value = brand
    }
    
    fun setColorFilter(color: String?) {
        _selectedColor.value = color
    }
    
    fun clearFilters() {
        _selectedMaterial.value = null
        _selectedBrand.value = null
        _selectedColor.value = null
    }
    
    fun deleteSpool(spoolId: Long) {
        viewModelScope.launch {
            try {
                spoolRepository.deleteSpoolById(spoolId)
            } catch (e: Exception) {
                _error.value = "Fehler beim Löschen: ${e.message}"
            }
        }
    }
}

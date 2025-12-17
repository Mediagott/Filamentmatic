package com.filamentmate.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filamentmate.app.data.database.entity.CalibrationRunEntity
import com.filamentmate.app.data.database.entity.FilamentProfileEntity
import com.filamentmate.app.data.repository.CalibrationRepository
import com.filamentmate.app.data.repository.FilamentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FilamentProfileDetailUiState(
    val profile: FilamentProfileEntity? = null,
    val calibrationRuns: List<CalibrationRunEntity> = emptyList(),
    val isNewProfile: Boolean = true,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FilamentProfileDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val filamentRepository: FilamentRepository,
    private val calibrationRepository: CalibrationRepository
) : ViewModel() {
    
    private val profileId: Long = savedStateHandle.get<Long>("profileId") ?: -1L
    private val isNewProfile = profileId == -1L
    
    private val _editableProfile = MutableStateFlow(
        FilamentProfileEntity(
            material = "PLA",
            brand = "",
            diameterMm = 1.75f
        )
    )
    val editableProfile: StateFlow<FilamentProfileEntity> = _editableProfile.asStateFlow()
    
    private val _isSaving = MutableStateFlow(false)
    private val _saveSuccess = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    
    val uiState: StateFlow<FilamentProfileDetailUiState> = if (isNewProfile) {
        combine(
            _editableProfile,
            _isSaving,
            _saveSuccess,
            _error
        ) { profile, isSaving, saveSuccess, error ->
            FilamentProfileDetailUiState(
                profile = profile,
                isNewProfile = true,
                isLoading = false,
                isSaving = isSaving,
                saveSuccess = saveSuccess,
                error = error
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FilamentProfileDetailUiState(isNewProfile = true, isLoading = false)
        )
    } else {
        combine(
            filamentRepository.getProfileById(profileId),
            calibrationRepository.getRunsByProfileId(profileId),
            _isSaving,
            _saveSuccess,
            _error
        ) { profile, calibrationRuns, isSaving, saveSuccess, error ->
            if (profile != null && _editableProfile.value.id == 0L) {
                _editableProfile.value = profile
            }
            
            FilamentProfileDetailUiState(
                profile = profile,
                calibrationRuns = calibrationRuns,
                isNewProfile = false,
                isLoading = profile == null,
                isSaving = isSaving,
                saveSuccess = saveSuccess,
                error = error
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FilamentProfileDetailUiState(isNewProfile = false)
        )
    }
    
    fun updateProfile(profile: FilamentProfileEntity) {
        _editableProfile.value = profile
    }
    
    fun saveProfile() {
        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null
            
            try {
                val profile = _editableProfile.value.copy(
                    updatedAt = System.currentTimeMillis()
                )
                if (isNewProfile) {
                    filamentRepository.insertProfile(profile)
                } else {
                    filamentRepository.updateProfile(profile)
                }
                _saveSuccess.value = true
            } catch (e: Exception) {
                _error.value = "Fehler beim Speichern: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }
}

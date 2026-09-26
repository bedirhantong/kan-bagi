package com.ribuufing.bloodapp.feature.logintype.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ribuufing.bloodapp.feature.logintype.domain.model.request.CompleteProfileRequest
import com.ribuufing.bloodapp.feature.logintype.domain.usecase.CompleteProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompleteProfileViewModel @Inject constructor(
    private val completeProfileUseCase: CompleteProfileUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<CompleteProfileState>(CompleteProfileState.Initial)
    val state: StateFlow<CompleteProfileState> = _state.asStateFlow()

    fun completeProfile(request: CompleteProfileRequest) {
        viewModelScope.launch {
            _state.value = CompleteProfileState.Loading
            try {
                val result = completeProfileUseCase(request)
                if (result.isSuccess) {
                    _state.value = CompleteProfileState.Success
                } else {
                    _state.value = CompleteProfileState.Error(result.resultMessage ?: "Profil tamamlanamadı")
                }
            } catch (e: Exception) {
                _state.value = CompleteProfileState.Error(e.message ?: "Profil tamamlanamadı")
            }
        }
    }

    fun reset() {
        _state.value = CompleteProfileState.Initial
    }
}

sealed class CompleteProfileState {
    object Initial : CompleteProfileState()
    object Loading : CompleteProfileState()
    object Success : CompleteProfileState()
    data class Error(val message: String) : CompleteProfileState()
} 
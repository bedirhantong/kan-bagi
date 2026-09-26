package com.ribuufing.bloodapp.feature.map.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ribuufing.bloodapp.feature.home.data.dto.PostResponse
import com.ribuufing.bloodapp.feature.home.domain.usecase.GetAllBloodRequestsUseCase
import com.ribuufing.bloodapp.feature.listhospitals.data.HospitalResponse
import com.ribuufing.bloodapp.feature.listhospitals.domain.GetAllHospitalsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HospitalDetailViewModel @Inject constructor(
    private val getAllHospitalsUseCase: GetAllHospitalsUseCase,
    private val getAllBloodRequestsUseCase: GetAllBloodRequestsUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(HospitalDetailState())
    val state: StateFlow<HospitalDetailState> = _state.asStateFlow()
    
    fun loadHospitalDetail(hospitalId: Int) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                
                val hospitalsResponse = getAllHospitalsUseCase()
                if (hospitalsResponse.isSuccess && hospitalsResponse.response != null) {
                    val hospitals = hospitalsResponse.response.items ?: emptyList()
                    val hospital = hospitals.find { it.id == hospitalId }
                    
                    if (hospital != null) {
                        _state.update { it.copy(hospital = hospital) }
                        loadHospitalPosts(hospitalId)
                    } else {
                        _state.update { it.copy(
                            error = "Hastane bulunamadı",
                            isLoading = false
                        ) }
                    }
                } else {
                    _state.update { it.copy(
                        error = hospitalsResponse.resultMessage ?: "Hastaneler yüklenemedi", 
                        isLoading = false
                    ) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    error = e.message ?: "Bir hata oluştu", 
                    isLoading = false
                ) }
            }
        }
    }
    
    private fun loadHospitalPosts(hospitalId: Int) {
        viewModelScope.launch {
            try {
                val response = getAllBloodRequestsUseCase(
                    hospitals = listOf(hospitalId)
                )
                
                if (response.isSuccess && response.response != null) {
                    val posts = response.response.items ?: emptyList()
                    _state.update { 
                        it.copy(
                            posts = posts,
                            isLoading = false
                        )
                    }
                } else {
                    _state.update { it.copy(
                        error = response.resultMessage ?: "Kan ihtiyaçları yüklenemedi",
                        isLoading = false
                    ) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    error = e.message ?: "Kan ihtiyaçları yüklenirken bir hata oluştu",
                    isLoading = false
                ) }
            }
        }
    }
}

data class HospitalDetailState(
    val hospital: HospitalResponse? = null,
    val posts: List<PostResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
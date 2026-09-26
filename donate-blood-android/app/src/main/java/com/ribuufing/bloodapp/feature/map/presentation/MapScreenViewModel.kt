package com.ribuufing.bloodapp.feature.map.presentation

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

data class BloodPoint(
    val ekipID: String,
    val ekipAdi: String,
    val koordinatLatitude: Double,
    val koordinatLongitude: Double,
    val adres: String,
    val mahalle: String,
    val ilceAd: String
)

@HiltViewModel
class MapScreenViewModel @Inject constructor(
    private val application: android.app.Application,
    private val getAllHospitalsUseCase: GetAllHospitalsUseCase,
    private val getAllBloodRequestsUseCase: GetAllBloodRequestsUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(MapScreenState())
    val state: StateFlow<MapScreenState> = _state.asStateFlow()

    init {
        loadBloodPoints()
        loadHospitals()
    }

    private fun loadBloodPoints() {
        viewModelScope.launch {
            try {
                val inputStream = application.assets.open("kizilay_blood_points.json")
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                val type = object : TypeToken<List<List<BloodPoint>>>() {}.type
                val bloodPointsList: List<List<BloodPoint>> = Gson().fromJson(jsonString, type)
                
                val uniqueBloodPoints = bloodPointsList.flatten().distinctBy { 
                    "${it.koordinatLatitude},${it.koordinatLongitude}" 
                }
                
                _state.update { currentState ->
                    currentState.copy(bloodPoints = uniqueBloodPoints)
                }
            } catch (e: Exception) {
                setError("Kan bağış noktaları yüklenirken bir hata oluştu: ${e.message}")
            }
        }
    }

    fun updateUserLocation(location: Location) {
        _state.update { currentState ->
            currentState.copy(
                userLocation = LatLng(location.latitude, location.longitude),
                isLocationLoaded = true,
                error = null,
                zoomLevel = 5.8f
            )
        }
    }

    fun updateZoomLevel(zoom: Float) {
        _state.update { currentState ->
            currentState.copy(zoomLevel = zoom)
        }
    }

    private fun loadHospitals() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val response = getAllHospitalsUseCase()
                
                if (response.isSuccess && response.response != null) {
                    val hospitals = response.response.items ?: emptyList()
                    _state.update { 
                        it.copy(
                            hospitals = hospitals,
                            isLoading = false
                        )
                    }
                } else {
                    setError("Hastaneler yüklenemedi: ${response.resultMessage}")
                }
            } catch (e: Exception) {
                setError("Hastaneler yüklenemedi: ${e.message}")
            }
        }
    }
    
    fun loadHospitalPosts(hospitalId: Int) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoadingPosts = true) }
                val response = getAllBloodRequestsUseCase(
                    hospitals = listOf(hospitalId)
                )
                
                if (response.isSuccess && response.response != null) {
                    val posts = response.response.items ?: emptyList()
                    _state.update { 
                        it.copy(
                            selectedHospitalPosts = posts,
                            isLoadingPosts = false
                        )
                    }
                } else {
                    setError("İlanlar yüklenemedi: ${response.resultMessage}")
                }
            } catch (e: Exception) {
                setError("İlanlar yüklenemedi: ${e.message}")
                _state.update { it.copy(isLoadingPosts = false) }
            }
        }
    }

    fun setError(message: String) {
        _state.update { currentState ->
            currentState.copy(
                error = message,
                isLoading = false,
                isLoadingPosts = false
            )
        }
    }

    fun onBloodPointClick(bloodPoint: BloodPoint) {
        _state.update { currentState ->
            currentState.copy(
                selectedBloodPoint = bloodPoint,
                selectedHospital = null,
                isBottomSheetVisible = true,
                sheetType = BottomSheetType.BLOOD_POINT
            )
        }
    }
    
    fun onHospitalClick(hospital: HospitalResponse) {
        _state.update { currentState ->
            currentState.copy(
                selectedHospital = hospital,
                selectedBloodPoint = null,
                isBottomSheetVisible = true,
                sheetType = BottomSheetType.HOSPITAL
            )
        }
        hospital.id?.let { loadHospitalPosts(it) }
    }

    fun hideBottomSheet() {
        _state.update { currentState ->
            currentState.copy(
                isBottomSheetVisible = false,
                selectedBloodPoint = null,
                selectedHospital = null,
                sheetType = BottomSheetType.NONE
            )
        }
    }
}

enum class BottomSheetType {
    NONE,
    BLOOD_POINT,
    HOSPITAL
}

data class MapScreenState(
    val userLocation: LatLng = LatLng(0.0, 0.0),
    val isLocationLoaded: Boolean = false,
    val error: String? = null,
    val mapType: Int = 1,
    val zoomLevel: Float = 5f,
    val bloodPoints: List<BloodPoint> = emptyList(),
    val hospitals: List<HospitalResponse> = emptyList(),
    val selectedBloodPoint: BloodPoint? = null,
    val selectedHospital: HospitalResponse? = null,
    val selectedHospitalPosts: List<PostResponse> = emptyList(),
    val isBottomSheetVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingPosts: Boolean = false,
    val sheetType: BottomSheetType = BottomSheetType.NONE
)
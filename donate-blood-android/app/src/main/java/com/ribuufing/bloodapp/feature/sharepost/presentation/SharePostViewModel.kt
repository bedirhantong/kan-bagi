package com.ribuufing.bloodapp.feature.sharepost.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ribuufing.bloodapp.feature.listhospitals.domain.GetAllHospitalsUseCase
import com.ribuufing.bloodapp.feature.profile.domain.uistate.GetProfileInfosUseCase
import com.ribuufing.bloodapp.feature.sharepost.data.model.CreateBloodRequestBody
import com.ribuufing.bloodapp.feature.sharepost.domain.mapper.HospitalMapper.toHospitalList
import com.ribuufing.bloodapp.feature.sharepost.domain.model.BloodType
import com.ribuufing.bloodapp.feature.sharepost.domain.model.Hospital
import com.ribuufing.bloodapp.feature.sharepost.domain.usecase.CreateBloodRequestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.ribuufing.bloodapp.core.utils.ErrorHandlingUtils

@HiltViewModel
class SharePostViewModel @Inject constructor(
    private val createBloodRequestUseCase: CreateBloodRequestUseCase,
    private val getProfileInfosUseCase: GetProfileInfosUseCase,
    private val getAllHospitalsUseCase: GetAllHospitalsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SharePostUiState())
    val state = _state.asStateFlow()

    private val _uiEvent = Channel<SharePostUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _hospitals = MutableStateFlow<List<Hospital>>(emptyList())
    val hospitals = _hospitals.asStateFlow()

    private val _bloodTypes = MutableStateFlow<List<BloodType>>(emptyList())
    val bloodTypes = _bloodTypes.asStateFlow()

    fun loadProfileInfo() {
        viewModelScope.launch {
            try {
                val response = getProfileInfosUseCase()
                if (response.isSuccess && response.response != null) {
                    val profile = response.response
                    updateState { 
                        copy(
                            ownerName = profile.name ?: "",
                            ownerSurname = profile.surname ?: "",
                            phoneNumbers = listOfNotNull(profile.phoneNumber).filter { it.isNotBlank() },
                            ownerId = profile.id ?: ""
                        )
                    }
                }
            } catch (e: Exception) {
                handleError(e.message ?: "Failed to load profile information")
            }
        }
    }

    fun loadInitialData() {
        viewModelScope.launch {
            try {
                val hospitalsResponse = getAllHospitalsUseCase()
                if (hospitalsResponse.isSuccess && hospitalsResponse.response != null) {
                    _hospitals.value = hospitalsResponse.response.items?.toHospitalList() ?:  emptyList()
                } else {
                    handleError(hospitalsResponse.resultMessage ?: "Failed to load hospitals")
                }

                val bloodTypes = listOf(
                    BloodType("1", "A_POSITIVE", "A Pozitif"),
                    BloodType("2", "A_NEGATIVE", "A Negatif"),
                    BloodType("3", "B_POSITIVE", "B Pozitif"),
                    BloodType("4", "B_NEGATIVE", "B Negatif"),
                    BloodType("5", "AB_POSITIVE", "AB Pozitif"),
                    BloodType("6", "AB_NEGATIVE", "AB Negatif"),
                    BloodType("7", "O_POSITIVE", "0 Pozitif"),
                    BloodType("8", "O_NEGATIVE", "0 Negatif")
                )
                _bloodTypes.value = bloodTypes
            } catch (e: Exception) {
                handleError(e.message ?: "Failed to load data")
            }
        }
    }

    fun onEvent(event: SharePostEvent) {
        when (event) {
            is SharePostEvent.PatientNameChanged -> updateState { copy(patientName = event.value) }
            is SharePostEvent.PatientAgeChanged -> updateState { copy(patientAge = event.value) }
            is SharePostEvent.HospitalSelected -> updateState { copy(selectedHospital = event.hospital) }
            is SharePostEvent.BloodTypeSelected -> updateState { copy(selectedBloodType = event.bloodType) }
            is SharePostEvent.RequesterNameChanged -> updateState { copy(ownerName = event.value) }
            is SharePostEvent.RequesterSurnameChanged -> updateState { copy(ownerSurname = event.value) }
            is SharePostEvent.ContactNumberAdded -> addPhoneNumber(event.value)
            is SharePostEvent.ContactNumberRemoved -> removePhoneNumber(event.value)
            is SharePostEvent.TitleChanged -> updateState { copy(title = event.value) }
            is SharePostEvent.AdditionalInfoChanged -> updateState { copy(description = event.value) }
            is SharePostEvent.UrgencyLevelChanged -> updateState { copy(urgencyLevel = event.value) }
            is SharePostEvent.TermsAccepted -> updateState { copy(acceptedTerms = event.value) }
            is SharePostEvent.HospitalDropdownExpandedChanged -> updateState { copy(isHospitalDropdownExpanded = event.value) }
            is SharePostEvent.BloodTypeDropdownExpandedChanged -> updateState { copy(isBloodTypeDropdownExpanded = event.value) }
            SharePostEvent.SubmitRequest -> submitRequest()
            SharePostEvent.DismissError -> updateState { copy(error = null) }
            SharePostEvent.CloseErrorDialog -> updateState { copy(showErrorDialog = false) }
            is SharePostEvent.HospitalSearch -> searchHospitals(event.query)
            is SharePostEvent.BloodTypeSearch -> searchBloodTypes(event.query)
        }
    }

    private fun addPhoneNumber(phoneNumber: String) {
        if (phoneNumber.isNotBlank() && !state.value.phoneNumbers.contains(phoneNumber)) {
            updateState { copy(phoneNumbers = phoneNumbers + phoneNumber) }
        }
    }

    private fun removePhoneNumber(phoneNumber: String) {
        updateState { copy(phoneNumbers = phoneNumbers - phoneNumber) }
    }

    private fun submitRequest() {
        viewModelScope.launch {
            try {
                updateState { copy(isLoading = true, error = null) }
                
                validateInput()?.let { error ->
                    handleError(error)
                    return@launch
                }

                val requestBody = CreateBloodRequestBody(
                    ownerName = state.value.ownerName,
                    ownerSurname = state.value.ownerSurname,
                    patientFullName = state.value.patientName,
                    patientAge = state.value.patientAge.toIntOrNull() ?: 0,
                    title = state.value.title,
                    description = state.value.description,
                    phoneNumbers = state.value.phoneNumbers,
                    bloodType = state.value.selectedBloodType?.type ?: "",
                    hospitalId = state.value.selectedHospital?.id ?: 0,
                )

                val response = createBloodRequestUseCase(requestBody)
                
                if (response.isSuccess) {
                    _uiEvent.send(SharePostUiEvent.Success)
                    updateState { copy(isSuccess = true) }
                } else {
                    handleError(response.resultMessage ?: "Kan bağışı isteği oluşturulurken bir hata oluştu")
                }
            } catch (e: Exception) {
                handleError(e.message ?: "Bir hata oluştu")
            } finally {
                updateState { copy(isLoading = false) }
            }
        }
    }

    private fun validateInput(): String? {
        return when {
            state.value.patientName.isBlank() -> "Hasta adı gerekli"
            state.value.patientAge.isBlank() -> "Hasta yaşı gerekli"
            state.value.selectedHospital == null -> "Hastane seçimi gerekli"
            state.value.selectedBloodType == null -> "Kan grubu seçimi gerekli"
            state.value.ownerName.isBlank() -> "İstek sahibi adı gerekli"
            state.value.ownerSurname.isBlank() -> "İstek sahibi soyadı gerekli"
            state.value.phoneNumbers.isEmpty() -> "En az bir telefon numarası gerekli"
            state.value.title.isBlank() -> "Başlık gerekli"
            !state.value.acceptedTerms -> "Şartları ve koşulları kabul etmeniz gerekli"
            else -> null
        }
    }

    private fun handleError(error: String) {
        updateState { copy(error = ErrorHandlingUtils.getCleanErrorMessage(error), showErrorDialog = true) }
    }

    private fun searchHospitals(query: String) {
        viewModelScope.launch {
            val filteredHospitals = hospitals.value.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.city.contains(query, ignoreCase = true) ||
                it.address.contains(query, ignoreCase = true)
            }
            updateState { copy(filteredHospitals = filteredHospitals) }
        }
    }

    private fun searchBloodTypes(query: String) {
        viewModelScope.launch {
            val filteredBloodTypes = bloodTypes.value.filter {
                it.type.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
            updateState { copy(filteredBloodTypes = filteredBloodTypes) }
        }
    }

    private fun updateState(update: SharePostUiState.() -> SharePostUiState) {
        _state.value = update(_state.value)
    }
}

data class SharePostUiState(
    val patientName: String = "",
    val patientAge: String = "",
    val selectedHospital: Hospital? = null,
    val selectedBloodType: BloodType? = null,
    val ownerName: String = "",
    val ownerSurname: String = "",
    val ownerId: String = "",
    val phoneNumbers: List<String> = emptyList(),
    val title: String = "",
    val description: String = "",
    val urgencyLevel: Int = 1,
    val acceptedTerms: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showErrorDialog: Boolean = false,
    val filteredHospitals: List<Hospital> = emptyList(),
    val filteredBloodTypes: List<BloodType> = emptyList(),
    val isSuccess: Boolean = false,
    val isHospitalDropdownExpanded: Boolean = false,
    val isBloodTypeDropdownExpanded: Boolean = false
)

sealed class SharePostEvent {
    data class PatientNameChanged(val value: String) : SharePostEvent()
    data class PatientAgeChanged(val value: String) : SharePostEvent()
    data class HospitalSelected(val hospital: Hospital) : SharePostEvent()
    data class BloodTypeSelected(val bloodType: BloodType) : SharePostEvent()
    data class RequesterNameChanged(val value: String) : SharePostEvent()
    data class RequesterSurnameChanged(val value: String) : SharePostEvent()
    data class ContactNumberAdded(val value: String) : SharePostEvent()
    data class ContactNumberRemoved(val value: String) : SharePostEvent()
    data class TitleChanged(val value: String) : SharePostEvent()
    data class AdditionalInfoChanged(val value: String) : SharePostEvent()
    data class UrgencyLevelChanged(val value: Int) : SharePostEvent()
    data class TermsAccepted(val value: Boolean) : SharePostEvent()
    data class HospitalDropdownExpandedChanged(val value: Boolean) : SharePostEvent()
    data class BloodTypeDropdownExpandedChanged(val value: Boolean) : SharePostEvent()
    data class HospitalSearch(val query: String) : SharePostEvent()
    data class BloodTypeSearch(val query: String) : SharePostEvent()
    data object SubmitRequest : SharePostEvent()
    data object DismissError : SharePostEvent()
    data object CloseErrorDialog : SharePostEvent()
}

sealed class SharePostUiEvent {
    data object Success : SharePostUiEvent()
}
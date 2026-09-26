package com.ribuufing.bloodapp.feature.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ribuufing.bloodapp.feature.listhospitals.domain.GetAllHospitalsUseCase
import com.ribuufing.bloodapp.feature.settings.domain.response.NotificationPreferencesRequest
import com.ribuufing.bloodapp.feature.settings.domain.usecase.GetNotificationPreferencesUseCase
import com.ribuufing.bloodapp.feature.settings.domain.usecase.SetNotificationPreferencesUseCase
import com.ribuufing.bloodapp.feature.sharepost.domain.mapper.HospitalMapper.toHospitalList
import com.ribuufing.bloodapp.feature.sharepost.domain.model.Hospital
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val setNotificationPreferencesUseCase: SetNotificationPreferencesUseCase,
    private val getNotificationPreferencesUseCase: GetNotificationPreferencesUseCase,
    private val getAllHospitalsUseCase: GetAllHospitalsUseCase
): ViewModel() {
    private val _state = MutableStateFlow(NotificationSettingsState())
    val state = _state.asStateFlow()

    init {
        loadNotificationPreferences()
        loadHospitals()
    }

    private fun loadNotificationPreferences() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val response = getNotificationPreferencesUseCase()
                
                if (response.isSuccess && response.response != null) {
                    val preferences = response.response
                    _state.update { currentState ->
                        currentState.copy(
                            emailNotifications = preferences.email ?: false,
                            phoneNotifications = preferences.phoneNumber ?: false,
                            pushNotifications = preferences.pushNotification ?: false,
                            selectedHospitals = preferences.preferredHospitals?.mapNotNull { it.toString() } ?: emptyList(),
                            selectedBloodTypes = preferences.preferredBloodTypes ?: emptyList(),
                            notificationsEnabled = (preferences.email ?: false) ||
                                    (preferences.phoneNumber ?: false) ||
                                    (preferences.pushNotification ?: false),
                            isLoading = false,
                            error = null
                        )
                    }
                } else {
                    _state.update { it.copy(
                        error = response.resultMessage ?: "Bildirim tercihleri yüklenemedi",
                        isLoading = false
                    ) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    error = e.message ?: "Bildirim tercihleri yüklenirken bir hata oluştu",
                    isLoading = false
                ) }
            }
        }
    }

    private fun loadHospitals() {
        viewModelScope.launch {
            try {
                val response = getAllHospitalsUseCase()
                if (response.isSuccess && response.response != null) {
                    _state.update { currentState ->
                        currentState.copy(
                            availableHospitals = response.response.items?.toHospitalList() ?: emptyList()
                        )
                    }
                } else {
                    _state.update { it.copy(error = response.resultMessage) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Hastaneler yüklenirken bir hata oluştu") }
            }
        }
    }

    fun updateNotificationSettings(
        emailEnabled: Boolean? = null,
        phoneEnabled: Boolean? = null,
        pushEnabled: Boolean? = null,
        selectedHospitals: List<String>? = null,
        selectedBloodTypes: List<String>? = null
    ) {
        _state.update { currentState ->
            currentState.copy(
                emailNotifications = emailEnabled ?: currentState.emailNotifications,
                phoneNotifications = phoneEnabled ?: currentState.phoneNotifications,
                pushNotifications = pushEnabled ?: currentState.pushNotifications,
                selectedHospitals = selectedHospitals ?: currentState.selectedHospitals,
                selectedBloodTypes = selectedBloodTypes ?: currentState.selectedBloodTypes,
                notificationsEnabled = (emailEnabled ?: currentState.emailNotifications) ||
                        (phoneEnabled ?: currentState.phoneNotifications) ||
                        (pushEnabled ?: currentState.pushNotifications)
            )
        }

        saveNotificationPreferences()
    }

    private fun saveNotificationPreferences() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isSaving = true) }
                val request = NotificationPreferencesRequest(
                    email = _state.value.emailNotifications,
                    phoneNumber = _state.value.phoneNotifications,
                    pushNotification = _state.value.pushNotifications,
                    preferredHospitals = _state.value.selectedHospitals,
                    preferredBloodTypes = _state.value.selectedBloodTypes
                )

                val response = setNotificationPreferencesUseCase(request)
                if (response.isSuccess) {
                    _state.update { it.copy(error = null, isSaving = false) }
                } else {
                    _state.update { it.copy(
                        error = response.resultMessage ?: "Tercihler kaydedilemedi",
                        isSaving = false
                    ) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    error = e.message ?: "Tercihler kaydedilirken bir hata oluştu",
                    isSaving = false
                ) }
            }
        }
    }

    fun toggleHospitalSelection(hospitalId: String) {
        _state.update { currentState ->
            val currentList = currentState.selectedHospitals.toMutableList()
            if (hospitalId in currentList) {
                currentList.remove(hospitalId)
            } else {
                currentList.add(hospitalId)
            }
            currentState.copy(selectedHospitals = currentList)
        }
        saveNotificationPreferences()
    }

    fun toggleBloodTypeSelection(bloodType: String) {
        _state.update { currentState ->
            val currentList = currentState.selectedBloodTypes.toMutableList()
            if (bloodType in currentList) {
                currentList.remove(bloodType)
            } else {
                currentList.add(bloodType)
            }
            currentState.copy(selectedBloodTypes = currentList)
        }
        saveNotificationPreferences()
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    fun refresh() {
        loadNotificationPreferences()
        loadHospitals()
    }
}

data class NotificationSettingsState(
    val notificationsEnabled: Boolean = false,
    val emailNotifications: Boolean = false,
    val phoneNotifications: Boolean = false,
    val pushNotifications: Boolean = false,
    val selectedHospitals: List<String> = emptyList(),
    val selectedBloodTypes: List<String> = emptyList(),
    val availableHospitals: List<Hospital> = emptyList(),
    val error: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false
)
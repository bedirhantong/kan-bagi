package com.ribuufing.bloodapp.feature.settings.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.microsoft.identity.client.IPublicClientApplication
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.exception.MsalException
import com.ribuufing.bloodapp.R
import com.ribuufing.bloodapp.core.manager.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val authManager: AuthManager
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    private val _logoutState = MutableStateFlow<LogoutState>(LogoutState.Initial)
    val logoutState: StateFlow<LogoutState> = _logoutState.asStateFlow()

    private var singleAccountApp: ISingleAccountPublicClientApplication? = null

    init {
        loadSettings()
        initializeMsal()
    }

    private fun initializeMsal() {
        PublicClientApplication.createSingleAccountPublicClientApplication(
            application.applicationContext,
            R.raw.auth_config_b2c,
            object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication) {
                    singleAccountApp = application
                }

                override fun onError(exception: MsalException) {
                    Log.e("SettingsViewModel", "MSAL initialization failed", exception)
                    _logoutState.value = LogoutState.Error("MSAL initialization failed")
                }
            }
        )
    }

    private fun loadSettings() {
        _state.value = SettingsState(
            theme = "Sistem Varsayılanı",
            language = "Türkçe",
            notificationsEnabled = true,
            emailNotifications = true,
            pushNotifications = true
        )
    }

    fun updateTheme(theme: String) {
        _state.update { it.copy(theme = theme) }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                // 1. MSAL Logout
                singleAccountApp?.signOut(object : ISingleAccountPublicClientApplication.SignOutCallback {
                    override fun onSignOut() {
                        // 2. Clear SharedPreferences
                        viewModelScope.launch {
                            try {
                                authManager.clearLoginState()
                                _logoutState.value = LogoutState.Success
                            } catch (e: Exception) {
                                Log.e("SettingsViewModel", "Error clearing SharedPreferences", e)
                                _logoutState.value = LogoutState.Error("Failed to clear local data")
                            }
                        }
                    }

                    override fun onError(exception: MsalException) {
                        Log.e("SettingsViewModel", "MSAL Sign out error", exception)
                        _logoutState.value = LogoutState.Error("Sign out failed")
                    }
                }) ?: run {
                    // If MSAL is not initialized, just clear SharedPreferences
                    authManager.clearLoginState()
                    _logoutState.value = LogoutState.Success
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Unexpected error during logout", e)
                _logoutState.value = LogoutState.Error("Unexpected error during logout")
            }
        }
    }
}

data class SettingsState(
    val theme: String = "Sistem Varsayılanı",
    val language: String = "Türkçe",
    val notificationsEnabled: Boolean = true,
    val emailNotifications: Boolean = true,
    val pushNotifications: Boolean = true
)

sealed class LogoutState {
    object Initial : LogoutState()
    object Success : LogoutState()
    data class Error(val message: String) : LogoutState()
}
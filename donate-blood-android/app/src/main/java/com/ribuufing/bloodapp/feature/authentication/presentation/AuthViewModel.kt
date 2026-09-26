package com.ribuufing.bloodapp.feature.authentication.presentation

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalException
import com.ribuufing.bloodapp.R
import com.ribuufing.bloodapp.core.manager.AuthManager
import com.ribuufing.bloodapp.feature.logintype.domain.usecase.IsProfileCompletedUseCase
import com.ribuufing.bloodapp.feature.form.domain.usecase.GetFormUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.navigation.NavController

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val isProfileCompletedUseCase: IsProfileCompletedUseCase,
    private val getFormUseCase: GetFormUseCase,
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _profileState = MutableStateFlow<ProfileCompletionState>(ProfileCompletionState.Initial)
    val profileState: StateFlow<ProfileCompletionState> = _profileState.asStateFlow()

    private var singleAccountApp: ISingleAccountPublicClientApplication? = null
    private var currentAccount: IAccount? = null
    private var checkedFormOnce = false

    // --- FORM STATE ---
    // Form ile ilgili tüm state ve fonksiyonlar kaldırıldı. Sadece profil tamamlanınca form akışı başlatılacak.

    companion object {
        private const val TAG = "AuthViewModel"
        private const val B2C_AUTHORITY = "https://bloodapp.b2clogin.com/tfp/bloodapp.onmicrosoft.com/B2C_1_signup_signin"
        
        private val USER_SCOPES = arrayOf(
            "https://bloodapp.onmicrosoft.com/c6230b95-d631-446c-b5ac-a5cf299039f4/gateway.access"
        )
        
        private val HOSPITAL_STAFF_SCOPES = arrayOf(
            "https://bloodapp.onmicrosoft.com/c6230b95-d631-446c-b5ac-a5cf299039f4/gateway.hospital.access"
        )
    }

    fun initializeMsal(activity: Activity) {
        Log.d(TAG, "Initializing MSAL...")
        PublicClientApplication.createSingleAccountPublicClientApplication(
            activity.applicationContext,
            R.raw.auth_config_b2c,
            object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication) {
                    Log.d(TAG, "MSAL Application created successfully")
                    singleAccountApp = application
                    loadCurrentAccount()
                }

                override fun onError(exception: MsalException) {
                    Log.e(TAG, "MSAL initialization failed", exception)
                    _loginState.value = LoginState.Error("MSAL initialization failed: ${exception.message}")
                }
            }
        )
    }

    private fun loadCurrentAccount() {
        Log.d(TAG, "Loading current account...")
        singleAccountApp?.getCurrentAccountAsync(object : ISingleAccountPublicClientApplication.CurrentAccountCallback {
            override fun onAccountLoaded(activeAccount: IAccount?) {
                Log.d(TAG, "Account loaded: ${activeAccount?.username}")
                currentAccount = activeAccount
                if (activeAccount != null) {
                    signInInteractively(null)
                } else {
                    _loginState.value = LoginState.Initial
                }
            }

            override fun onAccountChanged(priorAccount: IAccount?, currentAccount: IAccount?) {
                Log.d(TAG, "Account changed from ${priorAccount?.username} to ${currentAccount?.username}")
                if (currentAccount == null) {
                    _loginState.value = LoginState.Initial
                }
            }

            override fun onError(exception: MsalException) {
                Log.e(TAG, "Error loading account", exception)
                _loginState.value = LoginState.Error("Failed to load account: ${exception.message}")
            }
        })
    }

    fun signIn(activity: Activity, isHospitalStaff: Boolean = false) {
        Log.d(TAG, "Starting sign in process for ${if (isHospitalStaff) "hospital staff" else "regular user"}...")
        signInInteractively(activity, isHospitalStaff)
    }

    private fun signInInteractively(activity: Activity?, isHospitalStaff: Boolean = false) {
        try {
            val scopes = if (isHospitalStaff) HOSPITAL_STAFF_SCOPES else USER_SCOPES
            
            val parameters = AcquireTokenParameters.Builder()
                .startAuthorizationFromActivity(activity)
                .withScopes(scopes.toList())
                .withPrompt(Prompt.SELECT_ACCOUNT)
                .fromAuthority(B2C_AUTHORITY)
                .withCallback(object : AuthenticationCallback {
                    override fun onSuccess(authResult: IAuthenticationResult) {
                        Log.d(TAG, "Authentication successful for ${if (isHospitalStaff) "hospital staff" else "regular user"}")
                        Log.d(TAG, "Access Token acquired: ${authResult.accessToken.take(10)}...")
                        authManager.saveMsalAccessToken(authResult.accessToken)
                        authManager.saveUserId(authResult.tenantId)
                        authManager.saveUserType(if (isHospitalStaff) "HOSPITAL_STAFF" else "REGULAR_USER")
                        currentAccount = authResult.account
                        _loginState.value = LoginState.MsalSuccess(authResult)
                        if (!isHospitalStaff){
                            checkProfileCompletion()
                        }
                    }

                    override fun onError(exception: MsalException) {
                        Log.e(TAG, "Authentication error", exception)
                        val detailedMessage = """
                            Authentication failed:
                            - Error Code: ${exception.errorCode}
                            - Error Message: ${exception.message}
                            - Exception Type: ${exception.javaClass.simpleName}
                        """.trimIndent()
                        _loginState.value = LoginState.Error(detailedMessage)
                    }

                    override fun onCancel() {
                        Log.d(TAG, "Authentication cancelled by user")
                        _loginState.value = LoginState.Error("Sign in was cancelled")
                    }
                })
                .build()

            singleAccountApp?.acquireToken(parameters)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during sign in", e)
            _loginState.value = LoginState.Error("Unexpected error: ${e.message}")
        }
    }

    fun signOut() {
        Log.d(TAG, "Starting sign out process...")
        singleAccountApp?.signOut(object : ISingleAccountPublicClientApplication.SignOutCallback {
            override fun onSignOut() {
                Log.d(TAG, "Sign out successful")
                currentAccount = null
                _loginState.value = LoginState.Initial
            }

            override fun onError(exception: MsalException) {
                Log.e(TAG, "Sign out error", exception)
                _loginState.value = LoginState.Error("Sign out failed: ${exception.message}")
            }
        })
    }

    fun checkProfileCompletion() {
        viewModelScope.launch {
            try {
                _profileState.value = ProfileCompletionState.Loading
                val result = isProfileCompletedUseCase()
                if (result.isSuccess) {
                    result.response?.let { response ->
                        if (response.profileCompleted) {
                            _profileState.value = ProfileCompletionState.Completed
                            _loginState.value = LoginState.Success
                        } else {
                            _profileState.value = ProfileCompletionState.RequiresCompletion
                        }
                    } ?: run {
                        _profileState.value = ProfileCompletionState.Error("Profile check response is empty")
                        _loginState.value = LoginState.Error("Profile check response is empty")
                    }
                } else {
                    _profileState.value = ProfileCompletionState.Error(result.resultMessage ?: "Unknown error")
                    _loginState.value = LoginState.Error(result.resultMessage ?: "Failed to check profile completion")
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Unknown error during profile check"
                _profileState.value = ProfileCompletionState.Error(errorMessage)
                _loginState.value = LoginState.Error(errorMessage)
            }
        }
    }

    fun isHospitalStaff(): Boolean {
        return authManager.isHospitalStaff()
    }

    fun setLoginSuccess() {
        _loginState.value = LoginState.Success
    }

    fun resetFormCheck() {
        checkedFormOnce = false
    }

    suspend fun checkFormAndNavigate(navController: NavController) {
        if (!checkedFormOnce) {
            checkedFormOnce = true
            val result = getFormUseCase()
            if (result.success) {
                navController.navigate(com.ribuufing.bloodapp.navigation.BottomNavigationItems.Home.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            } else {
                navController.navigate("form_screen") {
                    popUpTo(0) { inclusive = false }
                    launchSingleTop = true
                }
            }
        }
    }
}

sealed class LoginState {
    object Initial : LoginState()
    data class MsalSuccess(val authResult: IAuthenticationResult) : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class ProfileCompletionState {
    object Initial : ProfileCompletionState()
    object Loading : ProfileCompletionState()
    object RequiresCompletion : ProfileCompletionState()
    object Completed : ProfileCompletionState()
    data class Error(val message: String) : ProfileCompletionState()
}
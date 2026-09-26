package com.ribuufing.bloodapp.feature.authentication.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ribuufing.bloodapp.feature.authentication.domain.model.request.BloodType
import com.ribuufing.bloodapp.feature.authentication.domain.model.request.RegisterRequestModel
import com.ribuufing.bloodapp.feature.authentication.domain.model.request.UserType
import com.ribuufing.bloodapp.feature.authentication.domain.usecase.RegisterUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val registerUserUseCase: RegisterUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SignupState())
    val state = _state.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun onEvent(event: SignupEvent) {
        when (event) {
            is SignupEvent.UpdateEmail -> {
                _state.update { it.copy(email = event.email, emailError = null) }
            }
            is SignupEvent.UpdatePassword -> {
                _state.update { it.copy(password = event.password, passwordError = null) }
            }
            is SignupEvent.UpdateConfirmPassword -> {
                _state.update { it.copy(confirmPassword = event.confirmPassword, confirmPasswordError = null) }
            }
            is SignupEvent.UpdateName -> {
                _state.update { it.copy(name = event.name, nameError = null) }
            }
            is SignupEvent.UpdateSurname -> {
                _state.update { it.copy(surname = event.surname, surnameError = null) }
            }
            is SignupEvent.UpdateTcIdentityNumber -> {
                _state.update { it.copy(tcIdentityNumber = event.tcIdentityNumber, tcIdentityNumberError = null) }
            }
            is SignupEvent.UpdateBirthDate -> {
                _state.update { it.copy(birthDate = event.birthDate, birthDateError = null) }
            }
            is SignupEvent.UpdateBloodType -> {
                _state.update { it.copy(bloodType = event.bloodType, bloodTypeError = null) }
            }
            is SignupEvent.UpdateUserType -> {
                _state.update { it.copy(userType = event.userType, userTypeError = null) }
            }
            SignupEvent.NextStep -> validateAndMoveToNextStep()
            SignupEvent.PreviousStep -> moveToPreviousStep()
            SignupEvent.Submit -> handleSignup()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun validateAndMoveToNextStep() {
        when (_state.value.currentStep) {
            SignupStep.CREDENTIALS -> validateCredentials()
            SignupStep.PERSONAL -> validatePersonal()
            SignupStep.TYPE -> validateType()
            SignupStep.SUMMARY -> handleSignup()
        }
    }

    private fun validateCredentials() {
        val state = _state.value
        when {
            state.email.isBlank() -> {
                _state.update { it.copy(emailError = "Email gereklidir") }
            }
            !state.email.contains("@") -> {
                _state.update { it.copy(emailError = "Geçerli bir email giriniz") }
            }
            state.password.length < 8 -> {
                _state.update { it.copy(passwordError = "Şifre en az 8 karakter olmalıdır") }
            }
            !state.password.matches(Regex(".*[A-Z].*")) -> {
                _state.update { it.copy(passwordError = "Şifre en az bir büyük harf içermelidir") }
            }
            !state.password.matches(Regex(".*[a-z].*")) -> {
                _state.update { it.copy(passwordError = "Şifre en az bir küçük harf içermelidir") }
            }
            !state.password.matches(Regex(".*[!@#\$%^&*()\\-_=+\\[\\]{};:'\",.<>/?].*")) -> {
                _state.update { it.copy(passwordError = "Şifre en az bir özel karakter içermelidir") }
            }
            state.password != state.confirmPassword -> {
                _state.update { it.copy(confirmPasswordError = "Şifreler eşleşmiyor") }
            }
            else -> {
                _state.update { it.copy(currentStep = SignupStep.PERSONAL) }
            }
        }
    }

    private fun validatePersonal() {
        val state = _state.value
        when {
            state.name.length < 2 -> {
                _state.update { it.copy(nameError = "İsim en az 2 karakter olmalıdır") }
            }
            state.surname.length < 2 -> {
                _state.update { it.copy(surnameError = "Soyisim en az 2 karakter olmalıdır") }
            }
            state.tcIdentityNumber.length != 11 -> {
                _state.update { it.copy(tcIdentityNumberError = "TC Kimlik No 11 haneli olmalıdır") }
            }
            state.birthDate == null -> {
                _state.update { it.copy(birthDateError = "Doğum tarihi gereklidir") }
            }
            else -> {
                _state.update { it.copy(currentStep = SignupStep.TYPE) }
            }
        }
    }

    private fun validateType() {
        val state = _state.value
        when {
            state.bloodType == null -> {
                _state.update { it.copy(bloodTypeError = "Kan grubu seçimi gereklidir") }
            }
            state.userType == null -> {
                _state.update { it.copy(userTypeError = "Kullanıcı tipi seçimi gereklidir") }
            }
            else -> {
                _state.update { it.copy(currentStep = SignupStep.SUMMARY) }
            }
        }
    }

    private fun moveToPreviousStep() {
        _state.update {
            it.copy(
                currentStep = when (it.currentStep) {
                    SignupStep.CREDENTIALS -> SignupStep.CREDENTIALS
                    SignupStep.PERSONAL -> SignupStep.CREDENTIALS
                    SignupStep.TYPE -> SignupStep.PERSONAL
                    SignupStep.SUMMARY -> SignupStep.TYPE
                }
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleSignup() {

    }
}

data class SignupState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val name: String = "",
    val surname: String = "",
    val tcIdentityNumber: String = "",
    val birthDate: LocalDate? = null,
    val bloodType: BloodType? = null,
    val userType: UserType? = null,
    val currentStep: SignupStep = SignupStep.CREDENTIALS,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val nameError: String? = null,
    val surnameError: String? = null,
    val tcIdentityNumberError: String? = null,
    val birthDateError: String? = null,
    val bloodTypeError: String? = null,
    val userTypeError: String? = null,
    val isLoading: Boolean = false,
    val registrationSuccess: Boolean = false,
    val error: String? = null
)

enum class SignupStep {
    CREDENTIALS,
    PERSONAL,
    TYPE,
    SUMMARY
}

sealed class SignupEvent {
    data class UpdateEmail(val email: String) : SignupEvent()
    data class UpdatePassword(val password: String) : SignupEvent()
    data class UpdateConfirmPassword(val confirmPassword: String) : SignupEvent()
    data class UpdateName(val name: String) : SignupEvent()
    data class UpdateSurname(val surname: String) : SignupEvent()
    data class UpdateTcIdentityNumber(val tcIdentityNumber: String) : SignupEvent()
    data class UpdateBirthDate(val birthDate: LocalDate) : SignupEvent()
    data class UpdateBloodType(val bloodType: BloodType) : SignupEvent()
    data class UpdateUserType(val userType: UserType) : SignupEvent()
    object NextStep : SignupEvent()
    object PreviousStep : SignupEvent()
    object Submit : SignupEvent()
} 
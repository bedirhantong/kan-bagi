package com.ribuufing.bloodapp.feature.scanqr.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ribuufing.bloodapp.feature.postdetail.domain.usecase.ValidateMatchingUseCase
import com.ribuufing.bloodapp.feature.postdetail.data.ValidateRequestBody
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QrScreenViewModel @Inject constructor(
    private val validateMatchingUseCase: ValidateMatchingUseCase
) : ViewModel() {
    
    private val _scanResult = MutableStateFlow<String?>(null)
    val scanResult: StateFlow<String?> = _scanResult
    
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent
    
    fun onQrCodeScanned(code: String) {
        viewModelScope.launch {
            Log.d("QrScanner", "Scanned QR code: $code")
            _scanResult.value = code
            val response = validateMatchingUseCase(ValidateRequestBody(matchingId = code))
            _uiEvent.emit(UiEvent.ShowMessage(response.resultMessage ?: if (response.success) "Başarılı" else "Bir hata oluştu", response.success))
        }
    }
    
    fun resetScanResult() {
        _scanResult.value = null
    }

    sealed class UiEvent {
        data class ShowMessage(val message: String, val isSuccess: Boolean): UiEvent()
    }
}
package com.ribuufing.bloodapp.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ribuufing.bloodapp.feature.home.data.dto.PostResponse
import com.ribuufing.bloodapp.feature.home.data.dto.SetPostActivenessRequestBody
import com.ribuufing.bloodapp.feature.home.data.dto.UpdatePostRequestBody
import com.ribuufing.bloodapp.feature.home.domain.usecase.GetSingleBloodRequestsUseCase
import com.ribuufing.bloodapp.feature.home.domain.usecase.SetPostActivenessUseCase
import com.ribuufing.bloodapp.feature.home.domain.usecase.UpdateBloodRequestUseCase
import com.ribuufing.bloodapp.feature.postdetail.domain.usecase.DeleteBloodRequestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OwneredPostDetailViewModel @Inject constructor(
    private val getSingleBloodRequestsUseCase: GetSingleBloodRequestsUseCase,
    private val updateBloodRequestUseCase: UpdateBloodRequestUseCase,
    private val setPostActivenessUseCase: SetPostActivenessUseCase,
    private val deleteBloodRequestUseCase: DeleteBloodRequestUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(OwneredPostDetailUiState())
    val state: StateFlow<OwneredPostDetailUiState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<OwneredPostDetailUiEvent>()
    val uiEvent: SharedFlow<OwneredPostDetailUiEvent> = _uiEvent

    fun loadPostDetail(postId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val response = getSingleBloodRequestsUseCase(postId)
            if (response.isSuccess && response.response != null) {
                _state.value = _state.value.copy(post = response.response, isLoading = false)
            } else {
                _state.value = _state.value.copy(isLoading = false)
                _uiEvent.emit(OwneredPostDetailUiEvent.ShowMessage(response.resultMessage ?: "Detay yüklenemedi", false))
            }
        }
    }

    fun updatePost(post: PostResponse) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            var bloodType =   formatBloodType(post.bloodType?: "")

            val req = UpdatePostRequestBody(
                id = post.id,
                patientFullName = post.patientFullName,
                patientAge = post.patientAge,
                title = post.title,
                description = post.description,
                phoneNumbers = post.phoneNumbers,
                bloodType = bloodType,
                hospitalId = post.hospital?.id.toString(),
                hospitalName = post.hospital?.name,
                hospitalAddress = post.hospital?.address,
                hospitalIcon = post.hospital?.hospitalIcon,
                isActive = post.isActive
            )
            val response = updateBloodRequestUseCase(req)
            if (response.isSuccess) {
                _uiEvent.emit(OwneredPostDetailUiEvent.ShowMessage(response.resultMessage ?: "Güncellendi", true))
                loadPostDetail(post.id ?: "")
            } else {
                _uiEvent.emit(OwneredPostDetailUiEvent.ShowMessage(response.resultMessage ?: "Güncellenemedi", false))
            }
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun toggleStatus(post: PostResponse) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val req = SetPostActivenessRequestBody(
                postId = post.id,
                isActive = post.isActive != true
            )
            val response = setPostActivenessUseCase(req)
            if (response.isSuccess) {
                _uiEvent.emit(OwneredPostDetailUiEvent.ShowMessage(response.resultMessage ?: "Durum güncellendi", true))
                loadPostDetail(post.id ?: "")
            } else {
                _uiEvent.emit(OwneredPostDetailUiEvent.ShowMessage(response.resultMessage ?: "Durum güncellenemedi", false))
            }
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val response = deleteBloodRequestUseCase(postId)
            if (response.isSuccess) {
                _uiEvent.emit(OwneredPostDetailUiEvent.ShowMessage(response.resultMessage ?: "Silindi", true))
                _uiEvent.emit(OwneredPostDetailUiEvent.PostDeleted)
            } else {
                _uiEvent.emit(OwneredPostDetailUiEvent.ShowMessage(response.resultMessage ?: "Silinemedi", false))
            }
            _state.value = _state.value.copy(isLoading = false)
        }
    }



    fun navigateToQr() {
        viewModelScope.launch {
            _uiEvent.emit(OwneredPostDetailUiEvent.NavigateToQr)
        }
    }

    private fun formatBloodType(bloodType: String): String {
        return when (bloodType) {
            "B-" -> "B_Negative"
            "B+" -> "B_Positive"
            "A-" -> "A_Negative"
            "A+" -> "A_Positive"
            "AB-" -> "AB_Negative"
            "AB+" -> "AB_Positive"
            "O-" -> "O_Negative"
            "O+" -> "O_Positive"
            else -> bloodType
        }
    }
}
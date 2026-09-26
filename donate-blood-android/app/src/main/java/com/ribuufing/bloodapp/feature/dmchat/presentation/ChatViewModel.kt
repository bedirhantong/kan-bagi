package com.ribuufing.bloodapp.feature.dmchat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ribuufing.bloodapp.feature.dmchat.domain.model.GetRoomsResponse
import com.ribuufing.bloodapp.feature.dmchat.domain.usecase.CreateChatUseCase
import com.ribuufing.bloodapp.feature.dmchat.domain.usecase.GetUserChatsUseCase
import com.ribuufing.bloodapp.feature.profile.domain.uistate.GetProfileInfosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getUserChatsUseCase: GetUserChatsUseCase,
    private val getProfileInfosUseCase: GetProfileInfosUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUserProfile()
    }

    private fun loadCurrentUserProfile() {
        viewModelScope.launch {
            try {
                val profileResponse = getProfileInfosUseCase()
                if (profileResponse.isSuccess && profileResponse.response != null) {
                    _uiState.value = _uiState.value.copy(
                        currentUserId = profileResponse.response.id
                    )
                    loadUserChats(profileResponse.response.id.toString())
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Kullanıcı bilgileri yüklenemedi"
                )
            }
        }
    }

    private fun loadUserChats(userId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val response = getUserChatsUseCase(userId)
                
                if (response.isSuccess && response.response != null) {
                    _uiState.value = _uiState.value.copy(
                        chats = response.response,
                        isLoading = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = response.resultMessage ?: "Sohbetler yüklenemedi",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Sohbetler yüklenirken bir hata oluştu",
                    isLoading = false
                )
            }
        }
    }

    fun refresh() {
        _uiState.value.currentUserId?.let { userId ->
            loadUserChats(userId)
        }
    }
}

data class ChatUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val chats: List<GetRoomsResponse> = emptyList(),
    val currentUserId: String? = null
)
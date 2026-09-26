package com.ribuufing.bloodapp.feature.dmchat.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ribuufing.bloodapp.feature.dmchat.data.websocket.ChatWebSocketService
import com.ribuufing.bloodapp.feature.dmchat.data.websocket.WebSocketConnectionState
import com.ribuufing.bloodapp.feature.dmchat.domain.model.LastMessage
import com.ribuufing.bloodapp.feature.dmchat.domain.model.MessageSingle
import com.ribuufing.bloodapp.feature.dmchat.domain.usecase.GetUserChatMessagesUseCase
import com.ribuufing.bloodapp.feature.profile.domain.uistate.GetProfileInfosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.delay

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val getUserChatMessagesUseCase: GetUserChatMessagesUseCase,
    private val getProfileInfosUseCase: GetProfileInfosUseCase,
    private val chatWebSocketService: ChatWebSocketService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatDetailUiState())
    val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()
    
    private var currentRoomId: String? = null

    init {
        loadCurrentUserProfile()
        observeWebSocketMessages()
        observeWebSocketConnection()
    }

    private fun observeWebSocketMessages() {
        viewModelScope.launch {
            chatWebSocketService.messages.collect { message ->
                Log.d("ChatViewModel", "Received message: $message")
                val currentMessages = _uiState.value.messages.toMutableList()
                currentMessages.add(message)
                _uiState.value = _uiState.value.copy(messages = currentMessages)
            }
        }
    }

    private fun observeWebSocketConnection() {
        viewModelScope.launch {
            chatWebSocketService.connectionState.collect { state ->
                Log.d("ChatViewModel", "WebSocket state: $state")
                when (state) {
                    is WebSocketConnectionState.Connected -> {
                        _uiState.value = _uiState.value.copy(
                            isConnected = true,
                            error = null
                        )
                    }
                    is WebSocketConnectionState.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isConnected = false,
                            error = state.message
                        )
                        // Try to reconnect after error
                        delay(5000) // Wait 5 seconds before reconnecting
                        currentRoomId?.let { roomId ->
                            _uiState.value.currentUserId?.let { userId ->
                                chatWebSocketService.connect(roomId, userId)
                            }
                        }
                    }
                    is WebSocketConnectionState.Disconnected -> {
                        _uiState.value = _uiState.value.copy(
                            isConnected = false,
                            error = null
                        )
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(
                            isConnected = false
                        )
                    }
                }
            }
        }
    }

    private fun loadCurrentUserProfile() {
        viewModelScope.launch {
            try {
                val profileResponse = getProfileInfosUseCase()
                if (profileResponse.isSuccess && profileResponse.response != null) {
                    _uiState.value = _uiState.value.copy(
                        currentUserId = profileResponse.response.id
                    )
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to load profile", e)
                _uiState.value = _uiState.value.copy(
                    error = "Kullanıcı bilgileri yüklenemedi"
                )
            }
        }
    }

    fun loadChatMessages(roomId: String) {
        currentRoomId = roomId
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val response = getUserChatMessagesUseCase(roomId, 1, 50)
                
                if (response.isSuccess && response.response != null) {
                    _uiState.value = _uiState.value.copy(
                        messages = response.response.items.orEmpty(),
                        isLoading = false,
                        error = null
                    )
                    
                    // Connect to WebSocket after loading messages
                    val currentUserId = _uiState.value.currentUserId
                    if (currentUserId != null) {
                        Log.d("ChatViewModel", "Connecting to WebSocket with roomId: $roomId, userId: $currentUserId")
                        chatWebSocketService.connect(roomId, currentUserId)
                    } else {
                        Log.e("ChatViewModel", "Cannot connect to WebSocket: currentUserId is null")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = response.resultMessage ?: "Mesajlar yüklenemedi",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to load messages", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Mesajlar yüklenirken bir hata oluştu",
                    isLoading = false
                )
            }
        }
    }

    fun sendMessage(content: String) {
        if (_uiState.value.isConnected) {
            val success = chatWebSocketService.sendMessage(content)
            if (!success) {
                Log.e("ChatViewModel", "Failed to send message")
                _uiState.value = _uiState.value.copy(
                    error = "Mesaj gönderilemedi"
                )
            }
        } else {
            Log.e("ChatViewModel", "Cannot send message: WebSocket not connected")
            _uiState.value = _uiState.value.copy(
                error = "Bağlantı yok, mesaj gönderilemedi"
            )
            // Try to reconnect
            currentRoomId?.let { roomId ->
                _uiState.value.currentUserId?.let { userId ->
                    chatWebSocketService.connect(roomId, userId)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        chatWebSocketService.disconnect()
    }
}

data class ChatDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val messages: List<LastMessage> = emptyList(),
    val currentUserId: String? = null,
    val isConnected: Boolean = false
) 
package com.ribuufing.bloodapp.feature.postdetail.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ribuufing.bloodapp.feature.dmchat.data.CreateRoomRequestBody
import com.ribuufing.bloodapp.feature.dmchat.domain.usecase.CreateChatUseCase
import com.ribuufing.bloodapp.feature.home.data.dto.PostResponse
import com.ribuufing.bloodapp.feature.home.domain.usecase.GetSingleBloodRequestsUseCase
import com.ribuufing.bloodapp.feature.home.domain.usecase.GetUserInfosByIdUseCase
import com.ribuufing.bloodapp.feature.postdetail.data.CreateMatchingRequestBody
import com.ribuufing.bloodapp.feature.postdetail.domain.usecase.CreateMatchingUseCase
import com.ribuufing.bloodapp.feature.postdetail.domain.usecase.DeleteBloodRequestUseCase
import com.ribuufing.bloodapp.feature.profile.domain.uistate.GetProfileInfosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val getSingleBloodRequestsUseCase: GetSingleBloodRequestsUseCase,
    private val deleteBloodRequestUseCase: DeleteBloodRequestUseCase,
    private val createChatUseCase: CreateChatUseCase,
    private val getProfileInfosUseCase: GetProfileInfosUseCase,
    private val getUserInfosByIdUseCase: GetUserInfosByIdUseCase,
    private val createMatchingUseCase: CreateMatchingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUserProfile()
    }

    private fun loadCurrentUserProfile() {
        viewModelScope.launch {
            try {
                val profileResponse = getProfileInfosUseCase()
                if (profileResponse.isSuccess && profileResponse.response != null) {
                    _uiState.value = _uiState.value.copy(
                        currentUserId = profileResponse.response.id,
                        currentUserName = "${profileResponse.response.name} ${profileResponse.response.surname}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Kullanıcı bilgileri yüklenemedi"
                )
            }
        }
    }

    fun createMatching() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val currentPost = uiState.value.post
                val currentUserId = uiState.value.currentUserId
                
                if (currentPost?.id == null || currentUserId == null) {
                    _uiState.value = _uiState.value.copy(
                        error = "Post veya kullanıcı bilgileri bulunamadı",
                        isLoading = false
                    )
                    return@launch
                }

                val requestBody = CreateMatchingRequestBody(
                    donorId = currentUserId,
                    postId = currentPost.id,
                    ownerId = uiState.value.post?.ownerId ?: "0"
                )

                val response = createMatchingUseCase(requestBody)
                
                if (response.success && response.response?.qrBase64 != null) {
                    _uiState.value = _uiState.value.copy(
                        qrCodeBase64 = response.response.qrBase64,
                        showQrDialog = true,
                        isLoading = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = response.resultMessage ?: "QR kodu oluşturulamadı",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "QR kodu oluşturulurken bir hata oluştu",
                    isLoading = false
                )
            }
        }
    }

    fun hideQrDialog() {
        _uiState.value = _uiState.value.copy(
            showQrDialog = false,
            qrCodeBase64 = null
        )
    }

    fun setError(message: String) {
        _uiState.value = _uiState.value.copy(
            error = message
        )
    }

    fun handleChatNavigation() {
        viewModelScope.launch {
            try {
                val currentUserId = uiState.value.currentUserId
                val currentUserName = uiState.value.currentUserName

                if (currentUserId == null || currentUserName == null) {
                    _uiState.value = _uiState.value.copy(
                        error = "Kullanıcı bilgileri bulunamadı",
                        isLoading = false
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                // Get post owner ID from the current post
                val postOwnerId = uiState.value.post?.ownerId
                if (postOwnerId == null) {
                    _uiState.value = _uiState.value.copy(
                        error = "Post sahibi bilgisi bulunamadı",
                        isLoading = false
                    )
                    return@launch
                }

                // Get post owner's name
                val postOwnerInfo = getUserInfosByIdUseCase(postOwnerId)
                val postOwnerName = if (postOwnerInfo.isSuccess && postOwnerInfo.response != null) {
                    "${postOwnerInfo.response.name} ${postOwnerInfo.response.surname}"
                } else {
                    "Kullanıcı"
                }

                // Create chat room
                val requestBody = CreateRoomRequestBody(
                    user1_id = currentUserId,
                    user2_id = postOwnerId,
                    user1_fullname = currentUserName,
                    user2_fullname = postOwnerName
                )

                val response = createChatUseCase(requestBody)
                if (response.isSuccess && response.response?.room_id != null) {
                    _uiState.value = _uiState.value.copy(
                        chatRoomId = response.response.room_id,
                        chatUser1Name = response.response.user1_fullname,
                        chatUser2Name = response.response.user2_fullname,
                        shouldNavigateToChat = true,
                        isLoading = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = response.resultMessage ?: "Sohbet başlatılamadı",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Sohbet başlatılırken bir hata oluştu",
                    isLoading = false
                )
            }
        }
    }

    fun onChatNavigated() {
        _uiState.value = _uiState.value.copy(
            shouldNavigateToChat = false,
            chatRoomId = null,
            chatUser1Name = null,
            chatUser2Name = null
        )
    }

    fun loadPostDetail(postId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val response = getSingleBloodRequestsUseCase(postId)
                
                if (response.isSuccess && response.response != null) {
                    _uiState.value = _uiState.value.copy(
                        post = response.response,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = response.resultMessage ?: "Bir hata oluştu",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Bir hata oluştu",
                    isLoading = false
                )
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val response = deleteBloodRequestUseCase(postId)
                
                if (response.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isDeleted = true,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = response.resultMessage ?: "Silme işlemi başarısız oldu",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Silme işlemi sırasında bir hata oluştu",
                    isLoading = false
                )
            }
        }
    }
}

data class PostDetailUiState(
    val post: PostResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeleted: Boolean = false,
    val chatRoomId: String? = null,
    val chatUser1Name: String? = null,
    val chatUser2Name: String? = null,
    val shouldNavigateToChat: Boolean = false,
    val currentUserId: String? = null,
    val currentUserName: String? = null,
    val qrCodeBase64: String? = null,
    val showQrDialog: Boolean = false
) 
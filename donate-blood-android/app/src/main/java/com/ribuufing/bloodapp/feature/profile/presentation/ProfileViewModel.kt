package com.ribuufing.bloodapp.feature.profile.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ribuufing.bloodapp.feature.home.data.dto.PostResponse
import com.ribuufing.bloodapp.feature.home.data.dto.SetPostActivenessRequestBody
import com.ribuufing.bloodapp.feature.home.domain.usecase.GetAllBloodRequestsUseCase
import com.ribuufing.bloodapp.feature.home.domain.usecase.GetDonatedBloodRequestsByUseIdUseCase
import com.ribuufing.bloodapp.feature.home.domain.usecase.GetUserActivePostUseCase
import com.ribuufing.bloodapp.feature.home.domain.usecase.SetPostActivenessUseCase
import com.ribuufing.bloodapp.feature.profile.domain.uistate.GetProfileInfosUseCase
import com.ribuufing.bloodapp.utils.loading.LoadingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileInfosUseCase: GetProfileInfosUseCase,
    private val getAllBloodRequestsUseCase: GetAllBloodRequestsUseCase,
    private val getUserActivePostUseCase: GetUserActivePostUseCase,
    private val setPostActivenessUseCase: SetPostActivenessUseCase,
    private val getDonatedBloodRequestsByUseIdUseCase: GetDonatedBloodRequestsByUseIdUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    private var _userBloodRequests: Flow<PagingData<PostResponse>>? = null
    val userBloodRequests: Flow<PagingData<PostResponse>>
        get() = _userBloodRequests ?: emptyFlow()

    private val _userActivePost = MutableStateFlow<PostResponse?>(null)
    val userActivePost: StateFlow<PostResponse?> = _userActivePost.asStateFlow()
    
    private val _userDonatedBloodRequests = MutableStateFlow<List<PostResponse>>(emptyList())
    val userDonatedBloodRequests: StateFlow<List<PostResponse>> = _userDonatedBloodRequests.asStateFlow()

    private var _userInactivePosts: Flow<PagingData<PostResponse>>? = null
    val userInactivePosts: Flow<PagingData<PostResponse>>
        get() = _userInactivePosts ?: emptyFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent

    init {
        loadProfileInfo()
    }

    private fun loadProfileInfo() {
        viewModelScope.launch {
            try {
                LoadingManager.show()
                val response = getProfileInfosUseCase()
                if (response.isSuccess && response.response != null) {
                    val profile = response.response
                    _state.update { currentState ->
                        currentState.copy(
                            name = profile.name ?: "",
                            surname = profile.surname ?: "",
                            phoneNumber = profile.phoneNumber ?: "",
                            userId = profile.id ?: "",
                            isLoading = false,
                            error = null
                        )
                    }

                    initializeActivePost(profile.id ?: "")
                    Log.d("Donations", "Initializing donated requests for user: ${profile.id}")
                    initializeUserDonatedRequests(profile.id ?: "")
                    initializeInactivePostsPaging(profile.id ?: "")
                } else {
                    _state.update { it.copy(error = response.resultMessage, isLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            } finally {
                LoadingManager.hide()
            }
        }
    }

    private fun initializeActivePost(userId: String) {
        viewModelScope.launch {
            try {
                val response = getUserActivePostUseCase(userId)
                if (response.isSuccess && response.response != null && response.response.isActive == true) {
                    _userActivePost.value = response.response
                } else {
                    _userActivePost.value = null
                }
            } catch (e: Exception) {
                _userActivePost.value = null
            }
        }
    }
    
    private fun initializeUserDonatedRequests(userId: String){
        viewModelScope.launch {
            try {
                // Now the API directly returns List<PostResponse>
                val donatedPosts = getDonatedBloodRequestsByUseIdUseCase(userId)
                Log.d("Donations", "Response received directly as list, size: ${donatedPosts.size}")
                _userDonatedBloodRequests.value = donatedPosts
            } catch (e: Exception) {
                Log.e("Donations", "Error loading donations", e)
                _userDonatedBloodRequests.value = emptyList()
            }
        }
    }

    private fun initializeInactivePostsPaging(userId: String) {
        _userInactivePosts = Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
                initialLoadSize = 10
            )
        ) {
            ProfileBloodRequestPagingSource(
                getAllBloodRequestsUseCase = getAllBloodRequestsUseCase,
                userId = userId,
                onlyInactive = true
            )
        }.flow.cachedIn(viewModelScope)
    }

    fun republishPost(postId: String) {
        viewModelScope.launch {
            try {
                LoadingManager.show()
                val response = setPostActivenessUseCase(
                    SetPostActivenessRequestBody(
                        postId = postId,
                        isActive = true
                    )
                )
                _userActivePost.value = if (response.isSuccess) response.response else null
                refresh()
                _uiEvent.emit(UiEvent.ShowMessage(
                    message = response.resultMessage ?: if (response.isSuccess) "Başarılı" else "Bir hata oluştu",
                    isSuccess = response.isSuccess
                ))
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
                _uiEvent.emit(UiEvent.ShowMessage(e.message ?: "Bir hata oluştu", isSuccess = false))
            } finally {
                LoadingManager.hide()
            }
        }
    }

    fun refresh() {
        _state.update { it.copy(isLoading = true, error = null) }
        loadProfileInfo()
    }
}

data class ProfileUiState(
    val name: String = "",
    val surname: String = "",
    val phoneNumber: String = "",
    val userId: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed class UiEvent {
    data class ShowMessage(val message: String, val isSuccess: Boolean): UiEvent()
}
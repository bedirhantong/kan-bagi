package com.ribuufing.bloodapp.feature.home.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ribuufing.bloodapp.core.data.LoadingState
import com.ribuufing.bloodapp.core.data.LoadingStateCollector
import com.ribuufing.bloodapp.core.data.Visibility
import com.ribuufing.bloodapp.feature.home.data.dto.PostResponse
import com.ribuufing.bloodapp.feature.home.data.paging.BloodRequestPagingSource
import com.ribuufing.bloodapp.feature.home.domain.usecase.GetAllBloodRequestsUseCase
import com.ribuufing.bloodapp.feature.home.domain.usecase.GetUserActivePostUseCase
import com.ribuufing.bloodapp.feature.home.presentation.StoryBoard
import com.ribuufing.bloodapp.feature.listhospitals.domain.GetAllHospitalsUseCase
import com.ribuufing.bloodapp.feature.profile.domain.uistate.GetProfileInfosUseCase
import com.ribuufing.bloodapp.utils.loading.LoadingManager
import com.ribuufing.bloodapp.feature.home.domain.usecase.GetStoriesFromContentfulUseCase
import com.ribuufing.bloodapp.feature.sharepost.domain.mapper.HospitalMapper.toHospitalList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllBloodRequestsUseCase: GetAllBloodRequestsUseCase,
    private val getUserActivePostUseCase: GetUserActivePostUseCase,
    private val getProfileInfosUseCase: GetProfileInfosUseCase,
    private val getAllHospitalsUseCase: GetAllHospitalsUseCase,
    private val getStoriesFromContentfulUseCase: GetStoriesFromContentfulUseCase
) : ViewModel(), LoadingStateCollector {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    override val loadingStateFlow = MutableStateFlow(LoadingState(show = Visibility.DEFAULT))

    private val _filterTrigger = MutableStateFlow(0)

    val bloodRequests: Flow<PagingData<PostResponse>> = _filterTrigger.flatMapLatest {
        Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
                initialLoadSize = 10
            )
        ) {
            BloodRequestPagingSource(
                getAllBloodRequestsUseCase = getAllBloodRequestsUseCase,
                hospitals = _uiState.value.selectedHospitals,
                bloodTypes = _uiState.value.selectedBloodTypes,
                sorting = _uiState.value.selectedSorting
            )
        }.flow.cachedIn(viewModelScope)
    }

    private suspend fun loadHospitals() {
        try {
            val response = getAllHospitalsUseCase()
            if (response.isSuccess && response.response != null) {
                _uiState.update { it.copy(
                    availableHospitals = response.response.items?.toHospitalList() ?: emptyList()
                ) }
            } else {
                _uiState.update { it.copy(
                    availableHospitals = emptyList(),
                    error = response.resultMessage
                ) }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(
                availableHospitals = emptyList(),
                error = e.message ?: "Hastaneler yüklenirken bir hata oluştu"
            ) }
        }
    }

    private suspend fun loadStories() {
        Log.d("ContentfulDebug", "HomeViewModel.loadStories() started")
        try {
            val stories: List<StoryBoard> = getStoriesFromContentfulUseCase().map {
                StoryBoard(
                    id = it.id,
                    icon = it.userIconUrl ?: "",
                    storyImage = it.storyImageUrl ?: "",
                    name = it.name ?: "",
                    description = it.description ?: "",
                    webUrl = it.webUrl
                )
            }
            Log.d("ContentfulDebug", "HomeViewModel.loadStories() success, story count: ${stories.size}")
            _uiState.update { it.copy(
                stories = stories,
                isLoading = false
            ) }
        } catch (e: Exception) {
            Log.e("ContentfulDebug", "HomeViewModel.loadStories() error: ${e.message}", e)
            _uiState.update { it.copy(
                stories = emptyList(),
                isLoading = false,
                error = e.message ?: "Hikayeler yüklenirken bir hata oluştu"
            ) }
        }
    }

    private suspend fun loadActiveBloodRequest() {
        try {
            val profileResponse = getProfileInfosUseCase()
            if (profileResponse.isSuccess && profileResponse.response?.id != null) {
                val userId = profileResponse.response.id
                val activePostResponse = getUserActivePostUseCase(userId)
                _uiState.update { it.copy(userid = userId) }

                if (activePostResponse.isSuccess && activePostResponse.response != null) {
                    _uiState.update { it.copy(
                        activePost = activePostResponse.response,
                        error = null
                    ) }
                } else {
                    _uiState.update { it.copy(
                        activePost = null,
                        error = activePostResponse.resultMessage
                    ) }
                }
            } else {
                _uiState.update { it.copy(
                    activePost = null,
                    error = "Kullanıcı bilgileri alınamadı"
                ) }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(
                activePost = null,
                error = e.message ?: "Aktif post yüklenirken bir hata oluştu"
            ) }
        }
    }

    fun refresh() {
        Log.d("ContentfulDebug", "HomeViewModel.refresh() called")
        viewModelScope.launch {
            try {
                LoadingManager.show()
                _uiState.update { it.copy(isLoading = true) }

                // Run all data loading operations in parallel but wait for all to complete
                supervisorScope {
                    val deferredStories = async { loadStories() }
                    val deferredHospitals = async { loadHospitals() }
                    val deferredActivePost = async { loadActiveBloodRequest() }
                    
                    try { deferredStories.await() } catch (e: Exception) { 
                        Log.e("HomeViewModel", "Failed to load stories", e) 
                    }
                    try { deferredHospitals.await() } catch (e: Exception) { 
                        Log.e("HomeViewModel", "Failed to load hospitals", e) 
                    }
                    try { deferredActivePost.await() } catch (e: Exception) { 
                        Log.e("HomeViewModel", "Failed to load active post", e) 
                    }
                }
            } finally {
                LoadingManager.hide()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun setFilters(hospitals: List<Int>, bloodTypes: List<String>) {
        _uiState.value = _uiState.value.copy(
            selectedHospitals = hospitals,
            selectedBloodTypes = bloodTypes
        )
        _filterTrigger.value++
    }

    fun setSorting(sorting: String) {
        _uiState.value = _uiState.value.copy(selectedSorting = sorting)
        _filterTrigger.value++
    }
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    var userid: String = "",
    val stories: List<StoryBoard> = emptyList(),
    val availableHospitals: List<com.ribuufing.bloodapp.feature.sharepost.domain.model.Hospital> = emptyList(),
    val activePost: PostResponse? = null,
    val lastRefreshTime: Long = System.currentTimeMillis(),
    val shouldRefreshOnResume: Boolean = true,
    val selectedHospitals: List<Int> = emptyList(),
    val selectedBloodTypes: List<String> = emptyList(),
    val selectedSorting: String = "Newest"
)
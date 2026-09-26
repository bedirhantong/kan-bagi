package com.ribuufing.bloodapp.feature.home.domain.usecase

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.home.data.dto.AllPostsResponse
import com.ribuufing.bloodapp.feature.home.domain.HomeRepository
import javax.inject.Inject

class GetAllBloodRequestsUseCase @Inject constructor(
    private val homeRepository: HomeRepository
) {
    suspend operator fun invoke(
        userId: String? = null,
        hospitals: List<Int>? = null,
        bloodTypes: List<String>? = null,
        showingResultsFrom: Int = 0,
        paging: Int = 10,
        sorting: String = "Newest"
    ): BaseResponse<AllPostsResponse> {
        return homeRepository.getAllBloodRequests(
            userId = userId.takeIf { !it.isNullOrBlank() },
            hospitals = hospitals.takeIf { !it.isNullOrEmpty() },
            bloodTypes = bloodTypes.takeIf { !it.isNullOrEmpty() },
            showingResultsFrom = showingResultsFrom,
            paging = paging,
            sorting = sorting
        )
    }
}
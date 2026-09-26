package com.ribuufing.bloodapp.feature.home.domain.usecase

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.home.data.dto.PostResponse
import com.ribuufing.bloodapp.feature.home.domain.HomeRepository
import javax.inject.Inject

class GetUserActivePostUseCase @Inject constructor(
    private val homeRepository: HomeRepository
) {
    suspend operator fun invoke(userId: String) : BaseResponse<PostResponse> {
        return homeRepository.getUserActivePost(userId)
    }
}
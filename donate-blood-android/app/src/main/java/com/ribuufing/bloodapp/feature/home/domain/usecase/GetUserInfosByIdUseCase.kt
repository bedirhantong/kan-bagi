package com.ribuufing.bloodapp.feature.home.domain.usecase

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.profile.domain.repository.ProfileRepository
import com.ribuufing.bloodapp.feature.profile.domain.response.GetProfileResponse
import javax.inject.Inject

class GetUserInfosByIdUseCase @Inject constructor(
    private val repository: ProfileRepository
){
    suspend operator fun invoke(id: String) : BaseResponse<GetProfileResponse> {
        return repository.getUserInfosById(id)
    }
}
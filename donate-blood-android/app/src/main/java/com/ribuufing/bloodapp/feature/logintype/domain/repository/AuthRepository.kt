package com.ribuufing.bloodapp.feature.logintype.domain.repository

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.authentication.domain.model.response.IsCompleteResponse
import com.ribuufing.bloodapp.feature.logintype.domain.model.request.CompleteProfileRequest
import com.ribuufing.bloodapp.feature.profile.domain.response.GetProfileResponse

interface AuthRepository {
    suspend fun isProfileCompleted(): BaseResponse<IsCompleteResponse>
    suspend fun completeProfile(
        completeProfileRequest: CompleteProfileRequest
    ): BaseResponse<GetProfileResponse>
}
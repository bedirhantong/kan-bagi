package com.ribuufing.bloodapp.feature.logintype.data.remote

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.authentication.domain.model.response.IsCompleteResponse
import com.ribuufing.bloodapp.feature.logintype.domain.repository.AuthRepository
import com.ribuufing.bloodapp.feature.logintype.domain.model.request.CompleteProfileRequest
import com.ribuufing.bloodapp.feature.profile.domain.response.GetProfileResponse
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: LoginApi
) : AuthRepository {
    override suspend fun isProfileCompleted(): BaseResponse<IsCompleteResponse> {
        return api.isProfileCompleted()
    }

    override suspend fun completeProfile(completeProfileRequest: CompleteProfileRequest): BaseResponse<GetProfileResponse> {
        return api.completeProfile(completeProfileRequest)
    }
}
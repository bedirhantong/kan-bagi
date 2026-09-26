package com.ribuufing.bloodapp.feature.profile.domain.repository

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.profile.domain.response.GetProfileResponse

interface ProfileRepository {
    suspend fun getProfileInfos(): BaseResponse<GetProfileResponse>
    suspend fun getUserInfosById(id: String): BaseResponse<GetProfileResponse>
}
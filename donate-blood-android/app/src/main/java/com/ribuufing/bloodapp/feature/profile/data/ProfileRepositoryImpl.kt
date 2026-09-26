package com.ribuufing.bloodapp.feature.profile.data

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.profile.data.remote.ProfileApi
import com.ribuufing.bloodapp.feature.profile.domain.repository.ProfileRepository
import com.ribuufing.bloodapp.feature.profile.domain.response.GetProfileResponse
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val api : ProfileApi
) : ProfileRepository{

    override suspend fun getProfileInfos(): BaseResponse<GetProfileResponse> {
        return api.getProfileDetails()
    }

    override suspend fun getUserInfosById(id: String): BaseResponse<GetProfileResponse> {
        return api.getUserProfileDetailsById(id)
    }
}
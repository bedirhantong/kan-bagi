package com.ribuufing.bloodapp.feature.logintype.data.remote

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.authentication.domain.model.response.IsCompleteResponse
import com.ribuufing.bloodapp.feature.logintype.domain.model.request.CompleteProfileRequest
import com.ribuufing.bloodapp.feature.profile.domain.response.GetProfileResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface LoginApi {

    @GET(IS_PROFILE_COMPLETED_URL)
    suspend fun isProfileCompleted(): BaseResponse<IsCompleteResponse>

    @POST(COMPLETE_PROFILE_URL)
    suspend fun completeProfile(
        @Body requestBody: CompleteProfileRequest
    ) : BaseResponse<GetProfileResponse>

    private companion object {

        const val COMPLETE_PROFILE_URL = "profile/complete-profile"
        const val IS_PROFILE_COMPLETED_URL ="Profile/is-profile-completed"
    }
}
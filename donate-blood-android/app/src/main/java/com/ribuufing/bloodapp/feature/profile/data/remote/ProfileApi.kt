package com.ribuufing.bloodapp.feature.profile.data.remote

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.profile.domain.response.GetProfileResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ProfileApi {
   @GET(GET_PROFILE_URL)
   suspend fun getProfileDetails(): BaseResponse<GetProfileResponse>

   @GET(GET_USER_PROFILE_URL)
   suspend fun getUserProfileDetailsById(
       @Query("UserId", encoded = true) postId: String
   ): BaseResponse<GetProfileResponse>

    private companion object {
        const val GET_PROFILE_URL ="Profile/get-session-info"
        const val GET_USER_PROFILE_URL ="Profile/get-user-info"
    }
}


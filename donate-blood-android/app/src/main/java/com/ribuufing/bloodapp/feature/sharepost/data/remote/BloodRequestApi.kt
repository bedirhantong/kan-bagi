package com.ribuufing.bloodapp.feature.sharepost.data.remote

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.home.data.dto.PostResponse
import com.ribuufing.bloodapp.feature.sharepost.data.model.CreateBloodRequestBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface BloodRequestApi {
    @POST(CREATE_POST)
    @Headers("Accept: application/json")
    suspend fun createPost(
        @Body requestBody: CreateBloodRequestBody
    ): BaseResponse<PostResponse>

    private companion object {
        const val CREATE_POST = "Post/create-post"
    }
}
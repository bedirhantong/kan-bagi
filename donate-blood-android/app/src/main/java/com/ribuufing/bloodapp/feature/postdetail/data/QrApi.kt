package com.ribuufing.bloodapp.feature.postdetail.data

import com.ribuufing.bloodapp.feature.postdetail.domain.response.ValidateMatchingResponse
import com.ribuufing.bloodapp.feature.postdetail.domain.usecase.CreateMatchingResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface QrApi {

    @POST(CREATE_MATCHING)
    suspend fun createMatching(
        @Body body: CreateMatchingRequestBody
    ) :QrBaseResponse<CreateMatchingResponse>

    @POST(VALIDATE_MATCHING)
    suspend fun validateMatching(
        @Body body: ValidateRequestBody
    ) :QrBaseResponse<ValidateMatchingResponse>

    private companion object {
        const val CREATE_MATCHING = "matching/create"
        const val VALIDATE_MATCHING = "matching/validate"
    }
}
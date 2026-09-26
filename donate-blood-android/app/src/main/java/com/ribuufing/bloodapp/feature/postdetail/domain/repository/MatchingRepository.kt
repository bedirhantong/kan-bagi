package com.ribuufing.bloodapp.feature.postdetail.domain.repository

import com.ribuufing.bloodapp.feature.postdetail.data.CreateMatchingRequestBody
import com.ribuufing.bloodapp.feature.postdetail.data.QrBaseResponse
import com.ribuufing.bloodapp.feature.postdetail.data.ValidateRequestBody
import com.ribuufing.bloodapp.feature.postdetail.domain.response.ValidateMatchingResponse
import com.ribuufing.bloodapp.feature.postdetail.domain.usecase.CreateMatchingResponse

interface MatchingRepository {
    suspend fun createMatching(
        body: CreateMatchingRequestBody
    ) :QrBaseResponse<CreateMatchingResponse>

    suspend fun validateMatching(
        body: ValidateRequestBody
    ):QrBaseResponse<ValidateMatchingResponse>
}
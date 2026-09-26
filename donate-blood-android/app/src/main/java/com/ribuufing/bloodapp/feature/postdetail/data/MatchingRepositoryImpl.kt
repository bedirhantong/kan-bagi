package com.ribuufing.bloodapp.feature.postdetail.data

import com.ribuufing.bloodapp.feature.postdetail.domain.repository.MatchingRepository
import com.ribuufing.bloodapp.feature.postdetail.domain.response.ValidateMatchingResponse
import com.ribuufing.bloodapp.feature.postdetail.domain.usecase.CreateMatchingResponse
import javax.inject.Inject

class MatchingRepositoryImpl @Inject constructor(
    private val qrApi: QrApi
): MatchingRepository{
    override suspend fun createMatching(body: CreateMatchingRequestBody): QrBaseResponse<CreateMatchingResponse> {
        return qrApi.createMatching(body)
    }

    override suspend fun validateMatching(body: ValidateRequestBody): QrBaseResponse<ValidateMatchingResponse> {
        return qrApi.validateMatching(body)
    }
}
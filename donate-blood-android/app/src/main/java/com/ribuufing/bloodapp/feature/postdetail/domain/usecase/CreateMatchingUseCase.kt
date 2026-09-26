package com.ribuufing.bloodapp.feature.postdetail.domain.usecase

import com.ribuufing.bloodapp.feature.postdetail.data.CreateMatchingRequestBody
import com.ribuufing.bloodapp.feature.postdetail.data.QrBaseResponse
import com.ribuufing.bloodapp.feature.postdetail.domain.repository.MatchingRepository
import javax.inject.Inject

class CreateMatchingUseCase @Inject constructor(
    private val matchingRepository: MatchingRepository
){
    suspend operator fun invoke( body: CreateMatchingRequestBody) :QrBaseResponse<CreateMatchingResponse>
    {
        return matchingRepository.createMatching(body)
    }
}
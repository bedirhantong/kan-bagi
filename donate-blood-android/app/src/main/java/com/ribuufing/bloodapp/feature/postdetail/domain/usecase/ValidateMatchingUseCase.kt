package com.ribuufing.bloodapp.feature.postdetail.domain.usecase

import com.ribuufing.bloodapp.feature.postdetail.data.QrBaseResponse
import com.ribuufing.bloodapp.feature.postdetail.data.ValidateRequestBody
import com.ribuufing.bloodapp.feature.postdetail.domain.repository.MatchingRepository
import com.ribuufing.bloodapp.feature.postdetail.domain.response.ValidateMatchingResponse
import javax.inject.Inject

class ValidateMatchingUseCase @Inject constructor(
    private val matchingRepository: MatchingRepository
){
    suspend operator fun invoke(
        body: ValidateRequestBody
    ):QrBaseResponse<ValidateMatchingResponse>{
        return matchingRepository.validateMatching(body)
    }
}
package com.ribuufing.bloodapp.feature.sharepost.domain.usecase

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.home.data.dto.PostResponse
import com.ribuufing.bloodapp.feature.sharepost.data.model.CreateBloodRequestBody
import com.ribuufing.bloodapp.feature.sharepost.domain.BloodRequestRepository
import javax.inject.Inject

class CreateBloodRequestUseCase @Inject constructor(
    private val bloodRequestRepository: BloodRequestRepository
) {
    suspend operator fun invoke(bloodRequest: CreateBloodRequestBody) : BaseResponse<PostResponse> {
        return bloodRequestRepository.createBloodRequest(bloodRequest)
    }
}
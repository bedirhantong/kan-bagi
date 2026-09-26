package com.ribuufing.bloodapp.feature.postdetail.domain.usecase

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.home.domain.HomeRepository
import javax.inject.Inject

class DeleteBloodRequestUseCase @Inject constructor(
    private val homeRepository: HomeRepository
) {
    suspend operator fun invoke(id: String) : BaseResponse<Boolean> {
       return homeRepository.deleteBloodRequest(id)
    }
}
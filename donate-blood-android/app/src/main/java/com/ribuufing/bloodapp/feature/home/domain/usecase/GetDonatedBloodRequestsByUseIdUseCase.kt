package com.ribuufing.bloodapp.feature.home.domain.usecase

import com.ribuufing.bloodapp.feature.home.data.dto.PostResponse
import com.ribuufing.bloodapp.feature.home.domain.HomeRepository
import javax.inject.Inject

class GetDonatedBloodRequestsByUseIdUseCase  @Inject constructor(
    private val homeRepository: HomeRepository
) {
    suspend operator fun invoke(
        userId: String
    ): List<PostResponse> {
        return homeRepository.getUserDonatedPsst(
            userId = userId,
        )
    }
}
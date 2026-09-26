package com.ribuufing.bloodapp.feature.listhospitals.domain

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.listhospitals.data.HospitalListResponse
import javax.inject.Inject

class GetAllHospitalsUseCase @Inject constructor(
    private val repository: HospitalRepository
) {
    suspend operator fun invoke() : BaseResponse<HospitalListResponse> {
        return repository.getHospitals()
    }
}
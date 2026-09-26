package com.ribuufing.bloodapp.feature.listhospitals.domain

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.listhospitals.data.HospitalResponse
import javax.inject.Inject

class GetHospitalUseCase @Inject constructor(
    private val repository: HospitalRepository
) {
    suspend operator fun invoke(id : Int) :  BaseResponse<HospitalResponse> {
        return repository.getHospitalById(id)
    }
}
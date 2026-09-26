package com.ribuufing.bloodapp.feature.listhospitals.data

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.core.data.ApiExecutor
import com.ribuufing.bloodapp.feature.listhospitals.domain.HospitalRepository
import javax.inject.Inject

class HospitalRepositoryImpl @Inject constructor(
    private val hospitalApi: HospitalApi
)  : HospitalRepository, ApiExecutor{
    override suspend fun getHospitals(): BaseResponse<HospitalListResponse> {
        return hospitalApi.getAllHospitals()
    }

    override suspend fun getHospitalById(id: Int): BaseResponse<HospitalResponse> {
        return hospitalApi.getHospital(id)
    }
}
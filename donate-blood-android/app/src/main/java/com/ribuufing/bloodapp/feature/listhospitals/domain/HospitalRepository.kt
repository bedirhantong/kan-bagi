package com.ribuufing.bloodapp.feature.listhospitals.domain

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.listhospitals.data.HospitalListResponse
import com.ribuufing.bloodapp.feature.listhospitals.data.HospitalResponse

interface HospitalRepository {
    suspend fun getHospitals(): BaseResponse<HospitalListResponse>

    suspend fun getHospitalById(id: Int): BaseResponse<HospitalResponse>
}
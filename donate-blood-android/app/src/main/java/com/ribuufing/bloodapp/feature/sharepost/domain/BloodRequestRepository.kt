package com.ribuufing.bloodapp.feature.sharepost.domain

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.home.data.dto.PostResponse
import com.ribuufing.bloodapp.feature.sharepost.data.model.CreateBloodRequestBody

interface BloodRequestRepository {
    suspend fun createBloodRequest (request: CreateBloodRequestBody) : BaseResponse<PostResponse>

}
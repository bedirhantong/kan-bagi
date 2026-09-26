package com.ribuufing.bloodapp.feature.sharepost.data.repository

import android.util.Log
import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.core.data.ApiExecutor
import com.ribuufing.bloodapp.feature.home.data.dto.PostResponse
import com.ribuufing.bloodapp.feature.sharepost.data.model.CreateBloodRequestBody
import com.ribuufing.bloodapp.feature.sharepost.data.remote.BloodRequestApi
import com.ribuufing.bloodapp.feature.sharepost.domain.BloodRequestRepository
import javax.inject.Inject

class BloodRequestRepositoryImpl @Inject constructor(
    private val api: BloodRequestApi
) : BloodRequestRepository, ApiExecutor {
    override suspend fun createBloodRequest(request: CreateBloodRequestBody): BaseResponse<PostResponse> {
        return try {
            val response = api.createPost(request)
            Log.d("BloodRequestRepo", "API Response: isSuccess=${response.isSuccess}, resultCode=${response.resultCode}, resultMessage=${response.resultMessage}")
            response
        } catch (e: Exception) {
            Log.e("BloodRequestRepo", "API Error: ${e.message}", e)
            BaseResponse(
                response = null,
                isSuccess = false,
                resultMessage = e.message ?: "Unexpected error occurred",
                resultCode = "ERROR"
            )
        }
    }
}
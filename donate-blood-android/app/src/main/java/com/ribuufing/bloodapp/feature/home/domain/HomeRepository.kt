package com.ribuufing.bloodapp.feature.home.domain

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.home.data.dto.AllPostsResponse
import com.ribuufing.bloodapp.feature.home.data.dto.PostResponse
import com.ribuufing.bloodapp.feature.home.data.dto.SetPostActivenessRequestBody
import com.ribuufing.bloodapp.feature.home.data.dto.UpdatePostRequestBody
import android.util.Log

interface HomeRepository {
    suspend fun getAllBloodRequests (
        userId: String? = null,
        hospitals: List<Int>? = null,
        bloodTypes: List<String>? = null,
        showingResultsFrom: Int = 1,
        paging: Int = 10,
        sorting: String = "Newest"
    ) : BaseResponse<AllPostsResponse>

    suspend fun getUserDonatedPsst(
        userId: String
    ) : List<PostResponse>

    suspend fun getSingleBloodRequest (
        postId: String
    ) : BaseResponse<PostResponse>

    suspend fun deleteBloodRequest (
        postId: String
    ) : BaseResponse<Boolean>

    suspend fun getUserActivePost (
        userId: String
    ) : BaseResponse<PostResponse>

    suspend fun updateBloodRequest (
        requestBody: UpdatePostRequestBody
    ): BaseResponse<PostResponse>

    suspend fun setPostActiveness(
        requestBody: SetPostActivenessRequestBody
    ) :BaseResponse<PostResponse>

    // Contentful stories
    suspend fun getStoriesFromContentful(): List<com.ribuufing.bloodapp.feature.home.data.dto.ContentfulStoryDto> {
        Log.d("ContentfulDebug", "HomeRepository.getStoriesFromContentful() called")
        return emptyList() // Sadece interface, gerçek implementasyon repositoryde
    }
}
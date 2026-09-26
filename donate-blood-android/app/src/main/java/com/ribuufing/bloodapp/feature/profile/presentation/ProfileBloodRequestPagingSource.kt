package com.ribuufing.bloodapp.feature.profile.presentation

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ribuufing.bloodapp.feature.home.data.dto.PostResponse
import com.ribuufing.bloodapp.feature.home.domain.usecase.GetAllBloodRequestsUseCase

class ProfileBloodRequestPagingSource(
    private val getAllBloodRequestsUseCase: GetAllBloodRequestsUseCase,
    private val userId: String,
    private val onlyInactive: Boolean = false
) : PagingSource<Int, PostResponse>() {

    override fun getRefreshKey(state: PagingState<Int, PostResponse>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(10) ?: anchorPage?.nextKey?.minus(10)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PostResponse> {
        return try {
            val offset = params.key ?: 0
            
            val response = getAllBloodRequestsUseCase(
                userId = userId,
                showingResultsFrom = offset,
                paging = params.loadSize
            )

            if (response.isSuccess && response.response != null) {
                var posts = response.response.items ?: emptyList()
                if (onlyInactive) {
                    posts = posts.filter { it.isActive == false }
                }
                val nextKey = if (posts.size < params.loadSize) null else offset + params.loadSize
                val prevKey = if (offset == 0) null else offset - params.loadSize

                LoadResult.Page(
                    data = posts,
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            } else {
                LoadResult.Error(Exception(response.resultMessage ?: "Unknown error"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
} 
package com.ribuufing.bloodapp.feature.home.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ribuufing.bloodapp.feature.home.data.dto.PostResponse
import com.ribuufing.bloodapp.feature.home.domain.usecase.GetAllBloodRequestsUseCase

class BloodRequestPagingSource(
    private val getAllBloodRequestsUseCase: GetAllBloodRequestsUseCase,
    private val hospitals: List<Int> = emptyList(),
    private val bloodTypes: List<String> = emptyList(),
    private val sorting: String = "Newest"
) : PagingSource<Int, PostResponse>() {

    override fun getRefreshKey(state: PagingState<Int, PostResponse>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(10) ?: anchorPage?.nextKey?.minus(10)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PostResponse> {
        return try {
            // offset değeri, eğer null ise 0'dan başla
            val offset = params.key ?: 0
            
            val response = getAllBloodRequestsUseCase(
                hospitals = if(hospitals.isNotEmpty()) hospitals else null,
                bloodTypes = if (bloodTypes.isNotEmpty()) bloodTypes else null,
                showingResultsFrom = offset, // 0'dan başlıyoruz
                paging = params.loadSize,
                sorting = sorting
            )

            if (response.isSuccess && response.response != null) {
                val posts = response.response.items ?: emptyList()
                
                // Eğer gelen liste boşsa veya istenen sayıdan az eleman varsa
                // sonraki sayfa null olmalı
                val nextKey = if (posts.size < params.loadSize) null else offset + params.loadSize
                
                // Önceki sayfa için offset değeri
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
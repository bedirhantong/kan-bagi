package com.ribuufing.bloodapp.feature.home.domain.usecase

import com.ribuufing.bloodapp.feature.home.data.dto.ContentfulStoryDto
import com.ribuufing.bloodapp.feature.home.domain.HomeRepository
import javax.inject.Inject
import android.util.Log

class GetStoriesFromContentfulUseCase @Inject constructor(
    private val homeRepository: HomeRepository
) {
    suspend operator fun invoke(): List<ContentfulStoryDto> {
        Log.d("ContentfulDebug", "GetStoriesFromContentfulUseCase invoked")
        val result = homeRepository.getStoriesFromContentful()
        Log.d("ContentfulDebug", "GetStoriesFromContentfulUseCase result size: ${'$'}{result.size}")
        return result
    }
} 
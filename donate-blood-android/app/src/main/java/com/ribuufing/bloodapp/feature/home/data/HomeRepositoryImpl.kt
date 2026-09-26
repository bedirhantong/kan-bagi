package com.ribuufing.bloodapp.feature.home.data

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.core.data.ApiExecutor
import com.ribuufing.bloodapp.feature.home.data.dto.AllPostsResponse
import com.ribuufing.bloodapp.feature.home.data.dto.PostResponse
import com.ribuufing.bloodapp.feature.home.data.dto.SetPostActivenessRequestBody
import com.ribuufing.bloodapp.feature.home.data.dto.UpdatePostRequestBody
import com.ribuufing.bloodapp.feature.home.data.remote.HomeApi
import com.ribuufing.bloodapp.feature.home.domain.HomeRepository
import javax.inject.Inject
import com.ribuufing.bloodapp.BuildConfig
import com.contentful.java.cda.CDAClient
import com.ribuufing.bloodapp.feature.home.data.dto.ContentfulStoryDto
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.contentful.java.cda.CDAEntry
import com.contentful.java.cda.CDAAsset

class HomeRepositoryImpl @Inject constructor(
    private val homeApi: HomeApi
) : HomeRepository, ApiExecutor {
    override suspend fun getAllBloodRequests(
        userId: String?,
        hospitals: List<Int>?,
        bloodTypes: List<String>?,
        showingResultsFrom: Int,
        paging: Int,
        sorting: String
    ): BaseResponse<AllPostsResponse> {
        return homeApi.getAllPosts(
            userId = userId.takeIf { !it.isNullOrBlank() },
            hospitals = hospitals.takeIf { !it.isNullOrEmpty() },
            bloodTypes = bloodTypes.takeIf { !it.isNullOrEmpty() },
            showingResultsFrom = showingResultsFrom,
            paging = paging,
            sorting = sorting
        )
    }

    override suspend fun getUserDonatedPsst(userId: String): List<PostResponse> {
        return homeApi.getUserDonatedPosts(userId)
    }

    override suspend fun getSingleBloodRequest(postId: String): BaseResponse<PostResponse> {
        return homeApi.getSinglePost(postId)
    }

    override suspend fun deleteBloodRequest(postId: String): BaseResponse<Boolean> {
        return homeApi.deletePost(postId)
    }

    override suspend fun getUserActivePost(userId: String): BaseResponse<PostResponse> {
        return homeApi.getUserActivePost(userId)
    }

    override suspend fun updateBloodRequest(requestBody: UpdatePostRequestBody): BaseResponse<PostResponse> {
        return homeApi.updatePost(requestBody)
    }

    override suspend fun setPostActiveness(requestBody: SetPostActivenessRequestBody): BaseResponse<PostResponse> {
        return homeApi.setPostActiveness(requestBody)
    }

    override suspend fun getStoriesFromContentful(): List<ContentfulStoryDto> = withContext(Dispatchers.IO) {
        // Anahtarlar local.properties'ten gelir (bkz. local.properties.example); yoksa hikâye gösterilmez.
        if (BuildConfig.CONTENTFUL_SPACE_ID.isBlank() || BuildConfig.CONTENTFUL_ACCESS_TOKEN.isBlank()) {
            return@withContext emptyList()
        }
        val client = CDAClient.builder()
            .setSpace(BuildConfig.CONTENTFUL_SPACE_ID)
            .setToken(BuildConfig.CONTENTFUL_ACCESS_TOKEN)
            .build()

        val entries = client.fetch(CDAEntry::class.java)
            .where("content_type", "story")
            .all()
            .items()

        Log.d("ContentfulDebug", "Story entry count: ${entries.size}")

        entries.mapNotNull { entry ->
            entry as? CDAEntry ?: return@mapNotNull null
            try {

                val userIconAssetId = entry.getField<CDAAsset>("userIconUrl").id()
                val userIconAsset = client.fetch(CDAAsset::class.java)
                    .one(userIconAssetId).url()


                val storyImageAssetId = entry.getField<CDAAsset>("storyImageUrl").id()
                val storyImageAsset = client.fetch(CDAAsset::class.java)
                    .one(storyImageAssetId).url()

                Log.d("ContentfulDebug", "Entry id: ${entry.id()}")
                val name = entry.getField<String>("name")
                val description = entry.getField<String>("description")
                val webUrl = entry.getField<String>("webUrl")

                Log.d("ContentfulDebug", "userIconAsset: $userIconAsset, storyImageAsset: $storyImageAsset, name: $name, description: $description, webUrl: $webUrl")

                ContentfulStoryDto(
                    id = entry.id(),
                    userIconUrl = "https:$userIconAsset",
                    storyImageUrl = "https:$storyImageAsset",
                    name = name,
                    description = description,
                    webUrl = webUrl
                )
            } catch (e: Exception) {
                Log.e("ContentfulDebug", "Error parsing entry: ${entry.id()} - ${e.message}", e)
                null
            }
        }
    }
}
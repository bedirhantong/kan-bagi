package com.ribuufing.bloodapp.feature.home.data.remote

import android.util.Log
import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.home.data.dto.AllPostsResponse
import com.ribuufing.bloodapp.feature.home.data.dto.PostResponse
import com.ribuufing.bloodapp.feature.home.data.dto.SetPostActivenessRequestBody
import com.ribuufing.bloodapp.feature.home.data.dto.UpdatePostRequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface HomeApi {

    @GET(GET_ALL_POSTS)
    suspend fun getAllPosts(
        @Query("UserId", encoded = true) userId: String? = null,
        @Query("Hospitals", encoded = true) hospitals: List<Int>?,
        @Query("BloodTypes", encoded = true) bloodTypes: List<String>? = null,
        @Query("ShowingResultsFrom") showingResultsFrom: Int = 1,
        @Query("Paging") paging: Int = 10,
        @Query("Sorting") sorting: String = "Newest"
    ): BaseResponse<AllPostsResponse>

    @GET(GET_SINGLE_POST)
    suspend fun getSinglePost(
        @Query("id", encoded = true) postId: String
    ): BaseResponse<PostResponse>

    @DELETE(DELETE_POST)
    suspend fun deletePost(
        @Query("id", encoded = true) postId: String
    ): BaseResponse<Boolean>

    @GET(USER_ACTIVE_POST)
    suspend fun getUserActivePost(
        @Query("UserId", encoded = true) userId: String
    ): BaseResponse<PostResponse>

    @PUT(UPDATE_POST)
    suspend fun updatePost(
        @Body requestBody: UpdatePostRequestBody
    ): BaseResponse<PostResponse>

    @POST(SET_POST_ACTIVENESS)
    suspend fun setPostActiveness(
        @Body requestBody: SetPostActivenessRequestBody
    ): BaseResponse<PostResponse>

    @GET(GET_USER_DONATED_POSTS)
    suspend fun getUserDonatedPosts(
        @Path("userId") userId: String
    ): List<PostResponse>

    private companion object{
        const val GET_ALL_POSTS = "Post/get-all-posts"
        const val GET_SINGLE_POST = "Post/get-post"
        const val DELETE_POST = "Post/delete-post"
        const val USER_ACTIVE_POST ="Post/get-user-active-post"
        const val UPDATE_POST = "Post/update-post"
        const val SET_POST_ACTIVENESS ="Post/set-post-activeness"
        const val GET_USER_DONATED_POSTS = "aggregate/user/{userId}/posts"
    }
}
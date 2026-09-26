package com.ribuufing.bloodapp.feature.home.data.dto

import com.google.gson.annotations.SerializedName

data class PlaceHolder(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("price")
    val price: Double? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("image")
    val image: String? = null,
    @SerializedName("rating")
    val rating: Rating? = null
)

data class Rating(
    @SerializedName("rate")
    val rate: Double? = null,
    @SerializedName("count")
    val count: Int? = null
)

data class ContentfulStoryDto(
    val id: String,
    val userIconUrl: String?,
    val storyImageUrl: String?,
    val name: String?,
    val description: String?,
    val webUrl: String?
)

package com.ribuufing.bloodapp.feature.postdetail.data

data class CreateMatchingRequestBody(
    val donorId: String ? = null,
    val postId: String? = null,
    val ownerId: String? = null
)

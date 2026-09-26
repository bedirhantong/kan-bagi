package com.ribuufing.bloodapp.feature.postdetail.domain.response

data class ValidateMatchingResponse(
    val matchingId: String? = null,
    val donorId: String? = null,
    val postId: String? = null,
    val ownerId: String? = null,
    val isVerified: Boolean? = null,
    val creationTime: String? = null,
    val lastUpdatedTime: String? = null
)

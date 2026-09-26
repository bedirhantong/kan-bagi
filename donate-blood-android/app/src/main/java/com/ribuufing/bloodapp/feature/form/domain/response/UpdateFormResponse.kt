package com.ribuufing.bloodapp.feature.form.domain.response

data class UpdateFormResponse(
    val formId: Long? = null,
    val donorId: String? = null,
    val matchingId: Long? = null,
    val formData: String? = null,
    val creationTime: String? = null,
    val lastUpdatedTime: String? = null
)

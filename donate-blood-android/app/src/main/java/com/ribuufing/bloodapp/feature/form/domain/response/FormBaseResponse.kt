package com.ribuufing.bloodapp.feature.form.domain.response

data class FormBaseResponse<T>(
    val response: T?,
    val success: Boolean,
    val resultMessage: String?,
    val resultCode: String?
)
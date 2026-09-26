package com.ribuufing.bloodapp.feature.postdetail.data

data class QrBaseResponse<T>(
    val response: T?,
    val success: Boolean,
    val resultMessage: String?,
    val resultCode: String?
)
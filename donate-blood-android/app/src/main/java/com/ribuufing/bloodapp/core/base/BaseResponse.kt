package com.ribuufing.bloodapp.core.base

data class BaseResponse<T>(
    val response: T?,
    val isSuccess: Boolean,
    val resultMessage: String?,
    val resultCode: String?
) 
package com.ribuufing.bloodapp.core.data.remote

sealed class ApiResult<out T> {
    class Success<T>(val response: T?) : ApiResult<T>()
    class Error(val error: ApiError) : ApiResult<Nothing>()
}
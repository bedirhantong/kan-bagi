package com.ribuufing.bloodapp.core.data

import com.ribuufing.bloodapp.core.data.remote.ApiError
import com.ribuufing.bloodapp.core.data.remote.ApiResult
import com.ribuufing.bloodapp.core.data.remote.Const.Companion.ERROR_CODE_UNAUTHORIZED
import retrofit2.Response
import java.net.ConnectException
import kotlinx.coroutines.flow.MutableStateFlow

interface ApiExecutor {

    suspend fun <T> execute(
        call: suspend () -> Response<T>,
        showLoadingDialog: Boolean = true,
        apiStateFlow: MutableStateFlow<LoadingState>
    ): ApiResult<T> {
        val response: Response<T>
        if (showLoadingDialog){
            apiStateFlow.emit(LoadingState(Visibility.SHOW))
        }
        try {
            response = call.invoke()
            if (showLoadingDialog){
                apiStateFlow.emit(LoadingState(Visibility.HIDE))
            }
            return if (response.isSuccessful) {
                ApiResult.Success(response.body())
            } else {
                if (response.code() == ERROR_CODE_UNAUTHORIZED) {
                    return ApiResult.Error(ApiError.Authentication(response.errorBody()))
                }
                ApiResult.Error(ApiError.Server(response.errorBody()))
            }
        } catch (exception: Exception) {
            if (showLoadingDialog){
                apiStateFlow.emit(LoadingState(Visibility.HIDE))
            }
            if (exception is ConnectException) {
                return ApiResult.Error(ApiError.NoInternet(exception.message))
            }
            return ApiResult.Error(ApiError.IO(exception.message))
        }

    }
}
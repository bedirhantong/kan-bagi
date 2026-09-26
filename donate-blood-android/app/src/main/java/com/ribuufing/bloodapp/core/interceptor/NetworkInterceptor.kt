package com.ribuufing.bloodapp.core.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class NetworkInterceptor @Inject constructor(
    private val headerProvider: HeaderProvider
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
        
        headerProvider.getHeaders().forEach { (key, value) ->
            requestBuilder.header(key, value)
        }
        
        return chain.proceed(requestBuilder.build())
    }
} 
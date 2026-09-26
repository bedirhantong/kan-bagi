package com.ribuufing.bloodapp.core.interceptor

import android.content.Context
import android.os.Build
import com.ribuufing.bloodapp.core.manager.AuthManager
import com.ribuufing.bloodapp.core.utils.NetworkUtils
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class IpAddressInterceptor @Inject constructor(
    private val context: Context,
    private val authManager: AuthManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        val deviceIp = if (isEmulator()) {
            "10.0.0.1"
        } else {
            NetworkUtils.getDeviceIpAddress(context)
        }
        
        authManager.ipAddress = deviceIp
        
        val requestWithIp = originalRequest.newBuilder()
            .addHeader("X-Forwarded-For", deviceIp)
            .build()
            
        return chain.proceed(requestWithIp)
    }
    
    private fun isEmulator(): Boolean {
        return Build.FINGERPRINT.contains("generic") ||
               Build.FINGERPRINT.startsWith("google/sdk_gphone") ||
               Build.MODEL.contains("sdk") ||
               Build.MODEL.contains("Emulator") ||
               Build.MODEL.contains("Android SDK")
    }
}
package com.ribuufing.bloodapp.core

import android.content.Context
import com.ribuufing.bloodapp.BuildConfig
import com.ribuufing.bloodapp.core.interceptor.IpAddressInterceptor
import com.ribuufing.bloodapp.core.interceptor.NetworkInterceptor
import com.ribuufing.bloodapp.core.manager.AuthManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    fun provideIpAddressInterceptor(
        @ApplicationContext context: Context,
        authManager: AuthManager
    ): IpAddressInterceptor {
        return IpAddressInterceptor(context, authManager)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        networkInterceptor: NetworkInterceptor,
        ipAddressInterceptor: IpAddressInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor(networkInterceptor)
            .addInterceptor(ipAddressInterceptor)
            .readTimeout(90, TimeUnit.SECONDS)
            .connectTimeout(90, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, authManager: AuthManager): Retrofit {
        val ip = authManager.ipAddress

        /*
        bedirhantong@Bedirhan-MacBook-Air ~ % ipconfig getifaddr en0
         */

        val baseUrl = if (isEmulator()) {
            "http://10.0.2.2:8000/"
        } else {
            "http://192.168.1.125:8000/"
        }

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

private fun isEmulator(): Boolean {
    return android.os.Build.FINGERPRINT.contains("generic") ||
           android.os.Build.FINGERPRINT.startsWith("google/sdk_gphone") ||
           android.os.Build.MODEL.contains("sdk") ||
           android.os.Build.MODEL.contains("Emulator") ||
           android.os.Build.MODEL.contains("Android SDK")
}
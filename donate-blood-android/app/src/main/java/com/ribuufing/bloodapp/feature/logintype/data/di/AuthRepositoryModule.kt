package com.ribuufing.bloodapp.feature.logintype.data.di

import com.ribuufing.bloodapp.feature.logintype.data.remote.AuthRepositoryImpl
import com.ribuufing.bloodapp.feature.logintype.data.remote.LoginApi
import com.ribuufing.bloodapp.feature.logintype.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
class AuthRepositoryModule {

    @Provides
    fun provideLoginApiService(retrofit: Retrofit) : LoginApi {
        return retrofit.create(LoginApi::class.java)
    }

    @Provides
    fun provideLoginRepository(loginApi: LoginApi) : AuthRepository = AuthRepositoryImpl(loginApi)
}
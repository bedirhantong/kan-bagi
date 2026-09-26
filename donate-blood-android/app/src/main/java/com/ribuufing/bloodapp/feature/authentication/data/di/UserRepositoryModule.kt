package com.ribuufing.bloodapp.feature.authentication.data.di

import com.ribuufing.bloodapp.feature.authentication.data.UserRepositoryImpl
import com.ribuufing.bloodapp.feature.authentication.data.remote.AuthApi
import com.ribuufing.bloodapp.feature.authentication.domain.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
class UserRepositoryModule {

    @Provides
    fun provideAuthApiService(retrofit: Retrofit) : AuthApi{
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    fun provideUserRepository(userApi: AuthApi) : UserRepository = UserRepositoryImpl(userApi)

}
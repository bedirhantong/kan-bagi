package com.ribuufing.bloodapp.feature.profile.data.di

import com.ribuufing.bloodapp.feature.profile.data.ProfileRepositoryImpl
import com.ribuufing.bloodapp.feature.profile.data.remote.ProfileApi
import com.ribuufing.bloodapp.feature.profile.domain.repository.ProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
class LoginModule {
    @Provides
    fun provideProfileApiService(retrofit: Retrofit) : ProfileApi {
        return retrofit.create(ProfileApi::class.java)
    }

    @Provides
    fun provideProfileRepository(profileApi: ProfileApi) : ProfileRepository =
        ProfileRepositoryImpl(profileApi)
}
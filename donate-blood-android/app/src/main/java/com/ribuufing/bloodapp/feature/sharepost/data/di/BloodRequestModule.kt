package com.ribuufing.bloodapp.feature.sharepost.data.di

import com.ribuufing.bloodapp.feature.sharepost.data.remote.BloodRequestApi
import com.ribuufing.bloodapp.feature.sharepost.data.repository.BloodRequestRepositoryImpl
import com.ribuufing.bloodapp.feature.sharepost.domain.BloodRequestRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import retrofit2.Retrofit

@Module
@InstallIn(ViewModelComponent::class)
class BloodRequestModule {
    @Provides
    fun provideBloodRequestApiService(retrofit: Retrofit) : BloodRequestApi {
        return retrofit.create(BloodRequestApi::class.java)
    }

    @Provides
    fun provideBloodRequestRepository(api: BloodRequestApi) : BloodRequestRepository =  BloodRequestRepositoryImpl(api)
}
package com.ribuufing.bloodapp.feature.home.data.di

import com.ribuufing.bloodapp.feature.home.data.HomeRepositoryImpl
import com.ribuufing.bloodapp.feature.home.data.remote.HomeApi
import com.ribuufing.bloodapp.feature.home.domain.HomeRepository
import com.ribuufing.bloodapp.feature.postdetail.data.MatchingRepositoryImpl
import com.ribuufing.bloodapp.feature.postdetail.data.QrApi
import com.ribuufing.bloodapp.feature.postdetail.domain.repository.MatchingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import retrofit2.Retrofit

@Module
@InstallIn(ViewModelComponent::class)
class HomeModule {
    @Provides
    fun provideHomeApiService(retrofit: Retrofit) : HomeApi{
        return retrofit.create(HomeApi::class.java)
    }

    @Provides
    fun provideQrApiService(retrofit: Retrofit) : QrApi{
        return retrofit.create(QrApi::class.java)
    }

    @Provides
    fun provideHomeRepository(homeApi: HomeApi) : HomeRepository = HomeRepositoryImpl(homeApi)

    @Provides
    fun provideMatchingRepository(qrApi: QrApi) : MatchingRepository = MatchingRepositoryImpl(qrApi)
}
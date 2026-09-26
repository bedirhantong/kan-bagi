package com.ribuufing.bloodapp.feature.dmchat.data.di

import com.ribuufing.bloodapp.feature.dmchat.data.DmRepositoryImpl
import com.ribuufing.bloodapp.feature.dmchat.data.remote.DmApi
import com.ribuufing.bloodapp.feature.dmchat.domain.DmRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DmChatModule {

    @Provides
    @Singleton
    fun provideDmApi(retrofit: Retrofit): DmApi {
        return retrofit.create(DmApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDmRepository(dmApi: DmApi): DmRepository {
        return DmRepositoryImpl(dmApi)
    }
} 
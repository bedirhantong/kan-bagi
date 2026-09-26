package com.ribuufing.bloodapp.feature.settings.data.di

import com.ribuufing.bloodapp.feature.settings.data.NotificationPreferencesRepositoryImpl
import com.ribuufing.bloodapp.feature.settings.data.remote.SettingsApi
import com.ribuufing.bloodapp.feature.settings.domain.repository.NotificationPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
class SettingsModule {
    @Provides
    fun provideSettingsApiService(retrofit: Retrofit) : SettingsApi {
        return retrofit.create(SettingsApi::class.java)
    }

    @Provides
    fun provideProfileRepository(settingsApi: SettingsApi) : NotificationPreferencesRepository =
        NotificationPreferencesRepositoryImpl(settingsApi)
}
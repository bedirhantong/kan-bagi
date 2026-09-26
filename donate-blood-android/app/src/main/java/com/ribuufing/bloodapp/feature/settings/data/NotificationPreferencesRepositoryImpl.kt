package com.ribuufing.bloodapp.feature.settings.data

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.settings.data.remote.SettingsApi
import com.ribuufing.bloodapp.feature.settings.domain.repository.NotificationPreferencesRepository
import com.ribuufing.bloodapp.feature.settings.domain.response.NotificationPreferencesRequest
import com.ribuufing.bloodapp.feature.settings.domain.response.NotificationPreferencesResponse
import javax.inject.Inject

class NotificationPreferencesRepositoryImpl @Inject constructor(
    private val settingsApi: SettingsApi
) : NotificationPreferencesRepository{
    override suspend fun setNotificationPreferences(request: NotificationPreferencesRequest): BaseResponse<NotificationPreferencesResponse> {
        return settingsApi.setNotificationSettings(request)
    }

    override suspend fun getNotificationPreferences(): BaseResponse<NotificationPreferencesResponse> {
        return settingsApi.getNotificationPreferences()
    }
}
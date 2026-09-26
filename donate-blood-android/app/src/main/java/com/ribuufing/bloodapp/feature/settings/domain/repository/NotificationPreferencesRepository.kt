package com.ribuufing.bloodapp.feature.settings.domain.repository

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.settings.domain.response.NotificationPreferencesRequest
import com.ribuufing.bloodapp.feature.settings.domain.response.NotificationPreferencesResponse

interface NotificationPreferencesRepository {
    suspend fun setNotificationPreferences(request: NotificationPreferencesRequest)
    : BaseResponse<NotificationPreferencesResponse>

    suspend fun getNotificationPreferences() : BaseResponse<NotificationPreferencesResponse>
}
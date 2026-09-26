package com.ribuufing.bloodapp.feature.settings.data.remote

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.settings.domain.response.NotificationPreferencesRequest
import com.ribuufing.bloodapp.feature.settings.domain.response.NotificationPreferencesResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SettingsApi {
    @POST(NOTIFICATION_SETTINGS)
    suspend fun setNotificationSettings(
        @Body request: NotificationPreferencesRequest,
    ) : BaseResponse<NotificationPreferencesResponse>


    @GET(GET_NOTIFICATION_SETTINGS)
    suspend fun getNotificationPreferences() : BaseResponse<NotificationPreferencesResponse>

    private companion object {
        const val NOTIFICATION_SETTINGS = "Profile/set-notification-preferences"
        const val GET_NOTIFICATION_SETTINGS = "Profile/get-notification-preferences"
    }
}
package com.ribuufing.bloodapp.feature.settings.domain.usecase

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.settings.domain.repository.NotificationPreferencesRepository
import com.ribuufing.bloodapp.feature.settings.domain.response.NotificationPreferencesRequest
import com.ribuufing.bloodapp.feature.settings.domain.response.NotificationPreferencesResponse
import javax.inject.Inject

class SetNotificationPreferencesUseCase @Inject constructor(
    private val repository: NotificationPreferencesRepository
){
    suspend operator fun invoke(
        request: NotificationPreferencesRequest,
    ) : BaseResponse<NotificationPreferencesResponse> {
        return repository.setNotificationPreferences(request)
    }
}
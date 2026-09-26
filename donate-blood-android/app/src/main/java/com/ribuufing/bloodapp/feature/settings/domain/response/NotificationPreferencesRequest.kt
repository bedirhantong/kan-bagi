package com.ribuufing.bloodapp.feature.settings.domain.response

data class NotificationPreferencesRequest(
    val email: Boolean?=null,
    val phoneNumber: Boolean?=null,
    val pushNotification: Boolean?=null,
    val preferredHospitals: List<String>?=null,
    val preferredBloodTypes: List<String>?=null
)

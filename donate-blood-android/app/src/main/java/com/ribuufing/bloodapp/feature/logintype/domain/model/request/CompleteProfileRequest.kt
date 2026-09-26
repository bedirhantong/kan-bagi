package com.ribuufing.bloodapp.feature.logintype.domain.model.request

data class CompleteProfileRequest(
    val name: String?=null,
    val surname: String?=null,
    val phoneNumber: String?=null,
    val tcIdentityNumber: String?=null,
    val birthDate: String?=null,
    val bloodType: String?=null,
    val userType: String?=null,
    val gender: String?=null
)

package com.ribuufing.bloodapp.feature.profile.domain.response

data class GetProfileResponse(
    val id: String? = null,
    val name: String? = null,
    val surname: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val tcIdentityNumber: String? = null,
    val birthDate: String? = null,
    val bloodType: String? = null,
    val gender: String? = null,
    val userType: String? = null
)

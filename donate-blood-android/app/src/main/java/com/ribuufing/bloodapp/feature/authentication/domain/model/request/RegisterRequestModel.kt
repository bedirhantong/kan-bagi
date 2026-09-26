package com.ribuufing.bloodapp.feature.authentication.domain.model.request

data class RegisterRequestModel(
    val email: String? = null,
    val password: String? = null,
    val confirmPassword: String? = null,
    val name: String? = null,
    val surname: String? = null,
    val tcIdentityNumber: String? = null,
    val birthDate: String? = null,
    val bloodType: Int? = null,
    val userType: Int? = null
)

enum class BloodType {
    A_POSITIVE,    // 0
    A_NEGATIVE,    // 1
    B_POSITIVE,    // 2
    B_NEGATIVE,    // 3
    AB_POSITIVE,   // 4
    AB_NEGATIVE,   // 5
    O_POSITIVE,    // 6
    O_NEGATIVE     // 7
}

enum class UserType {
    DONOR,         // 0
    RECIPIENT      // 1
}
package com.ribuufing.bloodapp.feature.home.domain.uimodel

enum class BloodType {
    A_POSITIVE,
    A_NEGATIVE,
    B_POSITIVE,
    B_NEGATIVE,
    AB_POSITIVE,
    AB_NEGATIVE,
    O_POSITIVE,
    O_NEGATIVE,
    UNKNOWN;

    fun displayName(): String = when(this) {
        A_POSITIVE -> "A+"
        A_NEGATIVE -> "A-"
        B_POSITIVE -> "B+"
        B_NEGATIVE -> "B-"
        AB_POSITIVE -> "AB+"
        AB_NEGATIVE -> "AB-"
        O_POSITIVE -> "O+"
        O_NEGATIVE -> "O-"
        UNKNOWN -> "Unknown"
    }
}

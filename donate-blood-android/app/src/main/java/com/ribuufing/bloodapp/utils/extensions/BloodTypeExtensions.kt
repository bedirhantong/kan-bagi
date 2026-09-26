package com.ribuufing.bloodapp.utils.extensions

class BloodTypeExtensions {
    private fun formatBloodType(bloodType: String): String {
        return when (bloodType) {
            "B-" -> "B_Negative"
            "B+" -> "B_Positive"
            "A-" -> "A_Negative"
            "A+" -> "A_Positive"
            "AB-" -> "AB_Negative"
            "AB+" -> "AB_Positive"
            "O-" -> "O_Negative"
            "O+" -> "O_Positive"
            else -> bloodType
        }
    }
}
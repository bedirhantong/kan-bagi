package com.ribuufing.bloodapp.feature.sharepost.domain.model

data class Hospital(
    val id: Int,
    val name: String,
    val city: String,
    val address: String,
    val icon: String,
    val latitude: Double,
    val longitude: Double
)

data class BloodType(
    val id: String,
    val type: String,
    val description: String = ""
) 
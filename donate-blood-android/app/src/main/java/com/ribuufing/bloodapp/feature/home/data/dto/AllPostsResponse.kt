package com.ribuufing.bloodapp.feature.home.data.dto

import com.ribuufing.bloodapp.feature.listhospitals.data.HospitalResponse

data class PostResponse(
    val id: String? = null,
    val ownerId: String? = null,
    val ownerName: String? = null,
    val ownerSurname: String? = null,
    val ownerEmail : String? = null,
    val patientFullName: String? = null,
    val patientAge: Int? = null,
    val title: String? = null,
    val description: String? = null,
    val phoneNumbers: List<String>? = null,
    val bloodType: String? = null,
    val isActive : Boolean? = null,
    val hospital: HospitalResponse? = null,
    val isVerified : Boolean? = null
)

data class AllPostsResponse(
    val items: List<PostResponse>? = null,
    val totalCount: Int? = null
)

package com.ribuufing.bloodapp.feature.home.data.dto

data class UpdatePostRequestBody(
    val id: String?=null,
    val patientFullName: String?=null,
    val patientAge: Int?=null,
    val title: String?=null,
    val description: String?=null,
    val phoneNumbers: List<String>?=null,
    val bloodType: String?=null,
    val hospitalId: String?=null,
    val hospitalName: String?=null,
    val hospitalAddress: String?=null,
    val hospitalIcon: String?=null,
    val isActive: Boolean?=null
)

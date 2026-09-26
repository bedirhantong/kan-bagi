package com.ribuufing.bloodapp.feature.sharepost.data.model

data class CreateBloodRequestBody(
    val ownerName: String,
    val ownerSurname: String,
    val patientFullName: String,
    val patientAge: Int,
    val title: String,
    val description: String,
    val phoneNumbers: List<String>,
    val bloodType: String,
    val hospitalId: Int,
) {
    fun toRequestBody(): HashMap<String, Any> {
        return hashMapOf(
            "ownerName" to ownerName,
            "ownerSurname" to ownerSurname,
            "patientFullName" to patientFullName,
            "patientAge" to patientAge,
            "title" to title,
            "description" to description,
            "phoneNumbers" to phoneNumbers,
            "bloodType" to bloodType,
            "hospitalId" to hospitalId,
            )
    }
}
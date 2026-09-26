package com.ribuufing.bloodapp.feature.listhospitals.data

data class HospitalListResponse(
    val items: List<HospitalResponse>? = null,
    val totalCount: Int? = null,
)

data class HospitalResponse(
    val id: Int? = null,
    val name: String? = null,
    val address: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val webSite: String? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    val districtId: Int? = null,
    val districtName: String? = null,
    val cityId: Int?= null,
    val cityName: String? = null,
    val countryId: Int? = null,
    val countryName: String? = null,
    val hospitalIcon : String? = "https://upload.wikimedia.org/wikipedia/commons/2/2c/Logo_of_Ministry_of_Health_%28Turkey%29.png"
)

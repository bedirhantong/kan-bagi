package com.ribuufing.bloodapp.feature.sharepost.domain.mapper

import com.ribuufing.bloodapp.feature.listhospitals.data.HospitalResponse
import com.ribuufing.bloodapp.feature.sharepost.domain.model.Hospital

object HospitalMapper {
    fun HospitalResponse.toHospital(): Hospital {
        return Hospital(
            id = this.id ?: 0,
            name = this.name ?: "",
            city = this.cityName ?: "",
            address = this.address ?: "",
            icon = this.hospitalIcon ?: "",
            latitude = this.lat ?: 0.0,
            longitude = this.lon ?: 0.0
        )
    }

    fun List<HospitalResponse>.toHospitalList(): List<Hospital> {
        return this.map { it.toHospital() }
    }
} 
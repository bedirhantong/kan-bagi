package com.ribuufing.bloodapp.feature.listhospitals.data

import com.ribuufing.bloodapp.core.base.BaseResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface HospitalApi {

    @GET(GET_ALL_HOSPITAL_ENDPOINT)
    suspend fun getAllHospitals(
        @Query("Name", encoded = true) Name: String? = null,
        @Query("LatStart", encoded = true) LatStart: Double? = null,
        @Query("LatEnd", encoded = true) LatEnd: Double? = null,
        @Query("LonStart", encoded = true) LonStart: Double? = null,
        @Query("LonEnd", encoded = true) LonEnd: Double? = null,
        @Query ("CityId", encoded = true) CityId: Int? = null,
        @Query("DistrictId", encoded = true) DistrictId: Int? = null,
        @Query("CountryId", encoded = true) CountryId: Int? = null,
        @Query("ShowingResultsFrom", encoded = true) ShowingResultsFrom: Int? = null,
        @Query("Paging", encoded = true) Paging: Int? = null,
        @Query("Sorting", encoded = true) Sorting: String? = null
    ): BaseResponse<HospitalListResponse>

    @GET(GET_SINGLE_HOSPITAL_ENDPOINT)
    suspend fun getHospital(
        @Query("Id", encoded = true) Id: Int? = null,
    ): BaseResponse<HospitalResponse>

    private companion object {
        const val GET_ALL_HOSPITAL_ENDPOINT = "Hospital/get-all-hospitals"
        const val GET_SINGLE_HOSPITAL_ENDPOINT ="Hospital/get-hospital"
    }
}



package com.ribuufing.bloodapp.feature.listhospitals.data

import com.ribuufing.bloodapp.feature.listhospitals.domain.HospitalRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import retrofit2.Retrofit

@Module
@InstallIn(ViewModelComponent::class)
class HospitalModule {
    @Provides
    fun provideHospitalApiService(retrofit: Retrofit) : HospitalApi {
        return retrofit.create(HospitalApi::class.java)
    }

    @Provides
    fun provideHospitalRepository(hospitalApi: HospitalApi) : HospitalRepository {
        return HospitalRepositoryImpl(hospitalApi)
    }
}
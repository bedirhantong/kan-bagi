package com.ribuufing.bloodapp.feature.form.data

import com.ribuufing.bloodapp.feature.form.domain.FormRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import retrofit2.Retrofit

@Module
@InstallIn(ViewModelComponent::class)
class FormModule {
    @Provides
    fun provideFormApi(retrofit: Retrofit) : FormApi {
        return retrofit.create(FormApi::class.java)
    }

    @Provides
    fun provideFormRepository(formApi: FormApi) : FormRepository {
        return FormRepositoryImpl(formApi)
    }
}
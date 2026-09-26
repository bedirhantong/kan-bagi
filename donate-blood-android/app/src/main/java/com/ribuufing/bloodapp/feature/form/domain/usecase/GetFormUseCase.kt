package com.ribuufing.bloodapp.feature.form.domain.usecase

import com.ribuufing.bloodapp.feature.form.data.FormRepository
import com.ribuufing.bloodapp.feature.form.domain.response.FormBaseResponse
import com.ribuufing.bloodapp.feature.form.domain.response.GetFormResponse
import javax.inject.Inject

class GetFormUseCase @Inject constructor(
    private val formRepository: FormRepository
){
    suspend operator fun invoke() :FormBaseResponse<GetFormResponse> {
        return formRepository.getForm()
    }
}
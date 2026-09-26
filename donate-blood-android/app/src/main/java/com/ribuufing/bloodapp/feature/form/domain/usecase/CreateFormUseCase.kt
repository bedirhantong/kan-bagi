package com.ribuufing.bloodapp.feature.form.domain.usecase

import com.ribuufing.bloodapp.feature.form.data.FormRepository
import com.ribuufing.bloodapp.feature.form.data.request.CreateFormRequest
import com.ribuufing.bloodapp.feature.form.domain.response.CreateFormResponse
import com.ribuufing.bloodapp.feature.form.domain.response.FormBaseResponse
import javax.inject.Inject

class CreateFormUseCase @Inject constructor(
    private val formRepository: FormRepository
){
    suspend operator fun invoke(
        createFormRequest: CreateFormRequest
    ) : FormBaseResponse<CreateFormResponse> {
        return formRepository.createForm(createFormRequest)
    }
}
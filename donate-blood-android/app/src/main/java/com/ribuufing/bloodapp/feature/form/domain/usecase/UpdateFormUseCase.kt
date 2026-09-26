package com.ribuufing.bloodapp.feature.form.domain.usecase

import com.ribuufing.bloodapp.feature.form.data.FormRepository
import com.ribuufing.bloodapp.feature.form.data.request.CreateFormRequest
import com.ribuufing.bloodapp.feature.form.domain.response.FormBaseResponse
import com.ribuufing.bloodapp.feature.form.domain.response.UpdateFormResponse
import javax.inject.Inject

class UpdateFormUseCase @Inject constructor(
    private val formRepository: FormRepository
){
    suspend operator fun invoke(
        createFormRequest: CreateFormRequest,
    ) : FormBaseResponse<UpdateFormResponse>{
        return formRepository.updateForm(createFormRequest)
    }
}
package com.ribuufing.bloodapp.feature.form.domain

import com.ribuufing.bloodapp.core.data.ApiExecutor
import com.ribuufing.bloodapp.feature.form.data.FormApi
import com.ribuufing.bloodapp.feature.form.data.FormRepository
import com.ribuufing.bloodapp.feature.form.data.request.CreateFormRequest
import com.ribuufing.bloodapp.feature.form.domain.response.CreateFormResponse
import com.ribuufing.bloodapp.feature.form.domain.response.FormBaseResponse
import com.ribuufing.bloodapp.feature.form.domain.response.GetFormResponse
import com.ribuufing.bloodapp.feature.form.domain.response.GetQuestionsResponse
import com.ribuufing.bloodapp.feature.form.domain.response.UpdateFormResponse

class FormRepositoryImpl (
    private val formApi: FormApi
) : FormRepository, ApiExecutor{
    override suspend fun createForm(requestBody: CreateFormRequest): FormBaseResponse<CreateFormResponse> {
        return formApi.createForm(requestBody)
    }

    override suspend fun getQuestions(): FormBaseResponse<List<GetQuestionsResponse>> {
        return formApi.getQuestions()
    }

    override suspend fun updateForm(requestBody: CreateFormRequest): FormBaseResponse<UpdateFormResponse> {
        return formApi.updateForm(requestBody)
    }

    override suspend fun getForm(): FormBaseResponse<GetFormResponse> {
        return formApi.getForm()
    }
}
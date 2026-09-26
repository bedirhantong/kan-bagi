package com.ribuufing.bloodapp.feature.form.data

import com.ribuufing.bloodapp.feature.form.data.request.CreateFormRequest
import com.ribuufing.bloodapp.feature.form.domain.response.CreateFormResponse
import com.ribuufing.bloodapp.feature.form.domain.response.FormBaseResponse
import com.ribuufing.bloodapp.feature.form.domain.response.GetFormResponse
import com.ribuufing.bloodapp.feature.form.domain.response.GetQuestionsResponse
import com.ribuufing.bloodapp.feature.form.domain.response.UpdateFormResponse

interface FormRepository {
    suspend fun createForm(requestBody:  CreateFormRequest): FormBaseResponse<CreateFormResponse>
    suspend fun getQuestions() : FormBaseResponse<List<GetQuestionsResponse>>
    suspend fun updateForm(requestBody: CreateFormRequest) : FormBaseResponse<UpdateFormResponse>
    suspend fun getForm() : FormBaseResponse<GetFormResponse>
}
package com.ribuufing.bloodapp.feature.form.data

import com.ribuufing.bloodapp.feature.form.data.request.CreateFormRequest
import com.ribuufing.bloodapp.feature.form.domain.response.CreateFormResponse
import com.ribuufing.bloodapp.feature.form.domain.response.FormBaseResponse
import com.ribuufing.bloodapp.feature.form.domain.response.GetFormResponse
import com.ribuufing.bloodapp.feature.form.domain.response.GetQuestionsResponse
import com.ribuufing.bloodapp.feature.form.domain.response.UpdateFormResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface FormApi {

    @PUT(UPDATE_FORM)
    suspend fun updateForm(
        @Body requestBody:  CreateFormRequest
    ) : FormBaseResponse<UpdateFormResponse>

    @POST(CREATE_FORM)
    suspend fun createForm(
        @Body requestBody:  CreateFormRequest
    ) : FormBaseResponse<CreateFormResponse>

    @GET(GET_QUESTIONS)
    suspend fun getQuestions(
    ) : FormBaseResponse<List<GetQuestionsResponse>>

    @GET(GET_FORM)
    suspend fun getForm() : FormBaseResponse<GetFormResponse>

    private companion object{
        const val CREATE_FORM =  "evaluation-form/createForm"
        const val UPDATE_FORM =  "evaluation-form/updateForm"
        const val GET_QUESTIONS =  "evaluation-form/questions"
        const val GET_FORM =  "evaluation-form/getForm"
    }
}
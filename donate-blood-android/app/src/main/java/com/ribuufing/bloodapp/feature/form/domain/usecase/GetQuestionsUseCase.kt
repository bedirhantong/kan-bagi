package com.ribuufing.bloodapp.feature.form.domain.usecase

import com.ribuufing.bloodapp.feature.form.data.FormRepository
import com.ribuufing.bloodapp.feature.form.domain.response.FormBaseResponse
import com.ribuufing.bloodapp.feature.form.domain.response.GetQuestionsResponse
import javax.inject.Inject

class GetQuestionsUseCase @Inject constructor(
    private val formRepository: FormRepository
){
    suspend operator fun invoke(
    ) : FormBaseResponse<List<GetQuestionsResponse>>{
        return formRepository.getQuestions()
    }
}
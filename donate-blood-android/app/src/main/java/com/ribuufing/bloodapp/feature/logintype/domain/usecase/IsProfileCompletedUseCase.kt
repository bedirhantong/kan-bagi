package com.ribuufing.bloodapp.feature.logintype.domain.usecase

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.authentication.domain.model.response.IsCompleteResponse
import com.ribuufing.bloodapp.feature.logintype.domain.repository.AuthRepository
import javax.inject.Inject

class IsProfileCompletedUseCase @Inject constructor(
    private val repository: AuthRepository
){
    suspend operator fun invoke():BaseResponse<IsCompleteResponse>{
        return repository.isProfileCompleted()
    }
}
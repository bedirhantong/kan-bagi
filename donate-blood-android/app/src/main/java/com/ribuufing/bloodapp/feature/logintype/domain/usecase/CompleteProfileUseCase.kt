package com.ribuufing.bloodapp.feature.logintype.domain.usecase

import com.ribuufing.bloodapp.feature.logintype.domain.model.request.CompleteProfileRequest
import com.ribuufing.bloodapp.feature.logintype.domain.repository.AuthRepository
import javax.inject.Inject

class CompleteProfileUseCase @Inject constructor(
    private val repository: AuthRepository
){
    suspend operator fun invoke(
        completeProfileRequest: CompleteProfileRequest
    ) = repository.completeProfile(completeProfileRequest)
}
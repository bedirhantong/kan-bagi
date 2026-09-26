package com.ribuufing.bloodapp.feature.authentication.domain.usecase

import com.ribuufing.bloodapp.feature.authentication.domain.UserRepository
import com.ribuufing.bloodapp.feature.authentication.domain.model.request.RegisterRequestModel
import com.ribuufing.bloodapp.feature.authentication.domain.model.response.SignupResponse
import javax.inject.Inject

class RegisterUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {

}
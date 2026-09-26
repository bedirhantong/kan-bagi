package com.ribuufing.bloodapp.feature.authentication.domain.usecase

import com.ribuufing.bloodapp.feature.authentication.domain.UserRepository
import javax.inject.Inject

class LoginUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {

}
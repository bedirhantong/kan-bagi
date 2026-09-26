package com.ribuufing.bloodapp.feature.authentication.data

import com.ribuufing.bloodapp.feature.authentication.data.remote.AuthApi
import com.ribuufing.bloodapp.feature.authentication.domain.UserRepository
import com.ribuufing.bloodapp.feature.authentication.domain.model.request.RegisterRequestModel
import com.ribuufing.bloodapp.feature.authentication.domain.model.response.SignupResponse
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val authService: AuthApi
) : UserRepository {

}
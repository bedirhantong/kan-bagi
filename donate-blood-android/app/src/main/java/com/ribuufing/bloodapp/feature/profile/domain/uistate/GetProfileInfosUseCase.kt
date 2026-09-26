package com.ribuufing.bloodapp.feature.profile.domain.uistate

import com.ribuufing.bloodapp.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class GetProfileInfosUseCase @Inject constructor(
    private val repository: ProfileRepository
){
    suspend operator fun invoke() = repository.getProfileInfos()
}
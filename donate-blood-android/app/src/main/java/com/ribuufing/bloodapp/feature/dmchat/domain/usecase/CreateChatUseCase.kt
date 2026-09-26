package com.ribuufing.bloodapp.feature.dmchat.domain.usecase

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.dmchat.data.CreateRoomRequestBody
import com.ribuufing.bloodapp.feature.dmchat.domain.DmRepository
import com.ribuufing.bloodapp.feature.dmchat.domain.model.CreateRoomResponse
import javax.inject.Inject

class CreateChatUseCase @Inject constructor(
    private val dmRepository: DmRepository
) {
    suspend operator fun invoke(createRoomRequestBody: CreateRoomRequestBody) : BaseResponse<CreateRoomResponse>{
        return dmRepository.createChatWithUsersIds(createRoomRequestBody)
    }
}
package com.ribuufing.bloodapp.feature.dmchat.domain.usecase

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.dmchat.domain.DmRepository
import com.ribuufing.bloodapp.feature.dmchat.domain.model.GetRoomsResponse
import javax.inject.Inject

class GetUserChatsUseCase @Inject constructor(
    private val dmRepository: DmRepository
) {
    suspend operator fun invoke(userId: String) : BaseResponse<List<GetRoomsResponse>>{
        return dmRepository.getUserChats(userId)
    }
}
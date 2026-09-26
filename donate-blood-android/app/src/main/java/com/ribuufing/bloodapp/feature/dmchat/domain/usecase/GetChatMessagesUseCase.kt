package com.ribuufing.bloodapp.feature.dmchat.domain.usecase

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.dmchat.domain.DmRepository
import com.ribuufing.bloodapp.feature.dmchat.domain.model.GetChatMessagesResponse
import com.ribuufing.bloodapp.feature.dmchat.domain.model.MessageSingle
import javax.inject.Inject

class GetUserChatMessagesUseCase @Inject constructor(
    private val dmRepository: DmRepository
) {
    suspend operator fun invoke(roomId: String?,page: Int?,size: Int?) : BaseResponse<GetChatMessagesResponse> {
        return dmRepository.getChatMessages(roomId, page, size)
    }
}
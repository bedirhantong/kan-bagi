package com.ribuufing.bloodapp.feature.dmchat.data

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.dmchat.data.remote.DmApi
import com.ribuufing.bloodapp.feature.dmchat.domain.DmRepository
import com.ribuufing.bloodapp.feature.dmchat.domain.model.CreateRoomResponse
import com.ribuufing.bloodapp.feature.dmchat.domain.model.GetChatMessagesResponse
import com.ribuufing.bloodapp.feature.dmchat.domain.model.GetRoomsResponse
import com.ribuufing.bloodapp.feature.dmchat.domain.model.MessageSingle
import javax.inject.Inject

class DmRepositoryImpl @Inject constructor(
    private val dmService: DmApi
): DmRepository {
    override suspend fun createChatWithUsersIds(createRoomRequestBody: CreateRoomRequestBody): BaseResponse<CreateRoomResponse> {
        return dmService.createNewRoom(createRoomRequestBody)
    }

    override suspend fun getUserChats(userId: String): BaseResponse<List<GetRoomsResponse>> {
        return dmService.getRoomsByUserId(userId)
    }

    override suspend fun getChatMessages(
        roomId: String?,
        page: Int?,
        size: Int?
    ): BaseResponse<GetChatMessagesResponse> {
        return dmService.getRoomMessagesByRoomId(roomId, page, size)
    }

}
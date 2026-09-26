package com.ribuufing.bloodapp.feature.dmchat.domain

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.dmchat.data.CreateRoomRequestBody
import com.ribuufing.bloodapp.feature.dmchat.domain.model.CreateRoomResponse
import com.ribuufing.bloodapp.feature.dmchat.domain.model.GetChatMessagesResponse
import com.ribuufing.bloodapp.feature.dmchat.domain.model.GetRoomsResponse
import com.ribuufing.bloodapp.feature.dmchat.domain.model.MessageSingle

interface DmRepository {
    suspend fun createChatWithUsersIds(createRoomRequestBody: CreateRoomRequestBody) : BaseResponse<CreateRoomResponse>
    suspend fun getUserChats(userId: String) : BaseResponse<List<GetRoomsResponse>>
    suspend fun getChatMessages(roomId: String?,page: Int?,size: Int?) : BaseResponse<GetChatMessagesResponse>
}
package com.ribuufing.bloodapp.feature.dmchat.data.remote

import com.ribuufing.bloodapp.core.base.BaseResponse
import com.ribuufing.bloodapp.feature.dmchat.data.CreateRoomRequestBody
import com.ribuufing.bloodapp.feature.dmchat.domain.model.CreateRoomResponse
import com.ribuufing.bloodapp.feature.dmchat.domain.model.GetChatMessagesResponse
import com.ribuufing.bloodapp.feature.dmchat.domain.model.GetRoomsResponse
import retrofit2.http.*

interface DmApi {

    @POST(CREATE_NEW_ROOM)
    suspend fun createNewRoom(
        @Body requestBody: CreateRoomRequestBody
    ) : BaseResponse<CreateRoomResponse>

    @GET(GET_USER_ROOMS)
    suspend fun getRoomsByUserId(
        @Query("user_id", encoded = true) userId: String? = null,
    ): BaseResponse<List<GetRoomsResponse>>

    @GET(GET_ROOM_CHATS)
    suspend fun getRoomMessagesByRoomId(
        @Query("room_id", encoded = true) roomId: String? = null,
        @Query("page", encoded = true) page: Int? = null,
        @Query("size", encoded = true) size: Int? = null
    ): BaseResponse<GetChatMessagesResponse>

    private companion object {
        const val CREATE_NEW_ROOM = "chat/create-room"
        const val GET_USER_ROOMS =  "chat/get-rooms-by-user-id"
        const val GET_ROOM_CHATS =  "chat/get-messages-by-room-id"
    }
}
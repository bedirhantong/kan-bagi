package com.ribuufing.bloodapp.feature.dmchat.domain.model

data class GetRoomsResponse (
    val room_id: String? = null,
    val user1_id: String? = null,
    val user1_fullname : String? = null,
    val user2_id: String? = null,
    val user2_fullname : String? = null,
    val created_at: String? = null,
    val isSuccess: Boolean? = null,
    val last_message: LastMessage? = null
)

data class LastMessage(
    val sender_user_id: String? = null,
    val sender_fullname : String? = null,
    val receiver_user_id: String? = null,
    val receiver_fullname : String? = null,
    val content: String? = null,
    val room_id: String? = null,
    val timestamp: String? = null
)

data class GetChatMessagesResponse(
    val items : List<LastMessage>? = null,
    val total : Int? = null,
    val page : Int? = null,
    val size : Int? = null,
    val pages : Int? = null
)
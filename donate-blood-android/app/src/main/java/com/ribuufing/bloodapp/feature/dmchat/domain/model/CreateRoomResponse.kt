package com.ribuufing.bloodapp.feature.dmchat.domain.model

data class CreateRoomResponse(
    val room_id: String? = null,
    val user1_id: String? = null,
    val user1_fullname: String? = null,
    val user2_id: String? = null,
    val user2_fullname: String? = null,
    val created_at: String? = null,
    val isSuccess: Boolean? = null
)

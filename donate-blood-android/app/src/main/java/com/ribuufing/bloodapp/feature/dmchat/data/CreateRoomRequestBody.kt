package com.ribuufing.bloodapp.feature.dmchat.data

data class CreateRoomRequestBody (
    val user1_id: String? = null,
    val user2_id: String? = null,
    val user1_fullname: String? = null,
    val user2_fullname: String? = null
)
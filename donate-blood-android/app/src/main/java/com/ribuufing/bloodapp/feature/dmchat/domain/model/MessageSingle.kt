package com.ribuufing.bloodapp.feature.dmchat.domain.model

data class MessageSingle(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val content: String,
    val timestamp: Long,
    val isRead: Boolean = false
) 
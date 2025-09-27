package com.example.lab2

import com.google.firebase.Timestamp

data class ChatPreview(
    val chatId: String = "",
    val otherUserId: String = "",
    val otherUserName: String = "",
    val lastMessage: String = "",
    val lastTimestamp: Timestamp? = null,
    val unseenCount: Int = 0
)



data class Message(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null,
    val seenBy: List<String> = emptyList()
)

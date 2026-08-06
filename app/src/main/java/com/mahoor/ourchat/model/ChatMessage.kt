package com.mahoor.ourchat.model

import com.google.firebase.Timestamp

data class ChatMessage(
    val id: String = "",
    val sender: String = "",
    val text: String = "",
    val fileUrl: String? = null,
    val fileName: String? = null,
    val fileType: String = "text",
    val timestamp: Timestamp = Timestamp.now()
)
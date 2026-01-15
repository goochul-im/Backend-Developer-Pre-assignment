package com.assignment.chat.domain

import java.time.OffsetDateTime

data class Chat(
    val id: Long? = null,
    val threadId: Long,
    val question: String,
    val answer: String,
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)

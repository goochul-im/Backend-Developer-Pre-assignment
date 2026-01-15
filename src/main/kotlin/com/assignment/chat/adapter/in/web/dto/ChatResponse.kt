package com.assignment.chat.adapter.`in`.web.dto

import com.assignment.chat.domain.Chat
import java.time.OffsetDateTime

data class ChatResponse(
    val id: Long,
    val threadId: Long,
    val question: String,
    val answer: String,
    val createdAt: OffsetDateTime
) {
    companion object {
        fun from(chat: Chat): ChatResponse = ChatResponse(
            id = chat.id!!,
            threadId = chat.threadId,
            question = chat.question,
            answer = chat.answer,
            createdAt = chat.createdAt
        )
    }
}

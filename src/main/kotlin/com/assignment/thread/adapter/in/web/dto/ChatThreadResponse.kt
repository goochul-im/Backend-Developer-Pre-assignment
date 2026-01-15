package com.assignment.thread.adapter.`in`.web.dto

import com.assignment.thread.domain.ChatThread
import java.time.OffsetDateTime

data class ChatThreadResponse(
    val id: Long,
    val userId: Long,
    val lastActivityAt: OffsetDateTime,
    val createdAt: OffsetDateTime
) {
    companion object {
        fun from(chatThread: ChatThread): ChatThreadResponse = ChatThreadResponse(
            id = chatThread.id!!,
            userId = chatThread.userId,
            lastActivityAt = chatThread.lastActivityAt,
            createdAt = chatThread.createdAt
        )
    }
}

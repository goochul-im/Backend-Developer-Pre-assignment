package com.assignment.chat.application.port.out

import com.assignment.chat.domain.Chat
import java.time.OffsetDateTime

interface ChatRepository {
    fun save(chat: Chat): Chat
    fun findById(id: Long): Chat?
    fun findByThreadId(threadId: Long): List<Chat>
    fun findByThreadIds(threadIds: List<Long>): List<Chat>
    fun deleteByThreadId(threadId: Long)
    fun countByCreatedAtBetween(start: OffsetDateTime, end: OffsetDateTime): Long
    fun findByCreatedAtBetween(start: OffsetDateTime, end: OffsetDateTime): List<Chat>
}

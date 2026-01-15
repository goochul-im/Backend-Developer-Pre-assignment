package com.assignment.chat.application.port.out

import com.assignment.chat.domain.Chat
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ChatRepository {
    fun save(chat: Chat): Chat
    fun findById(id: Long): Chat?
    fun findByThreadId(threadId: Long): List<Chat>
    fun findByThreadIds(threadIds: List<Long>): List<Chat>
    fun deleteByThreadId(threadId: Long)
}

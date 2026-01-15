package com.assignment.thread.application.port.`in`

import com.assignment.thread.domain.ChatThread
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface QueryChatThreadsUseCase {
    fun getChatThreadsByUser(userId: Long, pageable: Pageable): Page<ChatThread>
    fun getAllChatThreads(pageable: Pageable): Page<ChatThread>
    fun getChatThreadById(threadId: Long): ChatThread
}
